package com.workday.community.aem.core.services;

/**
 * The Coveo search service definition class.
 */
public interface SearchApiConfigService {
  /**
   * Get the Organization id.
   *
   * @return the Organization id from the configuration.
   */
  String getOrgId();

  /**
   * Get the search hub
   *
   * @return the search hub name.
   */
  String getSearchHub();

  /**
   * Get user id provider
   *
   * @return the user id provider.
   */
  String getUserIdProvider();

  /**
   * Get user id type
   *
   * @return the user id type.
   */
  String getUserIdType();

  /**
   * Get the default email
   *
   * @return the default email
   */
  String getDefaultEmail();

  /**
   * Get the Coveo search token API endpoint from the configuration.
   *
   * @return the Coveo search API endpoint.
   */
  String getSearchTokenAPI();

  /**
   * Get the Coveo search event type API endpoint from the configuration.
   *
   * @return the Coveo search event type API endpoint
   */
  String getSearchFieldLookupAPI();

  /**
   * Get the Coveo search token API key from the configuration.
   *
   * @return the Coveo search API key.
   */
  String getSearchTokenAPIKey();

  /**
   * Get the Coveo search recommendation API key from the configuration.
   *
   * @return the Coveo search recommendation API key.
   */
  String getRecommendationAPIKey();

  /**
   * Get the Coveo search upcoming event API key from the configuration.
   *
   * @return the Coveo search upcoming event API key.
   */
  String getUpcomingEventAPIKey();

  /**
   * Get the valid time (ms) that the Coveo search token need to keep valid from
   * the configuration.
   *
   * @return valid time (ms) that the Coveo search token need to keep valid.
   */
  int getTokenValidTime();

  /**
   * Get the dev mode configuration from the configuration
   *
   * @return true if it is dev mode (like local test), false if not.
   */
  boolean isDevMode();

  /**
   * Gets the global search redirection URL
   *
   * @return the global search redirection URL.
   */
  String getGlobalSearchURL();
}
