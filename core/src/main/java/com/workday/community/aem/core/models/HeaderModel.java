package com.workday.community.aem.core.models;

/**
 * The HeaderModel interface.
 */
public interface HeaderModel {

  /**
   * Gets the user navigation menu.
   *
   * @return The menu object as a string for common nav menus in the global header of the page.
   */
  String getUserHeaderMenus();

  /**
   *
   * @return The profile avatar icon data used for the profile in the global header of the page.
   */
  String getUserAvatar();

  /**
   *
   * @return The data layer data.
   */
  String getDataLayerData();
}