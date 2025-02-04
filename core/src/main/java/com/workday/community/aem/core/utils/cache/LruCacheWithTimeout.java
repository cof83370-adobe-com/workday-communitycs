package com.workday.community.aem.core.utils.cache;

import java.util.Map;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * The LRUCache Map class with timeout support.
 *
 * @param <K> The key in the map.
 * @param <V> The value in the map.
 */
public class LruCacheWithTimeout<K, V> extends LRUMap<K, V> {

  private final long timeoutMs;

  private final Map<String, Long> keyTimeStamps = new HashedMap<>();

  /**
   * Constructor.
   *
   * @param maxCapacity The cache capacity number.
   * @param timeoutMs   Cache timeout setting.
   */
  public LruCacheWithTimeout(int maxCapacity, long timeoutMs) {
    super(maxCapacity);
    this.timeoutMs = timeoutMs;
  }

  @Override
  public V get(Object key, boolean updateToMru) {
    if (key == null) {
      return null;
    }

    V value = super.get(key, updateToMru);
    String ks = key.toString();
    Long keyTimeStamp = this.keyTimeStamps.get(ks);
    if (value != null && keyTimeStamp != null && isExpired(keyTimeStamp)) {
      // If the retrieved item has expired, remove it from the cache and return null.
      if (value instanceof ResourceResolver && ((ResourceResolver) value).isLive()) {
        ((ResourceResolver) value).close();
      }
      remove(key);
      this.keyTimeStamps.remove(ks);
      return null;
    }

    return value;
  }

  @Override
  protected void addMapping(int hashIndex, int hashCode, K key, V value) {
    super.addMapping(hashIndex, hashCode, key, value);
    if (key != null) {
      keyTimeStamps.put(key.toString(), System.currentTimeMillis());
    }
  }

  private boolean isExpired(long keyTimeStamp) {
    // Implement logic to check if the item has expired based on the timeout duration.
    long currentTime = System.currentTimeMillis();
    return (currentTime - keyTimeStamp) > timeoutMs;
  }
}
