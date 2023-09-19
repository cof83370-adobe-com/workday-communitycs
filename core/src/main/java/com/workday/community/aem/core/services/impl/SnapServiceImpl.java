package com.workday.community.aem.core.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpStatus;

import java.util.Date;
import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

/**
 * The OSGi service implementation for snap logic.
 */
@Component(service = SnapService.class, property = {
    "service.pid=aem.core.services.snap"
}, configurationPid = "com.workday.community.aem.core.config.SnapConfig", immediate = true)
@Designate(ocd = SnapConfig.class)
public class SnapServiceImpl implements SnapService {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(SnapServiceImpl.class);

  /**
   * The Run-mode configuration service.
   */
  @Reference
  RunModeConfigService runModeConfigService;

  /**
   * The Drupal service.
   */
  @Reference
  DrupalService drupalService;

  @Reference
  CacheManagerService serviceCacheMgr;

  /** The resource resolver factory. */
  @Reference
  ResourceResolverFactory resResolverFactory;

  /** The snap Config. */
  private SnapConfig config;

  /** The gson service. */
  private final Gson gson = new Gson();

  @Activate
  @Modified
  @Override
  public void activate(SnapConfig config) {
    this.config = config;
    logger.debug("SnapService is activated. enable Cache: {}, beta: {}",
        config.enableCache(), config.beta());
  }

  @Override
  public void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
    this.resResolverFactory = resourceResolverFactory;
  }

  @Override
  public void setRunModeConfigService(RunModeConfigService runModeConfigService) {
    this.runModeConfigService = runModeConfigService;
  }

  @Override
  public void setDrupalService(DrupalService drupalService) {
    this.drupalService = drupalService;
  }

  // Following methods is for testing purpose
  public void setServiceCacheMgr(CacheManagerService serviceCacheMgr) {
    this.serviceCacheMgr = serviceCacheMgr;
  }

  @Override
  public String getUserHeaderMenu(String sfId) {
    String menuCacheKey = String.format("header_menu_%s_%s", getEnv(), sfId);
    if (!enableCache()) {
      serviceCacheMgr.invalidateCache(CacheBucketName.STRING_VALUE.name(), menuCacheKey);
    }
    String retValue = serviceCacheMgr.get(CacheBucketName.STRING_VALUE.name(), menuCacheKey, (key) -> {
      String snapUrl = config.snapUrl(), navApi = config.navApi(),
          apiToken = config.navApiToken(), apiKey = config.navApiKey();

      if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(navApi) ||
          StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
        // No Snap configuration provided, just return the default one.
        logger.debug(String.format("There is no value " +
            "for one or multiple configuration parameter: " +
            "snapUrl=%s;navApi=%s;apiToken=%s;apiKey=%s;",
            snapUrl, navApi, apiToken, apiKey));
        return gson.toJson(this.getDefaultHeaderMenu());
      }

      try {
        String url = CommunityUtils.formUrl(snapUrl, navApi);
        url = String.format(url, sfId);

        String traceId = "Community AEM-" + new Date().getTime();
        // Execute the request.
        APIResponse snapRes = RestApiUtil.doMenuGet(url, apiToken, apiKey, traceId);
        JsonObject defaultMenu = this.getDefaultHeaderMenu();
        if (snapRes == null || StringUtils.isEmpty(snapRes.getResponseBody()) ||
            snapRes.getResponseCode() != HttpStatus.SC_OK) {
          logger.error("Sfdc menu fetch is empty, fallback to use local default");
          return gson.toJson(defaultMenu);
        }

        // Gson object for json handling.
        JsonObject sfMenu = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);

        // If SFID returned from okta is not present in Salesforce, it returns response
        // but with null values. Check for profile value, since that is always going to
        // be present in case of correct salesforce response.
        if (sfMenu.get("profile") == null || sfMenu.get("profile").isJsonNull()) {
          logger.error("Nav profile is empty, fallback to use local default");
          return gson.toJson(defaultMenu);
        }

        // Update the user profile data from contactInformation field to userInfo field.
        updateProfileInfoWithNameAndAvatar(sfMenu, sfId);
        // Need to make merge sfMenu with local cache with beta experience.
        if (config.beta()) {
          return this.getMergedHeaderMenu(sfMenu, defaultMenu);
        }

        // Non-Beta will directly return the sf menu
        return gson.toJson(sfMenu);

      } catch (SnapException | JsonSyntaxException | JsonProcessingException e) {
        logger.error("Error in getNavUserData method call :: {}", e.getMessage());
      }

      return gson.toJson(this.getDefaultHeaderMenu());
    });

    if (OurmUtils.isMenuEmpty(gson, retValue)) {
      serviceCacheMgr.invalidateCache(CacheBucketName.STRING_VALUE.name(), menuCacheKey);
    }

    return retValue;
  }

  /**
   * Get default header menu.
   *
   * @return The menu.
   */
  private JsonObject getDefaultHeaderMenu() {
    String cacheKey = "default_menu_" + getEnv();
    if (!enableCache()) {
      serviceCacheMgr.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), cacheKey);
    }
    JsonObject ret = serviceCacheMgr.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey, (key) -> {
      try {
        ResourceResolver resourceResolver = this.serviceCacheMgr.getServiceResolver(READ_SERVICE_USER);
        // Reading the JSON File from DAM.
        return DamUtils.readJsonFromDam(resourceResolver, config.navFallbackMenuData());
      } catch (CacheException | DamException e) {
        logger
            .error(String.format("Exception in SnapServiceImpl for getFailStateHeaderMenu, error: %s", e.getMessage()));
        return new JsonObject();
      }
    });

    if (ret.isJsonNull() || (ret.isJsonObject() && ret.size() == 0)) {
      serviceCacheMgr.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), cacheKey);
    }
    return ret;
  }

  @Override
  public boolean enableCache() {
    return this.config.enableCache();
  }

  /**
   * Get merged header menu.
   *
   * @param sfNavObj The json object of nav.
   * @return The menu.
   */
  private String getMergedHeaderMenu(JsonObject sfNavObj, JsonObject defaultMenu) {
    if (sfNavObj != null && config.beta()) {
      String env = this.runModeConfigService.getEnv();
      CommonUtils.updateSourceFromTarget(sfNavObj, defaultMenu, "id", env);
      return gson.toJson(sfNavObj);
    }

    return gson.toJson(sfNavObj);
  }

  /**
   * Updates the user profile data from contact information.
   * 
   * @param sfMenu The menu data.
   */
  private void updateProfileInfoWithNameAndAvatar(JsonObject sfMenu, String sfId)
      throws JsonProcessingException {
    JsonElement profileElement = sfMenu.get(SnapConstants.PROFILE_KEY);

    if (profileElement != null && !profileElement.isJsonNull()) {
      JsonObject profileObject = profileElement.getAsJsonObject();
      // Populate user information.
      JsonElement contactObject = sfMenu.get(SnapConstants.USER_CONTACT_INFORMATION_KEY);
      JsonObject contactRoleElement = (contactObject != null && !contactObject.isJsonNull())
          ? contactObject.getAsJsonObject()
          : null;

      if (contactRoleElement != null && !contactRoleElement.isJsonNull()) {
        JsonElement lastName = contactRoleElement.get(SnapConstants.LAST_NAME_KEY);
        JsonElement firstName = contactRoleElement.get(SnapConstants.FIRST_NAME_KEY);

        JsonObject userInfoObject = new JsonObject();
        userInfoObject.addProperty(SnapConstants.LAST_NAME_KEY,
            (lastName != null && !lastName.isJsonNull()) ? lastName.getAsString() : StringUtils.EMPTY);
        userInfoObject.addProperty(SnapConstants.FIRST_NAME_KEY,
            (firstName != null && !firstName.isJsonNull()) ? firstName.getAsString() : StringUtils.EMPTY);
        userInfoObject.addProperty(SnapConstants.VIEW_PROFILE_LABEL_KEY, SnapConstants.PROFILE_BUTTON_VALUE);
        userInfoObject.addProperty(SnapConstants.HREF_KEY, config.userProfileUrl());
        profileObject.add(SnapConstants.USER_INFO_KEY, userInfoObject);

        // Populate profile photo information.
        JsonObject avatarObject = new JsonObject();
        avatarObject.addProperty(SnapConstants.IMAGE_DATA_KEY, this.drupalService.getUserProfileImage(sfId));
        profileObject.add(SnapConstants.AVATAR_KEY, avatarObject);
      }
    }
  }

  private String getEnv() {
    String env = this.runModeConfigService.getEnv();
    return (env == null) ? "local" : env;
  }

}