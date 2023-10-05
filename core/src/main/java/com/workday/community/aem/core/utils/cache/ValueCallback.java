package com.workday.community.aem.core.utils.cache;

public interface ValueCallback<V> {
  V getValue(String key);
}
