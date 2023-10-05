package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Lms service configuration interface.
 */
@ObjectClassDefinition(name = "Lms API Config", description = "LmsService OSGi Config Vaues")
public @interface LmsConfig {

  /**
   * Returns the URL to the LMS.
   *
   * @return The URL.
   */
  @AttributeDefinition(
      name = "Lms API Url",
      description = "Lms API Url.",
      type = AttributeType.STRING
  )
  String lmsUrl();

  /**
   * Returns the path to the LMS token.
   *
   * @return The path.
   */
  @AttributeDefinition(
      name = "Lms Token API Path",
      description = "Lms Token API Path.",
      type = AttributeType.STRING
  )
  String lmsTokenPath();

  /**
   * Returns the path to the LMS course details.
   *
   * @return The path.
   */
  @AttributeDefinition(
      name = "Lms Course Detail API Path",
      description = "Lms Course Detail API Path.",
      type = AttributeType.STRING
  )
  String lmsCourseDetailPath();

  /**
   * Returns the LMS client ID.
   *
   * @return The client ID.
   */
  @AttributeDefinition(
      name = "Lms API Client Id",
      description = "Lms API Client Secret.",
      type = AttributeType.STRING
  )
  String lmsAPIClientId();

  /**
   * Returns the LMS client secret.
   *
   * @return The client secret.
   */
  @AttributeDefinition(
      name = "Lms API Client Secret",
      description = "Lms API Client Secret.",
      type = AttributeType.STRING
  )
  String lmsAPIClientSecret();

  /**
   * Returns the LMS API refresh token.
   *
   * @return The refresh token.
   */
  @AttributeDefinition(
      name = "Lms API Refresh Token",
      description = "Lms API Refresh Token.",
      type = AttributeType.STRING
  )
  String lmsAPIRefreshToken();

  /**
   * Returns the LMS token cache size (defaults to 10.)
   *
   * @return The client ID.
   */
  @AttributeDefinition(
      name = "Lms token cache size",
      description = "Lms Token Cache size",
      type = AttributeType.INTEGER
  )
  int lmsTokenCacheMax() default 10;

  /**
   * Returns the LMS token cache timeout in ms (defaults to 59 minutes.)
   *
   * @return The timeout in ms.
   */
  @AttributeDefinition(
      name = "Lms token cache timeout duration",
      description = "Lms Token Cache Timeout Duration (mills)",
      type = AttributeType.LONG
  )
  long lmsTokenCacheTimeout() default 3540000L; // Default to 59 minutes.

}
