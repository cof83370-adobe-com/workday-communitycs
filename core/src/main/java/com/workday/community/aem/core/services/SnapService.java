package com.workday.community.aem.core.services;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.annotation.versioning.ProviderType;

/**
 * The Snap logic service definition interface.
 */
@ProviderType
public interface SnapService {
  /**
   * @param config Service configuration object for snap logic service. This
   *               method is used for programmatically pass
   *               a configuration to the service object during service activate
   *               stage.
   */
  void activate(SnapConfig config);

  /**
   * @param resourceResolverFactory ResourceResolverFactory object. This method is
   *                                used to explicitly pass the
   *                                Resource resolver to the snap logic service
   */
  void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory);

  /**
   * @param runModeConfigService RunModeConfigService object. This method is used
   *                             to explicitly pass the
   *                             runModeConfigService to the snap logic service
   */
  void setRunModeConfigService(RunModeConfigService runModeConfigService);

  /**
   * @param drupalService DrupalService object. This method is used
   *                      to explicitly pass the
   *                      drupalService to the snap logic service
   */
  void setDrupalService(DrupalService drupalService);

  /**
   * @param sfId Salesforce Id.
   * @return The menu object as a string for common nav menus in the global header
   *         of the page.
   */
  String getUserHeaderMenu(String sfId);

  /**
   * @param sfId Salesforce Id.
   * @return the user email from the snap logic api call.
   */
  JsonObject getUserContext(String sfId);

  /**
   *
   * @return true if the cache is enabled.
   */
  boolean enableCache();
}
