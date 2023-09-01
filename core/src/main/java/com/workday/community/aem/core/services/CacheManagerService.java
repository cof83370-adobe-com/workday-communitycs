package com.workday.community.aem.core.services;

import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.utils.cache.ValueCallback;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;

/**
 * The Cache manager definition interface.
 */

@ProviderType
public interface CacheManagerService {

  /**
   * Get the cached value from the cache based on pass-in cache bucket number
   * and key. The cache bucket name is predefined in CacheBucketName class
   * (@CacheBucketName), if not match, it will pick up CacheBucketName.OBJECT_VALUE.
   * For the cache key, please follow a naming conversion and introduce
   * proper namespace as part of the key to prevent possible key collision. Also,
   * isPresented() method can be used to check if the cache is already existing.
   *
   * @param cacheBucketName The pass-in cache name.
   * @param key The pass-in cache key.
   * @param callback The callback function to retrieve data for the cached key.
   * @return The value from the cache for the pass-in cache key.
   * @param <V> The type of returned value.
   */
   <V> V get(String cacheBucketName, String key, ValueCallback<V> callback);

  /**
   * To check if a key is already presented in a cache with corresponding cache name.
   * @param cacheBucketName The pass-in cache name.
   * @param key The pass-in cache key.
   * @return true if the key has associated cache in the cache.
   * @param <V> The type of returned value.
   */
  <V> boolean isPresent(String cacheBucketName, String key);

  /**
   * Clear cache for specific cache key in a cache bucket.
   *
   * @param cacheBucketName The cache bucket name.
   * @param key  The cache key.
   */
  void invalidateCache(String cacheBucketName, String key);

  /**
   * Clear all caches in a cache bucket.
   * @param cacheName The cache bucket name.
   */
  void invalidateCache(String cacheName);

  /**
   * Clear all caches
   */
  void invalidateCache() ;

  /**
   * Convenient method to get a service Resource resolver from Cache (create if not existing).
   * @param serviceUser The service username
   * @return the Service resource resolver from Cache.
   */
  ResourceResolver getServiceResolver(String serviceUser) throws CacheException;
}
