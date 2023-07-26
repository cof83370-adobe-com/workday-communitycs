package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Interface OurmDrupalConfig.
 */
@ObjectClassDefinition(name = "Ourm Users Search Config", description = "Ourm Users Search OSGi Config Vaues")
public @interface OurmDrupalConfig {

  /**
   * Ourm drupal lookup api endpoint.
   *
   * @return the string
   */
  @AttributeDefinition(name = "Ourm Users Search field look up Api endpoint", description = "Ourm Users Search field lookup Api endpoint")
  String ourmDrupalLookupApiEndpoint();


  /**
   * Ourm drupal consumer key.
   *
   * @return the string
   */
  @AttributeDefinition(name = "Ourm Users Search field look up Api Consumer Key", description = "Ourm Users Search field look up Api Consumer Key")
  String ourmDrupalConsumerKey();

  /**
   * Ourm drupal consumer secret.
   *
   * @return the string
   */
  @AttributeDefinition(name = "Ourm Users Search field look up Api Consumer Secret", description = "Ourm Users Search field look up Api Consumer Secret")
  String ourmDrupalConsumerSecret();

}
