package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The OURM configuration interface.
 */
@ObjectClassDefinition(
    name = "Ourm Drupal Search Config",
    description = "Ourm Drupal Search OSGi Config Vaues"
)
public @interface OurmDrupalConfig {

  /**
   * Ourm drupal lookup API endpoint.
   *
   * @return The endpoint URL.
   */
  @AttributeDefinition(
      name = "Ourm Drupal Rest Root End",
      description = "Ourm Drupal Rest Root endpoint"
  )
  String ourmDrupalRestRoot();

  /**
   * Ourm drupal user search API endpoint.
   *
   * @return The endpoint URL.
   */
  @AttributeDefinition(
      name = "Ourm Drupal User Search Path",
      description = "Ourm Drupal User Search Path"
  )
  String ourmDrupalUserSearchPath();

  /**
   * Ourm drupal consumer key.
   *
   * @return the string
   */
  @AttributeDefinition(
      name = "Ourm Drupal look up Api Consumer Key",
      description = "Ourm Drupal look up Api Consumer Key"
  )
  String ourmDrupalConsumerKey();

  /**
   * Ourm drupal consumer secret.
   *
   * @return the string
   */
  @AttributeDefinition(
      name = "Ourm Drupal look up Api Consumer Secret",
      description = "Ourm Drupal look up Api Consumer Secret"
  )
  String ourmDrupalConsumerSecret();

}
