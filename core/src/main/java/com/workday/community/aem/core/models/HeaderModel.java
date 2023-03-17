package com.workday.community.aem.core.models;

import java.io.IOException;

/**
 * The NavHeaderModel interface.
 */
public interface HeaderModel {

  /**
   * Gets the user navigation menu.
   *
   * @return The menu object as a string for common nav menus in the global header of the page.
   * @throws IOException
   */
  String getUserHeaderMenus();

  /**
   *
   * @return The profile avatar icon data used for the profile in the global header of the page.
   */
  String getUserAvatarUrl();
}