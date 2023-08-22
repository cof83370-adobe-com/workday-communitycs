package com.workday.community.aem.core.services;

import java.util.Map;

/**
 * enum for pre-defined cache buckets. We only support cache in pre-defined cache bucket inside.
 * When we need to introduce new cache bucket, we should
 *   1: Add a new internal cache bucket name as the existing GENERIC and STRING_VALUE.
 *   2: Put it in the mapValueTypes to define data type for values stored in the cache bucket.
 */
public enum CacheBucketName {
  OBJECT_VALUE("object_value_cache", "Cache bucket name for cached object"),
  STRING_VALUE("string_value_cache", "Cache bucket for string value");

  public final static Map<CacheBucketName, Class> mapValueTypes = Map.of(
      CacheBucketName.OBJECT_VALUE, Object.class,
      CacheBucketName.STRING_VALUE, Byte.class
  );

  final String label;
  final String name;

  CacheBucketName(String name, String label) {
    this.name = name;
    this.label = label;
  }
}
