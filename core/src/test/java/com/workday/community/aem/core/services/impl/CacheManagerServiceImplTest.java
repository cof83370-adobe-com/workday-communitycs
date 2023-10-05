package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.utils.cache.ValueCallback;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for CacheManagerServiceImpl.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CacheManagerServiceImplTest {
  private static final String TEST_CACHE_BUCKET = "test-cache-bucket";
  private static final String TEST_KEY = "test-key";
  private static final String NON_EXISTING_KEY = "not-existing-key";
  private static final String TEST_VALUE = "test-value";

  CacheConfig cacheConfig;

  @BeforeEach
  public void setup() {
     cacheConfig = TestUtil.getCacheConfig();
  }

  @Test
  public void testActivate() throws CacheException {
    CacheManagerServiceImpl cacheManager = new CacheManagerServiceImpl();
    cacheManager.activate(cacheConfig);
  }

  @Test
  public void testDeActive() throws CacheException {
    CacheManagerServiceImpl cacheManager = new CacheManagerServiceImpl();
    cacheManager.activate(cacheConfig);
    cacheManager.get(TEST_CACHE_BUCKET, TEST_KEY, (key) -> TEST_VALUE);
    cacheManager.deactivate();
  }

  @Test
  public void testGet() throws CacheException {
    CacheManagerServiceImpl cacheManager = new CacheManagerServiceImpl();
    cacheManager.activate(cacheConfig);
    ValueCallback<String> callback = new ValueCallback() {
      int count = 0;
      @Override
      public Object getValue(String key) {
        count++;
        return TEST_VALUE + count;
      }
    };

    String res = cacheManager.get(TEST_CACHE_BUCKET, TEST_KEY, callback);
    assertEquals(res, TEST_VALUE + 1);
    // Second call will not go through the call back.
    String res1 = cacheManager.get(TEST_CACHE_BUCKET, TEST_KEY, callback);
    assertEquals(res1, TEST_VALUE + 1);
  }

  @Test
  public void testClearCache() throws CacheException {
    CacheManagerServiceImpl cacheManager = new CacheManagerServiceImpl();
    cacheManager.activate(cacheConfig);
    ValueCallback<String> callback = new ValueCallback() {
      int count = 0;
      @Override
      public Object getValue(String key) {
        count++;
        return TEST_VALUE + count;
      }
    };

    String res = cacheManager.get(TEST_CACHE_BUCKET, TEST_KEY, callback);
    assertEquals(res, TEST_VALUE + 1);
    cacheManager.invalidateCache(TEST_CACHE_BUCKET, TEST_KEY);
    // Since cache is cleared, fetch again will hit callback and count will be 2.
    String res1 = cacheManager.get(TEST_CACHE_BUCKET, TEST_KEY, callback);
    assertEquals(res1, TEST_VALUE + 2);
    cacheManager.invalidateCache(TEST_CACHE_BUCKET);
    // Since cache is cleared, fetch again will hit callback and count will be 3.
    res1 = cacheManager.get(TEST_CACHE_BUCKET, TEST_KEY, callback);
    assertEquals(res1, TEST_VALUE + 3);
  }

  @Test
  public void testIfCacheExists() throws CacheException {
    CacheManagerServiceImpl cacheManager = new CacheManagerServiceImpl();
    cacheManager.activate(cacheConfig);
    ValueCallback<String> callback = (ValueCallback) key -> TEST_VALUE;

    cacheManager.get(TEST_CACHE_BUCKET, TEST_KEY, callback);
    boolean existed = cacheManager.isPresent(TEST_CACHE_BUCKET, TEST_KEY);
    assertTrue(existed);
    existed = cacheManager.isPresent(TEST_CACHE_BUCKET, NON_EXISTING_KEY);
    assertFalse(existed);
  }
}
