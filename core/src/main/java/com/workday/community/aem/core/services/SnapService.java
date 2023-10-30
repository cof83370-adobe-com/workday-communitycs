package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The Snap logic service definition interface.
 */
@ProviderType
public interface SnapService {
  /**
   * Retrieves the user header menu.
   *
   * @param sfId Salesforce Id.
   * @return The menu object as a string for common nav menus in the global header of the page.
   */
  String getUserHeaderMenu(String sfId);

  /**
   * Whether the cache is enabled.
   *
   * @return true if the cache is enabled.
   */
  boolean enableCache();
}
