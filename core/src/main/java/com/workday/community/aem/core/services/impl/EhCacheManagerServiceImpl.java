package com.workday.community.aem.core.services.impl;

import com.adobe.xfa.ut.StringUtils;
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
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.workday.community.aem.core.constants.GlobalConstants.CLOUD_CONFIG_NULL_VALUE;
import static com.workday.community.aem.core.services.cache.CacheBucketName.mapValueTypes;

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
  private final String INTERNAL_READ_SERVICE_RESOLVER_CACHE_KEY = "_service-resolver_";

  private CacheManager cacheManager;

  private EhCacheConfig config;
  final Map<String, Cache> caches = new HashMap<>();

  /** The resource resolver factory. */
  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Activate
  @Modified
  public void activate(EhCacheConfig config){
    this.config = config;
    if (cacheManager == null) {
      CacheManagerBuilder<CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder();
      String storagePath = config.storagePath();
      if (!StringUtils.isEmpty(storagePath) && !storagePath.equals(CLOUD_CONFIG_NULL_VALUE)) {
        builder.with(CacheManagerBuilder.persistence(new File(storagePath, "myData")));
      }
      this.cacheManager = builder.build(true);
    }
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
        String name = cacheKey;
        Cache cache = caches.get(name);
        cache.clear();
        this.cacheManager.removeCache(name);
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
  public ResourceResolver getServiceResolver(String serviceUser) throws CacheException {
    // No expiration of resolver.
    Cache<String, ResourceResolver> cache = getCache(CacheBucketName.GENERIC, INTERNAL_READ_SERVICE_RESOLVER_CACHE_KEY);
    ResourceResolver resolver = cache.get(INTERNAL_READ_SERVICE_RESOLVER_CACHE_KEY);
    if (resolver == null) {
      try {
        resolver = ResolverUtil.newResolver(this.resourceResolverFactory, serviceUser);
      } catch (LoginException e) {
        throw new CacheException("Failed to create Resolver in EhCacheManagerImpl");
      }
      cache.put(INTERNAL_READ_SERVICE_RESOLVER_CACHE_KEY, resolver);
    }

    return resolver;
  }

  // ================== Private methods ===============//
  private <V> Cache<String, V> getCache(CacheBucketName innerCacheName, String key) throws CacheException  {
    try {
      Cache<String, V> cache = caches.get(innerCacheName.name());
      if (cache != null) return cache;
      if (key == null) return null;

      ResourcePoolsBuilder poolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
          .heap(this.config.heapSize(), EntryUnit.ENTRIES)
          .offheap(this.config.offHeapSize(), MemoryUnit.MB);

      String storagePath = config.storagePath();
      if (!StringUtils.isEmpty(storagePath) &&
          !storagePath.equals(CLOUD_CONFIG_NULL_VALUE) && this.config.diskSize() > 0) {
        poolsBuilder.disk(this.config.diskSize(), MemoryUnit.MB, true);
      }


      CacheConfigurationBuilder<? extends Object, ? extends Object> builder =
          CacheConfigurationBuilder.newCacheConfigurationBuilder(
              String.class, mapValueTypes.get(innerCacheName), poolsBuilder
          );

      int duration = config.duration();
      // Resolver no need to expire in cache once it is created.
      if (duration == -1 || key.equals(INTERNAL_READ_SERVICE_RESOLVER_CACHE_KEY) ) {
        builder.withExpiry(ExpiryPolicy.NO_EXPIRY);
      } else if (duration > 0) {
        builder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(duration)));
      }

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
      innerCacheName = CacheBucketName.GENERIC;
    }

    return innerCacheName;
  }

  @Deactivate
  public void deactivate() {
    if (cacheManager != null) {
      cacheManager.close();
    }
  }
}
