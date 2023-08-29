package com.workday.community.aem.core.services;

/**
 * enum for pre-defined cache buckets. We only support cache in pre-defined cache bucket inside.
 * When we need to introduce new cache bucket, we should add a new internal cache bucket name as
 * the existing GENERIC and STRING_VALUE.
 */
public enum CacheBucketName {
  OBJECT_VALUE("object_value_cache", "Cache bucket name for cached object"),
  STRING_VALUE("string_value_cache", "Cache bucket for string value"),
  UUID_VALUE("uuid_value_cache", "Cache UUID string value");

  final String label;
  final String name;

  CacheBucketName(String name, String label) {
    this.name = name;
    this.label = label;
  }
}
