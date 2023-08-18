package com.workday.community.aem.core.services.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * enum for pre-defined cache bucket. Each cache bucket caches a specific type of data
 * When we need to introduce new cache bucket, we should
 *   1: Add a new internal cache bucket name as RESOLVER, GENERIC here
 *   2: Put it in the mapValueTypes to defined the type of data that will be stored in this cache bucket.
 */
public enum CacheBucketName {
  GENERIC("generic_object", "Cache bucket name for object value"),
  STRINGVALUE("string_value", "Cache bucket for string value");

  final public static Map<CacheBucketName, Class> mapValueTypes = new HashMap<>();

  static {
    mapValueTypes.put(CacheBucketName.GENERIC, Object.class);
    mapValueTypes.put(CacheBucketName.STRINGVALUE, Object.class);
  }

  final String label;
  final String name;

  CacheBucketName(String name, String label) {
    this.name = name;
    this.label = label;
  }
}
