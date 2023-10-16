package com.workday.community.aem.core.config;

import static com.workday.community.aem.core.constants.SearchConstants.SEARCH_EMAIL_SECURITY_PROVIDER;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Coveo search configuration class.
 */
@ObjectClassDefinition(
    name = "Coveo Search configuration",
    description = "Parameters for Coveo search"
)
public @interface CoveoSearchConfig {

  /**
   * Returns the URI to the Coveo search token API endpoint.
   *
   * @return The URI.
   */
  @AttributeDefinition(
      name = "Search token Api endpoint",
      description = "Search Token Api endpoint"
  )
  String tokenApi();

  /**
   * Returns the URI to the Coveo search field API endpoint.
   *
   * @return The URI.
   */
  @AttributeDefinition(
      name = "Search field look up Api endpoint",
      description = "Search field lookup Api endpoint"
  )
  String searchFieldLookupApi() default "https://platform.cloud.coveo.com/rest/search/values";

  /**
   * Returns the search token API key.
   *
   * @return The key.
   */
  @AttributeDefinition(name = "Search token Api Key", description = "Search Api Key")
  String tokenApiKey();

  /**
   * Returns the recommendation API key.
   *
   * @return The key.
   */
  @AttributeDefinition(name = "Recommendation API Key", description = "Recommendation API Key")
  String recommendationApiKey();

  /**
   * Returns the upcoming event API key.
   *
   * @return The key.
   */
  @AttributeDefinition(name = "Upcoming Event Api Key", description = "Upcoming Event Api Key")
  String upcomingEventApiKey();

  /**
   * Returns the organization ID.
   *
   * @return The organization ID.
   */
  @AttributeDefinition(name = "Organization Id", description = "Organization Id")
  String orgId();

  /**
   * Where the search query originated from.
   *
   * @return The search origin.
   */
  @AttributeDefinition(name = "Search Hub", description = "Search Hub", type = AttributeType.STRING)
  String searchHub();

  /**
   * Default email for fetching a token.
   *
   * @return The default email.
   */
  @AttributeDefinition(name = "Default email", description = "Default email for fetching token")
  String defaultEmail();

  /**
   * The token user ID provider.
   *
   * @return The provider.
   */
  @AttributeDefinition(name = "Token user id provider", description = "Token user id provider")
  String userIdProvider() default SEARCH_EMAIL_SECURITY_PROVIDER;

  /**
   * The token user type.
   *
   * @return The type.
   */
  @AttributeDefinition(name = "Token user type", description = "Token user type")
  String userType();

  /**
   * The time in ms that a token is valid.
   *
   * @return The length in ms.
   */
  @AttributeDefinition(
      name = "Token Valid period",
      description = "Time token is valid for (ms)",
      type = AttributeType.INTEGER
  )
  int tokenValidTime() default 60 * 1000;

  /**
   * Whether the site is in dev mode.
   *
   * @return True if in dev mode, otherwise false.
   */
  @AttributeDefinition(name = "Dev Mode", description = "Dev Mode", type = AttributeType.BOOLEAN)
  boolean devMode();

  /**
   * The redirection URL for global search.
   *
   * @return The URL.
   */
  @AttributeDefinition(
      name = "Search Redirection URL for global search",
      description = "Redirection URL for global search"
  )
  String globalSearchUrl();

}
