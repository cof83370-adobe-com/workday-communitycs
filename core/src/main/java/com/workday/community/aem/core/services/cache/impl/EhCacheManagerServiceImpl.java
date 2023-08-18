package com.workday.community.aem.core.services.cache.impl;

import com.workday.community.aem.core.config.EhCacheConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.cache.CacheBucketName;
import com.workday.community.aem.core.services.cache.EhCacheManager;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static com.workday.community.aem.core.services.cache.CacheBucketName.mapValueTypes;

@Component(service = EhCacheManager.class, property = {
    "service.pid=aem.core.services.cache.ehcache"
}, configurationPid = "com.workday.community.aem.core.config.EhCacheConfig",
    configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = EhCacheConfig.class)
public class EhCacheManagerServiceImpl implements EhCacheManager {
  private final static Logger LOGGER = LoggerFactory.getLogger(EhCacheManagerServiceImpl.class);
  private static CacheManager cacheManager;

  private EhCacheConfig config;
  final Map<CacheBucketName, Cache> caches = new HashMap<>();

  /** The resource resolver factory. */
  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Activate
  @Modified
  public void activate(EhCacheConfig config){
    this.config = config;
    if (cacheManager == null) {
      cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
      cacheManager.init();
    }
  }

  @Override
  public <V> V get(String cacheName, String key) {
    try {
      CacheBucketName innerName = getInnerCacheName(cacheName);
      Cache<String, V> cache = getCache(innerName, key);
      return cache.get(key);

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
      cache.put(key, value);
    } catch (CacheException e) {
      LOGGER.error(String.format(
          "Can't put value into cache for cache key: %s with value: %s in cache name: %s, error: %s",
          key, value, cacheName, e.getMessage()
      ));
    }
  }

  @Override
  public ResourceResolver getServiceResolver() throws CacheException {
    String serviceCacheKey = "service-resolver";
    Cache<String, ResourceResolver> cache = getCache(CacheBucketName.RESOLVER, serviceCacheKey);
    ResourceResolver resolver = cache.get(serviceCacheKey);
    if (resolver == null) {
      try {
        resolver = ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER);
      } catch (LoginException e) {
        throw new CacheException("Failed to create Resolver in EhCacheManagerImpl");
      }
      cache.put(serviceCacheKey, resolver);
    }

    return resolver;
  }

  @Override
  public <V> void clearCacheBucket(String cacheName, String key) throws CacheException {
    if (cacheName == null) {
      // clear all
      if (caches.isEmpty()) return;
      Iterator iterator = caches.keySet().iterator();
      while(iterator != null && iterator.hasNext()) {
        Cache<String, V> cache = (Cache<String, V>)iterator.next();
        cache.clear();
      }

      caches.clear();
      return;
    }

    CacheBucketName innerName = getInnerCacheName(cacheName);
    Cache<String, V> cache = getCache(innerName, key);
    if (cache != null) {
      cache.clear();
      caches.remove(getInnerCacheName(cacheName));
    }
  }

  @Override
  public <V> void clearCacheBucket(String cacheName) throws CacheException {
    clearCacheBucket(cacheName, null);
  }

  @Override
  public <V> void clearCacheBucket() throws CacheException {
    clearCacheBucket(null, null);
  }

  // ================== Private methods ===============//
  private <V> Cache<String, V> getCache(CacheBucketName innerCacheName, String key) throws CacheException  {
    try {
      Cache<String, V> cache = caches.get(innerCacheName);
      if (cache != null) return cache;
      if (key == null) return null;

      CacheConfigurationBuilder builder =
          CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
              mapValueTypes.get(innerCacheName), ResourcePoolsBuilder.heap(this.config.heapSize()));
      cache = cacheManager.createCache(innerCacheName.name(), builder);
      caches.put(innerCacheName, cache);

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
      innerCacheName = CacheBucketName.GENERIC;
    }

    return innerCacheName;
  }


  @Deactivate
  private void deactivate() {
    if (cacheManager != null) {
      cacheManager.close();
    }
  }
}
