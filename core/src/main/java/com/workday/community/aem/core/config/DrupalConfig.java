package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Drupal service configuration interface.
 */
@ObjectClassDefinition(name = "DrupalService Config", description = "Drupal Service OSGi Config Values")
public @interface DrupalConfig {
  /**
   * Gets drupal api url.
   *
   * @return Drupal Api Url
   */
  @AttributeDefinition(name = "Drupal API Url", description = "Drupal API Url.", type = AttributeType.STRING)
  String drupalApiUrl();

  /**
   * Drupal token path.
   *
   * @return Drupal token path
   */
  @AttributeDefinition(name = "Drupal Token API Path", description = "Drupal Token Path.", type = AttributeType.STRING)
  String drupalTokenPath();

  /**
   * Drupal User Data API Path.
   *
   * @return Drupal User Data API Path
   */
  @AttributeDefinition(name = "Drupal User Data API Path", description = "Drupal User Data Path.",
      type = AttributeType.STRING)
  String drupalUserDataPath();

  /**
   * Drupal API Client Id.
   *
   * @return Drupal API Client Id
   */
  @AttributeDefinition(name = "Drupal API User Lookup Client Id", description = "Drupal API User Lookup Client Id.",
      type = AttributeType.STRING)
  String drupalUserLookupClientId();

  /**
   * Drupal API Client Secret.
   *
   * @return Drupal API Client Secret
   */
  @AttributeDefinition(name = "Drupal API User Lookup Client Secret",
      description = "Drupal API User Lookup Client Secret.",
      type = AttributeType.STRING)
  String drupalUserLookupClientSecret();

  /**
   * Drupal API token cache size.
   *
   * @return Drupal API token cache size
   */
  @AttributeDefinition(name = "Drupal API token cache size", description = "Drupal API Token Cache size.",
      type = AttributeType.INTEGER)
  int drupalTokenCacheMax() default 10;

  /**
   * Drupal API token cache timeout duration.
   *
   * @return Drupal API token cache timeout duration
   */
  @AttributeDefinition(name = "Drupal API token cache timeout duration",
      description = "Drupal API Token Cache Timeout Duration (mills).", type = AttributeType.LONG)
  long drupalTokenCacheTimeout() default 1740000L; // Default to 29 minutes.

  /**
   * Enable cache.
   *
   * @return Enable cache
   */
  @AttributeDefinition(name = "Enable cache", description = "Enable cache(default true).", type = AttributeType.BOOLEAN)
  boolean enableCache() default true;

  /**
   * Drupal User Search API Path.
   *
   * @return Drupal User Search API Path
   */
  @AttributeDefinition(name = "Drupal User Search API Path", description = "Drupal User Search API Path.")
  String drupalUserSearchPath();

  /**
   * Drupal CSRF token path.
   *
   * @return Drupal CSRF token path
   */
  @AttributeDefinition(name = "Drupal CSRF Token API Path", description = "Drupal CSRF Token Path.",
      type = AttributeType.STRING)
  String drupalCsrfTokenPath();

  /**
   * Whether AEM - Drupal Content Sync enabled.
   *
   * @return True if enabled, otherwise false.
   */
  @AttributeDefinition(
          name = "AEM -Drupal Content Sync Enabled",
          description = "Is AEM to Drupal Content Sync Enabled",
          type = AttributeType.BOOLEAN
  )
  boolean contentSyncEnabled() default true;

  /**
   * Drupal AEM Content Data API Path.
   *
   * @return Drupal AEM Content Data API Path
   */
  @AttributeDefinition(name = "Drupal AEM Data Entity API Path", description = "Drupal AEM Data Entity API Path.",
          type = AttributeType.STRING)
  String drupalAemContentEntityPath();

  /**
   * Drupal AEM Content Data Delete API Path.
   *
   * @return Drupal AEM Content Data Delete API Path
   */
  @AttributeDefinition(name = "Drupal AEM Data Entity Delete API Path",
      description = "Drupal AEM Data Entity Delete API Path.",
      type = AttributeType.STRING)
  String drupalAemContentDeleteEntityPath();

}
