package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;

/**
 * The HeaderModel interface.
 */
public interface HeaderModel {

  /**
   * Gets the user navigation menu.
   *
   * @return The menu object as a string for common nav menus in the global header of the page
   */
  String getUserHeaderMenus();

  /**
   * Get data layer data.
   *
   * @return The data layer data
   */
  String getDataLayerData();

  /**
   * Gets global search redirection url.
   *
   * @return The global search redirection url
   */
  String getGlobalSearchUrl();

  /**
   * Get user client id.
   *
   * @return User UUID as user client id
   */
  String userClientId();

  /**
   * Indicate if client cache is enabled or not.
   *
   * @return "true" if enabled, "false" not
   */
  String enableClientCache();

  /**
   * Get search configuration.
   *
   * @return search configuration as a Json object
   */
  JsonObject getSearchConfig();
}