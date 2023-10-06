package com.workday.community.aem.utils;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.workday.community.aem.core.utils.cache.LruCacheWithTimeout;
import org.junit.jupiter.api.Test;

public class LruCacheWithTimeoutTest {
  @Test
  public void TestLRUMap() throws InterruptedException {
    LruCacheWithTimeout<String, String> map = new LruCacheWithTimeout<>(10, 100);
    // case 1: key/value pair expired
    map.put("test", "test1");
    Thread.sleep(200);
    String value = map.get("test");
    assertNull(value);

    // case 2: key/value pair not expired
    map.put("test2", "test2");
    Thread.sleep(10);
    String value1 = map.get("test2");
    assertNotNull(value1);
  }

}
