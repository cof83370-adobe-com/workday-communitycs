package com.workday.community.aem.core.services;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.pojos.ProfilePhoto;
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
   * Retrieves the user header menu.
   *
   * @param sfId Salesforce Id.
   *
   * @return The menu object as a string for common nav menus in the global header
   *     of the page.
   */
  String getUserHeaderMenu(String sfId);

  /**
   * Gets the user context.
   *
   * @param sfId Salesforce Id.
   *
   * @return the user email from the snap logic api call.
   */
  JsonObject getUserContext(String sfId);

  /**
   * Retrieves a profile photo.
   *
   * @param sfId Salesforce Id.
   *
   * @return The profile avatar icon data used for the profile in the global
   *     header of the page.
   */
  ProfilePhoto getProfilePhoto(String sfId);

  /**
   * Retrieves a user's profile data.
   *
   * @param sfId Salesforce Id.
   *
   * @return the user profile data.
   */
  String getUserProfile(String sfId);

  /**
   * Get adobe digital data.
   *
   * @param sfId        Salesforce Id.
   * @param pageTitle   Page title.
   * @param contentType Content type.
   * @return The adobe digital data.
   */
  String getAdobeDigitalData(String sfId, String pageTitle, String contentType);

  /**
   * Whether the cache is enabled.
   *
   * @return true if the cache is enabled.
   */
  boolean enableCache();
}
