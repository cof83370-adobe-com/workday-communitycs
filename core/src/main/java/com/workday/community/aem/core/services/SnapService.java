package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.SnapConfig;
import org.osgi.annotation.versioning.ProviderType;

/**
 * The Snap logic service definition interface.
 */
@ProviderType
public interface SnapService {

  /**
   * Activates the snap service.
   *
   * @param config Service configuration object for snap logic service. This
   *               method is used for programmatically pass
   *               a configuration to the service object during service activate
   *               stage.
   */
  void activate(SnapConfig config);

  /**
   * Setter for RunModeConfigService.
   *
   * @param runModeConfigService RunModeConfigService object. This method is used
   *                             to explicitly pass the
   *                             runModeConfigService to the snap logic service
   */
  void setRunModeConfigService(RunModeConfigService runModeConfigService);

  /**
   * Sets the drupal service.
   *
   * @param drupalService DrupalService object. This method is used
   *                      to explicitly pass the
   *                      drupalService to the snap logic service
   */
  void setDrupalService(DrupalService drupalService);

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
