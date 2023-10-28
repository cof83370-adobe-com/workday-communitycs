package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

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
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import java.util.Date;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The OSGi service implementation for snap logic.
 */
@Slf4j
@Component(
    service = SnapService.class,
    property = {"service.pid=aem.core.services.snap"},
    configurationPid = "com.workday.community.aem.core.config.SnapConfig",
    immediate = true
)
@Designate(ocd = SnapConfig.class)
public class SnapServiceImpl implements SnapService {

  /**
   * The Run-mode configuration service.
   */
  @Reference
  @Setter
  private RunModeConfigService runModeConfigService;

  /**
   * The Drupal service.
   */
  @Reference
  @Setter
  private DrupalService drupalService;

  /**
   * The cache manager service.
   */
  @Reference
  @Setter
  private CacheManagerService cacheManagerService;

  /**
   * The gson service.
   */
  private final Gson gson = new Gson();

  /**
   * The snap Config.
   */
  private SnapConfig config;

  /**
   * {@inheritDoc}
   */
  @Activate
  @Modified
  @Override
  public void activate(SnapConfig config) {
    this.config = config;
    log.debug("SnapService is activated. enable Cache: {}, beta: {}",
        config.enableCache(), config.beta());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUserHeaderMenu(String sfId) {
    if (StringUtils.isEmpty(sfId)) {
      return "";
    }

    String menuCacheKey = String.format("header_menu_%s_%s", getEnv(), sfId);
    if (!enableCache()) {
      cacheManagerService.invalidateCache(CacheBucketName.STRING_VALUE.name(), menuCacheKey);
    }
    String retValue =
        cacheManagerService.get(CacheBucketName.STRING_VALUE.name(), menuCacheKey, () -> {
          String snapUrl = config.snapUrl();
          String navApi = config.navApi();
          String apiToken = config.navApiToken();
          String apiKey = config.navApiKey();

          if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(navApi)
              || StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
            // No Snap configuration provided, just return the default one.
            log.debug("There is no value for one or multiple configuration parameter: "
                    + "snapUrl={};navApi={};apiToken={};apiKey={};",
                snapUrl, navApi, apiToken, apiKey);
            return gson.toJson(this.getDefaultHeaderMenu());
          }

          try {
            String url = CommunityUtils.formUrl(snapUrl, navApi);
            url = String.format(url, sfId);

            String traceId = "Community AEM-" + new Date().getTime();
            // Execute the request.
            ApiResponse snapRes = RestApiUtil.doMenuGet(url, apiToken, apiKey, traceId);
            JsonObject defaultMenu = this.getDefaultHeaderMenu();
            if (snapRes == null || StringUtils.isEmpty(snapRes.getResponseBody())
                || snapRes.getResponseCode() != HttpStatus.SC_OK) {
              log.error("Sfdc menu fetch is empty, fallback to use local default");
              return gson.toJson(defaultMenu);
            }

            // Gson object for json handling.
            JsonObject sfMenu = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);

            // If SFID returned from okta is not present in Salesforce, it returns response
            // but with null values. Check for profile value, since that is always going to
            // be present in case of correct salesforce response.
            if (sfMenu.get("profile") == null || sfMenu.get("profile").isJsonNull()) {
              log.error("Nav profile is empty, fallback to use local default");
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
            log.error("Error in getNavUserData method call :: {}", e.getMessage());
          }

          return gson.toJson(this.getDefaultHeaderMenu());
        });

    if (retValue == null || OurmUtils.isMenuEmpty(gson, retValue)) {
      cacheManagerService.invalidateCache(CacheBucketName.STRING_VALUE.name(), menuCacheKey);
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
      cacheManagerService.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), cacheKey);
    }
    JsonObject ret = cacheManagerService.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey,
        () -> {
          try {
            ResourceResolver resourceResolver =
                this.cacheManagerService.getServiceResolver(READ_SERVICE_USER);
            // Reading the JSON File from DAM.
            return DamUtils.readJsonFromDam(resourceResolver, config.navFallbackMenuData());
          } catch (CacheException | DamException e) {
            log
                .error(
                    String.format("Exception in SnapServiceImpl for getFailStateHeaderMenu, error: %s",
                        e.getMessage()));
            return new JsonObject();
          }
        });

    if (ret.isJsonNull() || (ret.isJsonObject() && ret.size() == 0)) {
      cacheManagerService.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), cacheKey);
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

    if (isJsonElementNonNull(profileElement)) {
      JsonObject profileObject = profileElement.getAsJsonObject();
      // Populate user information.
      JsonElement contactObject = sfMenu.get(SnapConstants.USER_CONTACT_INFORMATION_KEY);

      if (isJsonElementNonNull(contactObject)) {
        JsonObject contactRoleElement = contactObject.getAsJsonObject();
        JsonElement lastName = contactRoleElement.get(SnapConstants.LAST_NAME_KEY);
        JsonElement firstName = contactRoleElement.get(SnapConstants.FIRST_NAME_KEY);

        JsonObject userInfoObject = new JsonObject();
        userInfoObject.addProperty(SnapConstants.FIRST_NAME_KEY, getJsonElementAsString(firstName));
        userInfoObject.addProperty(SnapConstants.LAST_NAME_KEY, getJsonElementAsString(lastName));
        userInfoObject.addProperty(SnapConstants.VIEW_PROFILE_LABEL_KEY,
            SnapConstants.PROFILE_BUTTON_VALUE);
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

  /**
   * Helper for retrieving the String value of a given JsonElement.
   *
   * @param jsonElement The JsonElement object.
   * @return The string value, or an empy string if null.
   */
  private String getJsonElementAsString(JsonElement jsonElement) {
    if (isJsonElementNonNull(jsonElement)) {
      return jsonElement.getAsString();
    }

    return StringUtils.EMPTY;
  }

  /**
   * Helper for determining whether a given JsonElement is null.
   *
   * @param jsonElement The JsonElement object.
   * @return True if the object is non-null, and not of type JsonNull.
   */
  private boolean isJsonElementNonNull(JsonElement jsonElement) {
    return jsonElement != null && !jsonElement.isJsonNull();
  }

}
