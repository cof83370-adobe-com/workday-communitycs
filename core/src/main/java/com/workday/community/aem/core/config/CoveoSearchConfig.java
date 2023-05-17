package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import static com.workday.community.aem.core.constants.SearchConstants.SEARCH_EMAIL_SECURITY_PROVIDER;

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

  @AttributeDefinition(name = "Search field look up API endpoint", description = "Search field lookup API endpoint")
  String searchFieldLookupApi() default "https://platform.cloud.coveo.com/rest/search/values";

  @AttributeDefinition(name = "Search token API Key", description = "Search API Key")
  String tokenApiKey();

  @AttributeDefinition(name = "Default email", description = "Default email for fetching token")
  String defaultEmail();

  @AttributeDefinition(name = "Recommendation API Key", description = "Recommendation API Key")
  String recommendationApiKey();

  @AttributeDefinition(name = "Upcoming Event API Key", description = "Upcoming Event API Key")
  String upcomingEventApiKey();

  @AttributeDefinition(name = "Organization Id", description = "Organization Id")
  String orgId();

  @AttributeDefinition(name = "Token user id provider", description = "Token user id provider")
  String userIdProvider() default SEARCH_EMAIL_SECURITY_PROVIDER;

  @AttributeDefinition(name = "Token user type", description = "Token user type")
  String userType();

  @AttributeDefinition(name = "Search Hub", description = "Search Hub", type = AttributeType.STRING)
  String searchHub();

  @AttributeDefinition(name = "Token Valid period", description = "Time token is valid for (ms)",  type = AttributeType.INTEGER)
  int tokenValidTime() default 60 * 1000;

  @AttributeDefinition(name = "Dev Mode", description = "Dev Mode",  type = AttributeType.BOOLEAN)
  boolean devMode();
}
