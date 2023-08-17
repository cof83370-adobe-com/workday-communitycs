package com.workday.community.aem.core.services.cache;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * enum for pre-defined cache bucket. Each cache bucket caches a specific type of data
 *
 * When we need to introduce new cache bucket, we should
 *   1: Add a new internal cache bucket name as RESOLVER, GENERIC here
 *   2: Put it in the mapValueTypes to defined the type of data that will be stored in this cache bucket.
 */
public enum CacheBucketName {
  RESOLVER("resolver", "resolver cache bucket name"),
  GENERIC("generic", "generic cache bucket name");

  final public static Map<CacheBucketName, Class> mapValueTypes = new HashMap<>();

  static {
    mapValueTypes.put(CacheBucketName.RESOLVER, ResourceResolver.class);
    mapValueTypes.put(CacheBucketName.GENERIC, Object.class);
  }

  final String label;
  final String name;

  private CacheBucketName(String name, String label) {
    this.name = name;
    this.label = label;
  }
}
