package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Cache configuration", description = "Parameters for Cache")
public @interface CacheConfig {
  @AttributeDefinition(
      name = "Cache maximum size",
      description = "Cache maximum size",
      type = AttributeType.INTEGER
  )
  int maxSize() default 5000;

  @AttributeDefinition(
      name = "UUID maximum cache size",
      description = "UUID maximum cache size",
      type = AttributeType.INTEGER
  )
  int maxUUID() default 300000; // about 4.6MB

  @AttributeDefinition(
      name = "Cache maximum JCR user size",
      description = "Cache maximum JCR user size",
      type = AttributeType.INTEGER
  )
  int maxJcrUser() default 2000;

  @AttributeDefinition(
      name = "Cache duration in seconds before expire",
      description = "Cache duration in seconds before expire (default 2 hours)",
      type = AttributeType.INTEGER
  )
  int expireDuration() default 2 * 60 * 60;

  @AttributeDefinition(
      name = "JCR user cache duration in seconds before expire",
      description = "Cache duration in seconds before expire (default 2 hours)",
      type = AttributeType.INTEGER
  )
  int jcrUserExpireDuration() default 30 * 60;

  @AttributeDefinition(
      name = "Cache refresh duration in seconds ",
      description = "Cache duration in seconds (default 1 hours)",
      type = AttributeType.INTEGER
  )
  int refreshDuration() default 60 * 60;

  @AttributeDefinition(
      name = "Cache refresh duration in seconds ",
      description = "Cache duration in seconds (default 1 hours)",
      type = AttributeType.INTEGER
  )
  int jcrUserRefreshDuration() default 15 * 60;

  @AttributeDefinition(
      name = "Enable cache",
      description = "Indicate if the cache is enabled (default TRUE)",
      type = AttributeType.BOOLEAN
  )
  boolean enabled() default true;
}
