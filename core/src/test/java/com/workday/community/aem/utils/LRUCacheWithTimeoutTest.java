package com.workday.community.aem.utils;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.workday.community.aem.core.utils.cache.LRUCacheWithTimeout;
import org.junit.jupiter.api.Test;

public class LRUCacheWithTimeoutTest {
  @Test
  public void TestLRUMap() throws InterruptedException {
    LRUCacheWithTimeout<String, String> map = new LRUCacheWithTimeout<>(10, 100);
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
