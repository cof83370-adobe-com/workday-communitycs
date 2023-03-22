package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Coveo search configuration class.
 */
@ObjectClassDefinition(
    name = "Coveo Search configuration",
    description = "Parameters for Coveo search."
)
public @interface CoveoSearchConfig {

  @AttributeDefinition(name = "Search token Api endpoint", description = "Search Token api endpoint")
  String tokenApi();

  @AttributeDefinition(name = "Search token API Key", description = "Search API Key")
  String tokenApiKey();

  @AttributeDefinition(name = "Recommendation API Key", description = "Recommendation API Key")
  String recommendationApiKey();

  @AttributeDefinition(name = "Upcoming Event API Key", description = "Upcoming Event API Key")
  String upcomingEventApiKey();

  @AttributeDefinition(name = "Organization Id", description = "Organization Id")
  String orgId();

  @AttributeDefinition(name = "Token Valid period", description = "Time token is valid for (ms)",  type = AttributeType.INTEGER)
  int tokenValidTime();

  @AttributeDefinition(name = "Dev Mode", description = "Dev Mode",  type = AttributeType.BOOLEAN)
  boolean devMode();
}
