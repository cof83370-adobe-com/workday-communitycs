package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Coveo service configuration interface.
 */
@ObjectClassDefinition(
    name = "Coveo Indexing api configuration",
    description = "Parameters for Coveo indexing api"
)
public @interface CoveoIndexApiConfig {

  /**
   * Whether Coveo indexing is enabled.
   *
   * @return True if enabled, otherwise false.
   */
  @AttributeDefinition(
      name = "Enabled",
      description = "Is Coveo indexing enabled",
      type = AttributeType.BOOLEAN
  )
  boolean isCoveoIndexingEnabled() default false;

  /**
   * Returns the Coveo API key.
   *
   * @return The Coveo API key.
   */
  @AttributeDefinition(
      name = "Api key",
      description = "Coveo api key",
      type = AttributeType.STRING
  )
  String coveoApiKey();

  /**
   * Returns the URI to the Coveo push API endpoint.
   *
   * @return The URI.
   */
  @AttributeDefinition(
      name = "Push Api Uri",
      description = "Coveo push api endpoint",
      type = AttributeType.STRING
  )
  String pushApiUri() default "https://api.cloud.coveo.com/push/v1/organizations/";

  /**
   * Returns the URI of the Coveo source API endpoint.
   *
   * @return The URI.
   */
  @AttributeDefinition(
      name = "Source Api Uri",
      description = "Coveo source api endpoint",
      type = AttributeType.STRING
  )
  String sourceApiUri() default "https://platform.cloud.coveo.com/rest/organizations/";

  /**
   * Returns the Coveo organization ID.
   *
   * @return The organization ID.
   */
  @AttributeDefinition(
      name = "Organization Id",
      description = "Coveo organization id",
      type = AttributeType.STRING
  )
  String organizationId();

  /**
   * Returns the Coveo source ID.
   *
   * @return The source ID.
   */
  @AttributeDefinition(
      name = "Source Id",
      description = "Coveo source id",
      type = AttributeType.STRING
  )
  String sourceId();

  /**
   * Returns the number of documents to process in a given batch.
   *
   * @return The batch size.
   */
  @AttributeDefinition(
      name = "Batch size",
      description = "Coveo job batch size",
      type = AttributeType.INTEGER
  )
  int batchSize() default 50;

}
