package com.workday.community.aem.core.services.cache.impl;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

public class CacheHelper {
  private static CacheManager cacheManager;
   public synchronized static CacheManager getCacheManager() {
     if (cacheManager == null) {
       cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
       cacheManager.init();
     }
    return cacheManager;
  }
}
