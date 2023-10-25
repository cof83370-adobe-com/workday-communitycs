package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The cache configuration interface.
 */
@ObjectClassDefinition(name = "Cache configuration", description = "Parameters for Cache")
public @interface CacheConfig {

  /**
   * The cache max size.
   *
   * @return The max size.
   */
  @AttributeDefinition(
      name = "Cache maximum size",
      description = "Cache maximum size",
      type = AttributeType.INTEGER
  )
  int maxSize() default 5000;

  /**
   * The UUID cache max size, defaults to 300,000 (or about 4.6MB.)
   *
   * @return The max size.
   */
  @AttributeDefinition(
      name = "UUID maximum cache size",
      description = "UUID maximum cache size",
      type = AttributeType.INTEGER
  )
  int maxUuid() default 300000;

  /**
   * The menu cache max size, defaults to 1000 (or about 16 MB.)
   *
   * @return The max size.
   */
  @AttributeDefinition(
      name = "Cache maximum size",
      description = "Cache maximum size (recommend less than 1000)",
      type = AttributeType.INTEGER
  )
  int maxMenuSize() default 1000;

  /**
   * The JCR user cache max size.
   *
   * @return The max size.
   */
  @AttributeDefinition(
      name = "Cache maximum JCR user size",
      description = "Cache maximum JCR user size",
      type = AttributeType.INTEGER
  )
  int maxJcrUser() default 2000;

  /**
   * The user group cache max size.
   *
   * @return The max size.
   */
  @AttributeDefinition(
      name = "Maximum user group cache size",
      description = "Maximum user group cache size",
      type = AttributeType.INTEGER
  )
  int maxUserGroup() default 5000;

  /**
   * The time in seconds before the cache is invalidated.
   *
   * @return The length in seconds.
   */
  @AttributeDefinition(
      name = "Cache duration in seconds before expire",
      description = "Cache duration in seconds before expire (default 2 hours)",
      type = AttributeType.INTEGER
  )
  int expireDuration() default 2 * 60 * 60;

  /**
   * The time in seconds before the JCR user cache is invalidated.
   *
   * @return The length in seconds.
   */
  @AttributeDefinition(
      name = "JCR user cache duration in seconds before expire",
      description = "JCR user cache duration in seconds before expire (default 30 minutes)",
      type = AttributeType.INTEGER
  )
  int jcrUserExpireDuration() default 30 * 60;

  /**
   * The time in seconds before the cache cleaned.
   *
   * @return The length in seconds.
   */
  @AttributeDefinition(
      name = "Cache maintenance period in regular cleanup",
      description = "Cache maintenance period in regular cleanup (default 1 day)",
      type = AttributeType.INTEGER
  )
  int cacheCleanPeriod() default 24 * 60 * 60;

  /**
   * Whether the cache is enabled.
   *
   * @return True if enabled, otherwise false.
   */
  @AttributeDefinition(
      name = "Enable cache",
      description = "Indicate if the cache is enabled (default TRUE)",
      type = AttributeType.BOOLEAN
  )
  boolean enabled() default true;

}
