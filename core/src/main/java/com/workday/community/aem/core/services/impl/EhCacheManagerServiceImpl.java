package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.EhCacheConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.EhCacheManager;
import com.workday.community.aem.core.utils.LRUCacheWithTimeout;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * The EhCacheManagerService implementation class.
 */
@Component(service = EhCacheManager.class, property = {
    "service.pid=aem.core.services.cache.ehcache"
}, configurationPid = "com.workday.community.aem.core.config.EhCacheConfig",
    configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = EhCacheConfig.class)
public class EhCacheManagerServiceImpl implements EhCacheManager {
  private final static Logger LOGGER = LoggerFactory.getLogger(EhCacheManagerServiceImpl.class);
  private CacheManager cacheManager;
  private EhCacheConfig config;
  private final LRUCacheWithTimeout<String, ResourceResolver> resolverCache = new LRUCacheWithTimeout<>(2, 12 * 60 * 60 * 100);
  final Map<String, Cache> caches = new HashMap<>();

  /** The resource resolver factory. */
  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Activate
  @Modified
  public void activate(EhCacheConfig config) throws CacheException{
    this.config = config;
    if (cacheManager == null) {
      CacheManagerBuilder<CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder();
      this.cacheManager = builder.build(true);
    }
  }

  @Deactivate
  public void deactivate() {
    if (cacheManager != null) {
      ClearAllCaches();
      cacheManager.close();
    }

    closeAndClearCachedResolvers();
  }

  @Override
  public <V> V get(String cacheName, String key) {
    try {
      CacheBucketName innerName = getInnerCacheName(cacheName);
      Cache<String, V> cache = getCache(innerName, key);
      if (cache != null) {
        return cache.get(key);
      }
    } catch (CacheException e) {
      LOGGER.error(String.format(
          "Can't get value from cache for cache key: %s in cache name: %s, error: %s",
          key, cacheName, e.getMessage()
      ));
    }

    return null;
  }

  @Override
  public <V> void put(String cacheName, String key, V value) {
    try {
      CacheBucketName innerName = getInnerCacheName(cacheName);
      Cache<String, V> cache = getCache(innerName, key);
      if (cache != null) {
        cache.put(key, value);
      }
    } catch (CacheException e) {
      LOGGER.error(String.format(
          "Can't put value into cache for cache key: %s with value: %s in cache name: %s, error: %s",
          key, value, cacheName, e.getMessage()
      ));
    }
  }

  @Override
  public void ClearAllCaches(String cacheName, String key)  {
    if (cacheName == null) {
      // clear all
      if (caches.isEmpty()) return;
      for (String cacheKey : caches.keySet()) {
        Cache cache = caches.get(cacheKey);
        cache.clear();
        this.cacheManager.removeCache(cacheKey);
      }

      caches.clear();
      return;
    }

    CacheBucketName innerName = getInnerCacheName(cacheName);
    Cache cache = null;
    try {
      cache = getCache(innerName, key);
    } catch (CacheException e) {
     LOGGER.error("Retrieve cache fails");
    }

    if (cache != null) {
      cache.clear();
      this.cacheManager.removeCache(cacheName);
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
  synchronized public ResourceResolver getServiceResolver(String serviceUser) throws CacheException {
    // No expiration of resolver.
    ResourceResolver resolver = resolverCache.get(serviceUser);
    if (resolver == null) {
      try {
        resolver = ResolverUtil.newResolver(this.resourceResolverFactory, serviceUser);
      } catch (LoginException e) {
        throw new CacheException("Failed to create Resolver in EhCacheManagerImpl");
      }
      resolverCache.put(serviceUser, resolver);
    }

    return resolver;
  }

  // ================== Private methods ===============//
  private <V> Cache<String, V> getCache(CacheBucketName innerCacheName, String key) throws CacheException  {
    try {
      Cache<String, V> cache = caches.get(innerCacheName.name());
      if (cache != null) return cache;
      if (key == null) return null;

      ResourcePoolsBuilder poolsBuilder;
      poolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
          .heap(this.config.heapSize(), EntryUnit.ENTRIES);

     CacheConfigurationBuilder<?, ?> builder =
          CacheConfigurationBuilder.newCacheConfigurationBuilder(
              String.class, CacheBucketName.mapValueTypes.get(innerCacheName), poolsBuilder
          );

      int regularDuration = config.duration();
      builder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(regularDuration)));

      cache = (Cache<String, V>) cacheManager.createCache(innerCacheName.name(), builder);
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
    } catch (NullPointerException | IllegalArgumentException e) {
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
