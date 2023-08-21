package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.EhCacheConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.EhCacheManager;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for EhCacheManagerServiceImpl.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EhCacheManagerServiceImplTest {
  @Mock
  CacheManager cacheManager;

  @Mock
  EhCacheConfig ehCacheConfig;

  @Mock
  ResourceResolverFactory resourceResolverFactory;

  @InjectMocks
  EhCacheManagerServiceImpl ehCacheManager;

  private static final String TEST_CACHE_BUCKET = "test-cache-bucket";
  private static final String TEST_KEY = "test-key";
  private static final String TEST_VALUE = "test-value";

  @BeforeEach
  public void setup() {
    lenient().when(ehCacheConfig.heapSize()).thenReturn(10);
//    lenient().when(ehCacheConfig.offHeapSize()).thenReturn(1);
  }

  @Test
  public void testActivate() throws CacheException, LoginException {
    EhCacheManager ehCacheManager1 = new EhCacheManagerServiceImpl();
    try(MockedStatic<ResolverUtil> mockResolverUtils = mockStatic(ResolverUtil.class)) {
      ResourceResolver resolver = mock(ResourceResolver.class);
      mockResolverUtils.when(()->ResolverUtil.newResolver(any(), anyString())).thenReturn(resolver);
      ((EhCacheManagerServiceImpl) ehCacheManager1).activate(ehCacheConfig);
    }
  }

  @Test
  public void testDeActive() {
    ehCacheManager.deactivate();
    verify(cacheManager, times(1)).close();
  }

  @Test
  public void testGet() {
    Cache cache = mock(Cache.class);
    lenient().when(cache.get(eq(TEST_KEY))).thenReturn(TEST_VALUE);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);

    String res = ehCacheManager.get(TEST_CACHE_BUCKET, TEST_KEY);
    assertEquals(res, TEST_VALUE);
  }

  @Test
  public void testGetWithStringBucket() {
    Cache cache = mock(Cache.class);
    lenient().when(cache.get(eq(TEST_KEY))).thenReturn(TEST_VALUE);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);

    String res = ehCacheManager.get(CacheBucketName.STRING_VALUE.name(), TEST_KEY);
    assertEquals(res, TEST_VALUE);
  }

  @Test
  public void testGetWithException() {
    Cache cache = mock(Cache.class);
    lenient().when(cache.get(eq(TEST_KEY))).thenReturn(TEST_VALUE);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenThrow(new IllegalArgumentException("test failed"));
    String res = ehCacheManager.get(CacheBucketName.STRING_VALUE.name(), TEST_KEY);
    assertNull(res);
  }

  @Test
  public void testPut() {
    Cache cache = mock(Cache.class);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);
    ehCacheManager.put(TEST_CACHE_BUCKET, TEST_KEY, TEST_VALUE);
    lenient().when(cache.get(eq(TEST_KEY))).thenReturn(TEST_VALUE);
    String res = ehCacheManager.get(TEST_CACHE_BUCKET, TEST_KEY);
    assertEquals(res, TEST_VALUE);
  }

  @Test
  public void testPutWithStringBucket() {
    Cache cache = mock(Cache.class);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);
    ehCacheManager.put(TEST_CACHE_BUCKET, TEST_KEY, TEST_VALUE);
    lenient().when(cache.get(eq(TEST_KEY))).thenReturn(TEST_VALUE);
    String res = ehCacheManager.get(CacheBucketName.STRING_VALUE.name(), TEST_KEY);
    assertEquals(res, TEST_VALUE);
  }

  @Test
  public void testPutWithException() {
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenThrow(new IllegalArgumentException("test failed"));
    ehCacheManager.put(TEST_CACHE_BUCKET, TEST_KEY, TEST_VALUE);
    String val = ehCacheManager.get(TEST_CACHE_BUCKET, TEST_KEY);
    assertNull(val);
  }

  @Test
  public void testClearCache() {
    Cache cache = mock(Cache.class);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);
    ehCacheManager.ClearAllCaches(TEST_CACHE_BUCKET, TEST_CACHE_BUCKET);
    verify(cache, Mockito.times(1)).clear();
  }

  @Test
  public void testGetServiceResolver() throws CacheException, LoginException {
    Cache cache = mock(Cache.class);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);
    ResourceResolver mockResolver = mock(ResourceResolver.class);
    lenient().when(resourceResolverFactory.getServiceResourceResolver(any())).thenReturn(mockResolver);
    ResourceResolver resolver = ehCacheManager.getServiceResolver(READ_SERVICE_USER);
    assertEquals(resolver, mockResolver);
  }

  @Test
  public void testClearAllCache() {
    Cache cache = mock(Cache.class);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);
    testPut();
    ehCacheManager.ClearAllCaches();
    verify(cacheManager, Mockito.times(1)).removeCache(anyString());

    ehCacheManager.ClearAllCaches(TEST_CACHE_BUCKET);
  }

  @Test
  public void testClearAllCacheWithBucketName() {
    Cache cache = mock(Cache.class);
    lenient().when(cacheManager.createCache(anyString(), (CacheConfigurationBuilder) any())).thenReturn(cache);
    testPut();
    ehCacheManager.ClearAllCaches(TEST_CACHE_BUCKET);
    verify(cacheManager, Mockito.times(1)).removeCache(anyString());
  }
}
