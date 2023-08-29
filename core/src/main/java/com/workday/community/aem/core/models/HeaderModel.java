package com.workday.community.aem.core.models;

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
   * @return CHANGED if the cache state is changed.
   */
  String cacheStatus();
}