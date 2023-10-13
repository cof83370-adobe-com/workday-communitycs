package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;

/**
 * The HeaderModel interface.
 */
public interface HeaderModel {

  /**
   * Gets the user navigation menu.
   *
   * @return The menu object as a string for common nav menus in the global header
   *         of the page.
   */
  String getUserHeaderMenus();

  /**
   *
   * @return The data layer data.
   */
  String getDataLayerData();

  /**
   * Gets global search redirection url.
   *
   * @return The global search redirection url.
   */
  String getGlobalSearchURL();

  /**
   *
   * @return User UUID as user client id.
   */
  String userClientId();

  /**
   * Get search configuration.
   *
   * @return search configuration as a Json object.
   */
  JsonObject getSearchConfig();
}