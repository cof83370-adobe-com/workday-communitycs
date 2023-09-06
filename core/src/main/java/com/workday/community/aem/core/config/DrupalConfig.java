package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Drupal service configuration interface.
 */
@ObjectClassDefinition(name = "DrupalService Config", description = "Drupal Service OSGi Config Values")
public @interface DrupalConfig {
    @AttributeDefinition(name = "Drupal API Url", description = "Drupal API Url.", type = AttributeType.STRING)
    String drupalApiUrl();

    @AttributeDefinition(name = "Drupal Token API Path", description = "Drupal Token Path.", type = AttributeType.STRING)
    String drupalTokenPath();

    @AttributeDefinition(name = "Drupal User Data API Path", description = "Drupal User Data Path.", type = AttributeType.STRING)
    String drupalUserDataPath();

    @AttributeDefinition(name = "Drupal API Client Id", description = "Drupal API Client Id.", type = AttributeType.STRING)
    String drupalApiClientId();

    @AttributeDefinition(name = "Drupal API Client Secret", description = "Drupal API Client Secret.", type = AttributeType.STRING)
    String drupalApiClientSecret();

    @AttributeDefinition(name = "Drupal API token cache size", description = "Drupal API Token Cache size", type = AttributeType.INTEGER)
    int drupalTokenCacheMax() default 10;

    @AttributeDefinition(name = "Drupal API token cache timeout duration", description = "Drupal API Token Cache Timeout Duration (mills)", type = AttributeType.LONG)
    long drupalTokenCacheTimeout() default 3540000L; // Default to 59 minutes.
}
