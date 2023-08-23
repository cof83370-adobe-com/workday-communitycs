package com.workday.community.aem.core.utils.cache;

public interface ValueCallback<String, V>  {
  V getValue(String key);
}
