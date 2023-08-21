package com.workday.community.aem.core.services;

import java.util.Map;

/**
 * enum for pre-defined cache buckets. We only support cache in pre-defined cache bucket inside.
 * When we need to introduce new cache bucket, we should
 *   1: Add a new internal cache bucket name as the existing GENERIC and STRING_VALUE.
 *   2: Put it in the mapValueTypes to define data type for values stored in the cache bucket.
 */
public enum CacheBucketName {
  GENERIC("generic_object", "Cache bucket name for object value"),
  STRING_VALUE("string_value", "Cache bucket for string value"),
  LONG_VALUE("long_value", "Cache bucket for long value"),
  BYTES_VALUE("bytes_value", "Cache bucket for value as bytes");
  public final static Map<CacheBucketName, Class> mapValueTypes = Map.of(
      CacheBucketName.GENERIC,
      Object.class, CacheBucketName.STRING_VALUE,
      String.class, CacheBucketName.LONG_VALUE,
      Long.class, CacheBucketName.BYTES_VALUE,
      Byte.class
  );

  final String label;
  final String name;

  CacheBucketName(String name, String label) {
    this.name = name;
    this.label = label;
  }
}
