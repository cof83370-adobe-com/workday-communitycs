package com.workday.community.aem.core;

import com.workday.community.aem.core.config.CacheConfig;

import java.lang.annotation.Annotation;

public class TestUtil {
  public static CacheConfig getCacheConfig() {
    return new CacheConfig() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return null;
      }

      @Override
      public int maxSize() {
        return 10;
      }

      public int maxUUID() {
        return 0;
      }

      @Override
      public int maxJcrUser() {
        return 0;
      }

      @Override
      public int expireDuration() {
        return 10;
      }

      @Override
      public int jcrUserExpireDuration() {
        return 0;
      }

      @Override
      public int refreshDuration() {
        return 10;
      }

      @Override
      public int jcrUserRefreshDuration() {
        return 0;
      }

      @Override
      public boolean enabled() {
        return true;
      }
    };
  }
}
