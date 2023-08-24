package com.workday.community.aem.core.services.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.utils.cache.ValueCallback;
import com.workday.community.aem.core.utils.cache.LRUCacheWithTimeout;
import com.workday.community.aem.core.utils.ResolverUtil;
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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The CacheManagerService implementation class.
 */
@Component(service = CacheManagerService.class, property = {
    "service.pid=aem.core.services.cache.serviceCache"
}, configurationPid = "com.workday.community.aem.core.config.CacheConfig",
    configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = CacheConfig.class)
public class CacheManagerServiceImpl implements CacheManagerService {
  private final static Logger LOGGER = LoggerFactory.getLogger(CacheManagerServiceImpl.class);

  private final LRUCacheWithTimeout<String, ResourceResolver> resolverCache = new LRUCacheWithTimeout<>(2, 12 * 60 * 60 * 100);
  private Map<String, LoadingCache> caches;

  /** The resource resolver factory. */
  @Reference
  private ResourceResolverFactory resourceResolverFactory;
  private CacheBuilder builder;
  private CacheConfig config;

  public CacheManagerServiceImpl() {
    caches = new ConcurrentHashMap<>();
  }

  // This method is mainly for testing purpose.
  public void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
    this.resourceResolverFactory = resourceResolverFactory;
  }

  @Activate
  @Modified
  public void activate(CacheConfig config) throws CacheException{
    if(builder == null) {
      this.config = config;
      builder = CacheBuilder.newBuilder()
          .maximumSize(config.maxSize())
          .expireAfterAccess(config.expireDuration(), TimeUnit.SECONDS)
          .refreshAfterWrite(config.refreshDuration(), TimeUnit.SECONDS);
    }
  }

  @Deactivate
  public void deactivate() {
    ClearAllCaches();
    closeAndClearCachedResolvers();
  }

  @Override
  public <V> V get(String cacheName, String key, ValueCallback<String, V> callback) {
    if (!this.config.enabled()) {
      return callback.getValue(key);
    }
    try {
      CacheBucketName innerName = getInnerCacheName(cacheName);
      LoadingCache<String, V> cache = getCache(innerName, key, callback);
      if (cache != null) {
        return cache.get(key);
      }
    } catch (CacheException | ExecutionException e) {
      LOGGER.error(String.format(
          "Can't get value from cache for cache key: %s in cache name: %s, error: %s",
          key, cacheName, e.getMessage()
      ));
    }

    return null;
  }

  @Override
  public <V> boolean isPresented(String cacheName, String key) {
    try {
      CacheBucketName innerName = getInnerCacheName(cacheName);
      LoadingCache<String, V> cache = getCache(innerName, key, null);
      return Objects.requireNonNull(cache).asMap().containsKey(key);
    } catch (CacheException e) {
      LOGGER.error(String.format(
          "Can't get cache for cache key: %s in cache name: %s, error: %s",
          key, cacheName, e.getMessage()));
       return false;
    }
  }

  @Override
  public void ClearAllCaches(String cacheName, String key)  {
    if (cacheName == null) {
      // clear all
      if (caches.isEmpty()) return;
      for (String cacheKey : caches.keySet()) {
        LoadingCache cache = caches.get(cacheKey);
        cache.invalidateAll();
      }
      caches.clear();
    }
  }

  @Override
  public void ClearAllCaches(String cacheName) {
    ClearAllCaches(cacheName, null);
  }

  @Override
  public void ClearAllCaches() {
    ClearAllCaches(null, null);
  }

  // ====== Convenient Utility APIs ====== //
  @Override
  public ResourceResolver getServiceResolver(String serviceUser) throws CacheException {
    // No expiration of resolver.
    ResourceResolver resolver = resolverCache.get(serviceUser);
    if (resolver == null) {
      try {
        resolver = ResolverUtil.newResolver(this.resourceResolverFactory, serviceUser);
      } catch (LoginException e) {
        throw new CacheException("Failed to create Resolver in CacheManagerImpl");
      }
      resolverCache.put(serviceUser, resolver);
    }

    return resolver;
  }

  // ================== Private methods ===============//
  private <V> LoadingCache<String, V> getCache(CacheBucketName innerCacheName, String key,
                                               ValueCallback<String, V> callback) throws CacheException  {
    try {
      LoadingCache<String, V> cache = caches.get(innerCacheName.name());
      if (cache != null) return cache;
      if (key == null) return null;

      cache = builder.build( new CacheLoader<String, V>() {
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
    for (String key :resolverCache.keySet()) {
       ResourceResolver resolver = resolverCache.get(key);
      if (resolver.isLive()) {
        resolver.close();
      }
    }
    resolverCache.clear();
  }
}
