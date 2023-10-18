package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_ID;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_NUMBER;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_ROLE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.IS_NSC;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.NSC;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.PAGE_NAME;
import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import java.util.Date;
import java.util.regex.PatternSyntaxException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
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
    configurationPolicy = ConfigurationPolicy.OPTIONAL
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
    String menuCacheKey = String.format("header_menu_%s_%s", getEnv(), sfId);
    if (!enableCache()) {
      cacheManagerService.invalidateCache(CacheBucketName.STRING_VALUE.name(), menuCacheKey);
    }
    String retValue =
        cacheManagerService.get(CacheBucketName.STRING_VALUE.name(), menuCacheKey, (key) -> {
          String snapUrl = config.snapUrl();
          String navApi = config.navApi();
          String apiToken = config.navApiToken();
          String apiKey = config.navApiKey();

          if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(navApi)
              || StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
            // No Snap configuration provided, just return the default one.
            log.debug("there is no value for one or multiple configuration parameter: "
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

    if (OurmUtils.isMenuEmpty(gson, retValue)) {
      cacheManagerService.invalidateCache(CacheBucketName.STRING_VALUE.name(), menuCacheKey);
    }

    return retValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonObject getUserContext(String sfId) {
    String cacheKey = String.format("user_context_%s_%s", getEnv(), sfId);
    if (!enableCache()) {
      cacheManagerService.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), cacheKey);
    }

    JsonObject ret = cacheManagerService.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey,
        (key) -> {
          try {
            log.debug("SnapImpl: Calling snap api getUserContext()...");
            String url = CommunityUtils.formUrl(config.snapUrl(), config.snapContextPath());
            if (url == null) {
              return new JsonObject();
            }

            url = String.format(url, sfId);
            String jsonResponse =
                RestApiUtil.doSnapGet(url, config.snapContextApiToken(), config.snapContextApiKey());
            return gson.fromJson(jsonResponse, JsonObject.class);
          } catch (SnapException | JsonSyntaxException e) {
            log.error("Error in getUserContext method :: {}", e.getMessage());
          }

          log.error("User context is not fetched from the snap context API call without error, "
              + "please contact admin.");
          return new JsonObject();
        }
    );

    if (ret.isJsonNull() || (ret.isJsonObject() && ret.size() == 0)) {
      cacheManagerService.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), cacheKey);
    }

    return ret;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ProfilePhoto getProfilePhoto(String userId) {
    String cacheKey = String.format("profile_photo_%s_%s", getEnv(), userId);
    if (!enableCache()) {
      cacheManagerService.invalidateCache(CacheBucketName.OBJECT_VALUE.name(),
          cacheKey);
    }
    return cacheManagerService.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey,
        (key) -> {
          String snapUrl = config.snapUrl();
          String avatarUrl = config.sfdcUserAvatarUrl();
          String url = CommunityUtils.formUrl(snapUrl, avatarUrl);
          url = String.format(url, userId);

          try {
            log.info("SnapImpl: Calling SNAP getProfilePhoto(), url is {}", url);
            String jsonResponse = RestApiUtil.doSnapGet(url, config.sfdcUserAvatarToken(),
                config.sfdcUserAvatarApiKey());
            if (StringUtils.isNotBlank(jsonResponse)) {
              ObjectMapper objectMapper = new ObjectMapper();
              return objectMapper.readValue(jsonResponse, ProfilePhoto.class);
            }
          } catch (SnapException | JsonProcessingException e) {
            log.error("Error in getProfilePhoto method, {} ", e.getMessage());
          }
          return null;
        });
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
        (key) -> {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUserProfile(String sfId) {
    String cacheKey = String.format("user_profile_%s_%s", getEnv(), sfId);
    if (!enableCache()) {
      cacheManagerService.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), cacheKey);
    }
    Object userProfile =
        cacheManagerService.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey, (key) -> {
          try {
            String url = CommunityUtils.formUrl(config.snapUrl(), config.snapProfilePath());
            if (StringUtils.isNotBlank(url)) {
              url = String.format(url, sfId);
              return RestApiUtil.doSnapGet(url, config.snapProfileApiToken(),
                  config.snapProfileApiKey());
            }
          } catch (SnapException | JsonSyntaxException e) {
            log.error("Error in getUserProfile method :: {}", e.getMessage());
          }
          log.error("User profile data is not fetched from the snap profile API call without "
              + "error, please contact admin.");
          return null;
        });

    if (userProfile == null) {
      return null;
    }
    return (userProfile instanceof JsonObject) ? gson.toJson(userProfile) : userProfile.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAdobeDigitalData(String sfId, String pageTitle, String contentType) {
    String pTitle = pageTitle == null ? "dpt" : pageTitle.replaceAll("[^\\w\\s]", "");
    String cntType = contentType == null ? "dct" : contentType.replaceAll("[^\\w\\s]", "");
    String cacheKey = String.format("adobe_data_%s_%s.%s.%s", getEnv(), sfId, pageTitle, contentType);
    if (!enableCache()) {
      cacheManagerService.invalidateCache(CacheBucketName.STRING_VALUE.name(), cacheKey);
    }
    return cacheManagerService.get(CacheBucketName.STRING_VALUE.name(), cacheKey, (key) -> {
      String profileData = getUserProfile(sfId);
      JsonObject digitalData = generateAdobeDigitalData(profileData);

      JsonObject pageProperties = new JsonObject();
      pageProperties.addProperty(CONTENT_TYPE, cntType);
      pageProperties.addProperty(PAGE_NAME, pTitle);

      digitalData.add("page", pageProperties);
      return String.format("{\"%s\":%s}", "digitalData", gson.toJson(digitalData));
    });
  }

  /**
   * {@inheritDoc}
   */
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
   * Generate adobe digital data.
   *
   * @param profileData The user profile api response as string.
   * @return The digital data.
   */
  private JsonObject generateAdobeDigitalData(String profileData) {
    String contactRole = "";
    String contactNumber = "";
    String accountId = "";
    String accountName = "";
    String accountType = "";
    boolean isNsc = false;
    String timeZoneStr = "";
    JsonObject profileObject;
    if (profileData != null) {
      try {
        profileObject = gson.fromJson(profileData, JsonObject.class);
      } catch (JsonSyntaxException e) {
        profileObject = new JsonObject();
        log.error("Error in generateAdobeDigitalData method :: {}",
            e.getMessage());
      }

      JsonElement contactRoleElement = profileObject.get(CONTACT_ROLE);
      contactRole = getJsonElementAsString(contactRoleElement);
      isNsc = contactRole.contains(NSC);

      JsonElement contactNumberElement = profileObject.get(CONTACT_NUMBER);
      contactNumber = getJsonElementAsString(contactNumberElement);

      JsonElement wrcOrgId = profileObject.get("wrcOrgId");
      accountId = getJsonElementAsString(wrcOrgId);

      JsonElement organizationName = profileObject.get("organizationName");
      accountName = getJsonElementAsString(organizationName);

      JsonElement isWorkmateElement = profileObject.get("isWorkmate");
      boolean isWorkdayMate = isJsonElementNonNull(isWorkmateElement)
          && isWorkmateElement.getAsBoolean();

      JsonElement typeElement = profileObject.get("type");
      accountType = isWorkdayMate
          ? "workday"
          : getJsonElementAsString(typeElement).toLowerCase();

      JsonElement timeZoneElement = profileObject.get("timeZone");
      timeZoneStr = getJsonElementAsString(timeZoneElement);
    }

    JsonObject userProperties = new JsonObject();
    userProperties.addProperty(CONTACT_ROLE, contactRole);
    userProperties.addProperty(CONTACT_NUMBER, contactNumber);
    userProperties.addProperty(IS_NSC, isNsc);
    userProperties.addProperty("timeZone", timeZoneStr);

    JsonObject orgProperties = new JsonObject();
    orgProperties.addProperty(ACCOUNT_ID, accountId);
    orgProperties.addProperty(ACCOUNT_NAME, accountName);
    orgProperties.addProperty(ACCOUNT_TYPE, accountType);

    JsonObject digitalData = new JsonObject();
    digitalData.add("user", userProperties);
    digitalData.add("org", orgProperties);

    return digitalData;
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
        avatarObject.addProperty(SnapConstants.IMAGE_DATA_KEY, getUserAvatar(sfId));
        profileObject.add(SnapConstants.AVATAR_KEY, avatarObject);
      }
    }
  }

  /**
   * Gets the user avatar data.
   *
   * @param sfId SFID
   * @return image data as string
   */
  private String getUserAvatar(String sfId) {
    ProfilePhoto content = getProfilePhoto(sfId);
    if (content == null) {
      return StringUtils.EMPTY;
    }

    String encodedPhoto = content.getBase64content();
    String extension = content.getFileNameWithExtension();
    try {
      String[] extensionSplit =
          StringUtils.isNotBlank(extension) ? extension.split("\\.") : new String[] {};
      if (extensionSplit.length > 0) {
        extension = extensionSplit[extensionSplit.length - 1];
      } else {
        log.error("No extension found in the data");
      }
    } catch (ArrayIndexOutOfBoundsException | PatternSyntaxException e) {
      log.error("An exception occurred" + e.getMessage());
    }
    if (StringUtils.isNotBlank(extension) && StringUtils.isNotBlank(encodedPhoto)) {
      return "data:image/" + extension + ";base64," + encodedPhoto;
    } else {
      log.error("getUserAvatar method returns null.");
      return StringUtils.EMPTY;
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
