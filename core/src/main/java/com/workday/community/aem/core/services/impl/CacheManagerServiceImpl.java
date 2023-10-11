package com.workday.community.aem.core.services.impl;

import com.adobe.xfa.ut.StringUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.utils.cache.LruCacheWithTimeout;
import com.workday.community.aem.core.utils.cache.ValueCallback;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CacheManagerService implementation class.
 */
@Component(service = CacheManagerService.class, property = {
    "service.pid=aem.core.services.cache.serviceCache"
}, configurationPid = "com.workday.community.aem.core.config.CacheConfig",
    configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = CacheConfig.class)
public class CacheManagerServiceImpl implements CacheManagerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerServiceImpl.class);

  private final LRUMap<String, ResourceResolver> resolverCache;

  private final Map<String, LoadingCache> caches;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private ScheduledFuture<?> cleanCacheHandle;

  /**
   * The resource resolver factory.
   */
  @Reference
  @Setter
  private ResourceResolverFactory resourceResolverFactory;

  private CacheConfig config;

  public CacheManagerServiceImpl() {
    caches = new ConcurrentHashMap<>();
    resolverCache = new LruCacheWithTimeout<>(2, 12 * 60 * 60 * 100);
  }

  /**
   * Activates the cache manager service.
   */
  @Activate
  @Modified
  public void activate(CacheConfig config) {
    this.config = config;
    if (!config.enabled()) {
      invalidateCache();
      closeAndClearCachedResolvers();
    }
    setUpRegularCacheClean();
    LOGGER.debug("config: enabled:{}, expire:{}, user expire:{}, uuid:{}, user max:{}, "
            + "refresh:{}, menu size {}", config.enabled(), config.expireDuration(),
        config.jcrUserExpireDuration(), config.maxUuid(), config.maxJcrUser(),
        config.refreshDuration(), config.maxMenuSize());
  }

  /**
   * Deactivates the cache manager service.
   */
  @Deactivate
  public void deactivate() throws CacheException {
    invalidateCache();
    closeAndClearCachedResolvers();
    if (null != cleanCacheHandle && !cleanCacheHandle.isDone() && !cleanCacheHandle.isCancelled()) {
      cleanCacheHandle.cancel(true);
      cleanCacheHandle = null;
    }

    if (scheduler != null) {
      scheduler.shutdown();
      try {
        if (scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
          LOGGER.info("Cache clean scheduler is correctly closed.");
        }
      } catch (InterruptedException e) {
        throw new CacheException(e.getMessage());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <V> V get(String cacheBucketName, String key, ValueCallback<V> callback) {
    if (!this.config.enabled()) {
      return callback == null ? null : callback.getValue(key);
    }
    try {
      CacheBucketName innerName = getInnerCacheName(cacheBucketName);
      LoadingCache<String, V> cache = getCache(innerName, key, callback);
      if (cache != null) {
        return cache.get(key);
      }
    } catch (CacheException | ExecutionException e) {
      LOGGER.error(String.format(
          "Can't get value from cache for cache key: %s in cache name: %s, error: %s",
          key, cacheBucketName, e.getMessage()
      ));
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <V> boolean isPresent(String cacheBucketName, String key) {
    try {
      CacheBucketName innerName = getInnerCacheName(cacheBucketName);
      LoadingCache<String, V> cache = getCache(innerName, key, null);
      return Objects.requireNonNull(cache).asMap().containsKey(key);
    } catch (CacheException e) {
      LOGGER.error(String.format(
          "Can't get cache for cache key: %s in cache name: %s, error: %s",
          key, cacheBucketName, e.getMessage()));
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invalidateCache(String cacheBucketName, String key) {
    if (!this.config.enabled()) {
      return;
    }
    if (cacheBucketName == null) {
      // clear all
      if (caches.isEmpty()) {
        return;
      }
      for (String cacheKey : caches.keySet()) {
        LoadingCache cache = caches.get(cacheKey);
        cache.invalidateAll();
        cache.cleanUp();
      }
      caches.clear();
    } else if (!StringUtils.isEmpty(cacheBucketName)) {
      LoadingCache cache = caches.get(getInnerCacheName(cacheBucketName).name());
      if (cache == null) {
        LOGGER.debug("There are some problems if this get hit, contact community admin.");
        return;
      }
      if (StringUtils.isEmpty(key)) {
        // Clear cache with cache name
        cache.invalidateAll();
        cache.cleanUp();
        return;
      }
      // Clear cache for specific key in the cache
      cache.invalidate(key);
      cache.cleanUp();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invalidateCache(String cacheBucketName) {
    invalidateCache(cacheBucketName, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invalidateCache() {
    invalidateCache(null, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceResolver getServiceResolver(String serviceUser) throws CacheException {
    ResourceResolver resolver = config.enabled() ? resolverCache.get(serviceUser) : null;
    if (resolver == null || !resolver.isLive()) {
      try {
        resolver = ResolverUtil.newResolver(this.resourceResolverFactory, serviceUser);
      } catch (LoginException e) {
        throw new CacheException("Failed to create Resolver in CacheManagerImpl");
      }
      if (config.enabled()) {
        resolverCache.put(serviceUser, resolver);
      }
    }

    return resolver;
  }

  private <V> LoadingCache<String, V> getCache(CacheBucketName innerCacheName, String key,
                                               ValueCallback<V> callback) throws CacheException {
    try {
      LoadingCache<String, V> cache = caches.get(innerCacheName.name());
      if (cache != null) {
        return cache;
      }
      if (key == null) {
        return null;
      }

      CacheBuilder builder = CacheBuilder.newBuilder();
      if (innerCacheName == CacheBucketName.UUID_VALUE) {
        builder.maximumSize(config.maxUuid());
      } else if (innerCacheName == CacheBucketName.JCR_USER) {
        builder.maximumSize(config.maxJcrUser())
            .expireAfterAccess(config.jcrUserExpireDuration(), TimeUnit.SECONDS);
      } else if (innerCacheName == CacheBucketName.SF_USER_GROUP) {
        builder.maximumSize(config.maxUserGroup())
            .expireAfterAccess(config.expireDuration(), TimeUnit.SECONDS)
            .refreshAfterWrite(config.refreshDuration(), TimeUnit.SECONDS);
      } else if (innerCacheName == CacheBucketName.SF_MENU) {
        builder.maximumSize(config.maxMenuSize())
            .weakValues()
            .expireAfterAccess(config.expireDuration(), TimeUnit.SECONDS)
            .refreshAfterWrite(config.refreshDuration(), TimeUnit.SECONDS);
      } else {
        builder.maximumSize(config.maxSize())
            .expireAfterAccess(config.expireDuration(), TimeUnit.SECONDS)
            .refreshAfterWrite(config.refreshDuration(), TimeUnit.SECONDS);
      }
      cache = builder.build(new CacheLoader<String, V>() {
        public V load(String key) throws CacheException {
          V ret = null;
          if (callback != null) {
            LOGGER.debug("Enter callback method to call API to get value for: " + key);
            ret = callback.getValue(key);
          }
          if (ret == null) {
            throw new CacheException("The returned value is null");
          }
          LOGGER.debug("Return value from load(..) method for cache key: " + key);
          return ret;
        }

        public ListenableFuture<V> reload(final String key, V preVal) throws CacheException {
          LOGGER.debug(java.lang.String.format("reload value for key %s happens", key));
          ListenableFuture<V> ret = null;
          if (callback != null) {
            LOGGER.debug("Enter callback method to call API to reload value again for: " + key);
            ret = Futures.immediateFuture(callback.getValue(key));
          }

          if (ret == null) {
            throw new CacheException("The reload value is null");
          }
          LOGGER.debug("Return value from reload(..) method for key: " + key);
          return ret;
        }
      });

      caches.put(innerCacheName.name(), cache);
      return cache;
    } catch (IllegalArgumentException e) {
      throw new CacheException("Can't create or retrieve cache from the cache store");
    }
  }

  private CacheBucketName getInnerCacheName(String cacheName) {
    CacheBucketName innerCacheName;
    try {
      innerCacheName = CacheBucketName.valueOf(cacheName);
    } catch (IllegalArgumentException e) {
      innerCacheName = CacheBucketName.OBJECT_VALUE;
    }

    return innerCacheName;
  }

  private void closeAndClearCachedResolvers() {
    for (String key : resolverCache.keySet()) {
      ResourceResolver resolver = resolverCache.get(key);
      if (resolver.isLive()) {
        resolver.close();
      }
    }
    resolverCache.clear();
  }

  private void setUpRegularCacheClean() {
    if (this.config.enabled()) {
      final Runnable cleanCache = () -> {
        invalidateCache();
        closeAndClearCachedResolvers();
      };

      cleanCacheHandle = scheduler.scheduleAtFixedRate(
          cleanCache, 10, config.cacheCleanPeriod(), TimeUnit.SECONDS);
    }
  }
}
