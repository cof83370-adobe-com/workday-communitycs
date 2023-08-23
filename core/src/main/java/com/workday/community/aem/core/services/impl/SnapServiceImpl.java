package com.workday.community.aem.core.services.impl;

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
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.DamUtils;
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
import java.util.regex.PatternSyntaxException;

import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.PAGE_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_NUMBER;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_ROLE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.IS_NSC;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.NSC;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_ID;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_TYPE;
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
    logger.info("SnapService is activated.");
  }

  @Override
  public void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
    this.resResolverFactory = resourceResolverFactory;
  }

  @Override
  public void setRunModeConfigService(RunModeConfigService runModeConfigService) {
    this.runModeConfigService = runModeConfigService;
  }

  // Following methods is for testing purpose
  public void setServiceCacheMgr(CacheManagerService serviceCacheMgr) {
    this.serviceCacheMgr = serviceCacheMgr;
  }

  @Override
  public String getUserHeaderMenu(String sfId) {
    String menuCacheKey =  String.format("menu-%s", sfId);

    return serviceCacheMgr.get(CacheBucketName.STRING_VALUE.name(), menuCacheKey, (key) -> {
      String snapUrl = config.snapUrl(), navApi = config.navApi(),
          apiToken = config.navApiToken(), apiKey = config.navApiKey();

      if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(navApi) ||
          StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
        // No Snap configuration provided, just return the default one.
        logger.debug(String.format("there is no value " +
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
          String finalMenu = this.getMergedHeaderMenu(sfMenu, defaultMenu);
          return finalMenu;
        }

        // Non-Beta will directly return the sf menu
        return gson.toJson(sfMenu);

      } catch (SnapException | JsonSyntaxException | JsonProcessingException e) {
        logger.error("Error in getNavUserData method call :: {}", e.getMessage());
      }

      return gson.toJson(this.getDefaultHeaderMenu());
    });
  }

  @Override
  public JsonObject getUserContext(String sfId) {
    String cacheKey = String.format("profile-%s", sfId);
    return serviceCacheMgr.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey, (key) -> {
      try {
        logger.debug("SnapImpl: Calling SNAP getUserContext()...");
        String url = CommunityUtils.formUrl(config.snapUrl(), config.snapContextPath());
        if (url == null) {
          return new JsonObject();
        }

        url = String.format(url, sfId);
        String jsonResponse = RestApiUtil.doSnapGet(url, config.snapContextApiToken(), config.snapContextApiKey());
        JsonObject res = gson.fromJson(jsonResponse, JsonObject.class);
        return res;
      } catch (SnapException | JsonSyntaxException e) {
        logger.error("Error in getUserContext method :: {}", e.getMessage());
      }

      logger.error("User context is not fetched from the snap context API call without error, please contact admin.");
      return new JsonObject();
    });
  }

  @Override
  public ProfilePhoto getProfilePhoto(String userId) {
    String cacheKey = String.format("photo-%s", userId);
    return serviceCacheMgr.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey, (key) -> {
      String snapUrl = config.snapUrl(), avatarUrl = config.sfdcUserAvatarUrl();
      String url = CommunityUtils.formUrl(snapUrl, avatarUrl);
      url = String.format(url, userId);

      try {
        logger.info("SnapImpl: Calling SNAP getProfilePhoto(), url is {}", url);
        String jsonResponse = RestApiUtil.doSnapGet(url, config.sfdcUserAvatarToken(), config.sfdcUserAvatarApiKey());
        if (jsonResponse != null) {
          ObjectMapper objectMapper = new ObjectMapper();
          ProfilePhoto profilePhoto = objectMapper.readValue(jsonResponse, ProfilePhoto.class);
          return profilePhoto;
        }
      } catch (SnapException | JsonProcessingException e) {
        logger.error("Error in getProfilePhoto method, {} ", e.getMessage());
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
    return serviceCacheMgr.get(CacheBucketName.OBJECT_VALUE.name(), "default-menu", (key) -> {
      try {
        ResourceResolver resourceResolver = this.serviceCacheMgr.getServiceResolver(READ_SERVICE_USER);
        // Reading the JSON File from DAM.
        return DamUtils.readJsonFromDam(resourceResolver, config.navFallbackMenuData());
      } catch (CacheException | DamException e) {
        logger.error(String.format("Exception in SnapServiceImpl for getFailStateHeaderMenu, error: %s", e.getMessage()));
        return new JsonObject();
      }
    });
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

  @Override
  public String getUserProfile(String sfId) {
    String cacheKey = String.format("profile-%s", sfId);
    return serviceCacheMgr.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey, (key) -> {
      try {
        String url = CommunityUtils.formUrl(config.snapUrl(), config.snapProfilePath());
        if (StringUtils.isNotBlank(url)) {
          url = String.format(url, sfId);
          return RestApiUtil.doSnapGet(url, config.snapProfileApiToken(), config.snapProfileApiKey());
        }
      } catch (SnapException | JsonSyntaxException e) {
        logger.error("Error in getUserProfile method :: {}", e.getMessage());
      }

      logger
          .error("User profile data is not fetched from the snap profile API call without error, please contact admin.");
      return null;
    });
  }

  @Override
  public String getAdobeDigitalData(String sfId, String pageTitle, String contentType) {
    String cacheKey = String.format("%s.%s.%s", sfId, pageTitle, contentType);
    return serviceCacheMgr.get(CacheBucketName.STRING_VALUE.name(), cacheKey, (key) -> {
      String profileData = getUserProfile(sfId);
      JsonObject digitalData = generateAdobeDigitalData(profileData);

      JsonObject pageProperties = new JsonObject();
      pageProperties.addProperty(CONTENT_TYPE, contentType);
      pageProperties.addProperty(PAGE_NAME, pageTitle);

      digitalData.add("page", pageProperties);
      String res = String.format("{\"%s\":%s}", "digitalData", gson.toJson(digitalData));
      return res;
    });
  }

  /**
   * Generate adobe digital data.
   * 
   * @param profileData The user profile api response as string.
   * @return The digital data.
   */
  private JsonObject generateAdobeDigitalData(String profileData) {
    JsonObject digitalData = new JsonObject();
    JsonObject userProperties = new JsonObject();
    JsonObject orgProperties = new JsonObject();
    String contactRole = "";
    String contactNumber = "";
    String accountID = "";
    String accountName = "";
    String accountType = "";
    boolean isNSC = false;
    String timeZoneStr = "";
    if (profileData != null) {
      try {
        JsonObject profileObject = gson.fromJson(profileData, JsonObject.class);
        JsonElement contactRoleElement = profileObject.get(CONTACT_ROLE);
        contactRole = (contactRoleElement == null || contactRoleElement.isJsonNull()) ? ""
            : contactRoleElement.getAsString();
        JsonElement contactNumberElement = profileObject.get(CONTACT_NUMBER);
        contactNumber = (contactRoleElement == null || contactNumberElement.isJsonNull()) ? ""
            : contactNumberElement.getAsString();
        isNSC = contactRole.contains(NSC);
        JsonElement wrcOrgId = profileObject.get("wrcOrgId");
        accountID = (wrcOrgId == null || wrcOrgId.isJsonNull()) ? "" : wrcOrgId.getAsString();
        JsonElement organizationName = profileObject.get("organizationName");
        accountName = (organizationName == null || organizationName.isJsonNull()) ? "" : organizationName.getAsString();
        JsonElement isWorkmateElement = profileObject.get("isWorkmate");
        boolean isWorkdayMate = isWorkmateElement != null && !isWorkmateElement.isJsonNull() &&
            isWorkmateElement.getAsBoolean();
        JsonElement typeElement = profileObject.get("type");
        accountType = isWorkdayMate ? "workday"
            : (typeElement == null || typeElement.isJsonNull() ? "" : typeElement.getAsString().toLowerCase());
        JsonElement timeZoneElement = profileObject.get("timeZone");
        timeZoneStr = (timeZoneElement == null || timeZoneElement.isJsonNull()) ? "" : timeZoneElement.getAsString();
      } catch (JsonSyntaxException e) {
        logger.error("Error in generateAdobeDigitalData method :: {}",
            e.getMessage());
      }
    }
    userProperties.addProperty(CONTACT_ROLE, contactRole);
    userProperties.addProperty(CONTACT_NUMBER, contactNumber);
    userProperties.addProperty(IS_NSC, isNSC);
    userProperties.addProperty("timeZone", timeZoneStr);
    orgProperties.addProperty(ACCOUNT_ID, accountID);
    orgProperties.addProperty(ACCOUNT_NAME, accountName);
    orgProperties.addProperty(ACCOUNT_TYPE, accountType);
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
        avatarObject.addProperty(SnapConstants.IMAGE_DATA_KEY, getUserAvatar(sfId));
        profileObject.add(SnapConstants.AVATAR_KEY, avatarObject);
      }
    }
  }

  /**
   * Gets the user avatar data
   * 
   * @param sfId SFID
   * @return image data as string
   */
  private String getUserAvatar(String sfId) {
    String cacheKey = String.format("avatar_%s", sfId);

    return serviceCacheMgr.get(CacheBucketName.OBJECT_VALUE.name(), cacheKey, (key) -> {
      ProfilePhoto content = getProfilePhoto(sfId);
      String encodedPhoto = "";
      String extension = "";
      if (content != null) {
        encodedPhoto = content.getBase64content();
        extension = content.getFileNameWithExtension();
      }
      try {
        String[] extensionSplit = extension.split("\\.");
        if (extensionSplit.length > 0) {
          extension = extensionSplit[extensionSplit.length - 1];
        } else {
          logger.error("No extension found in the data");
        }
      } catch (ArrayIndexOutOfBoundsException | PatternSyntaxException e) {
        logger.error("An exception occurred" + e.getMessage());
      }
      if (StringUtils.isNotBlank(extension) && StringUtils.isNotBlank(encodedPhoto)) {
        return "data:image/" + extension + ";base64," + encodedPhoto;
      } else {
        logger.error("getUserAvatar method returns null.");
        return "";
      }
    });
  }
}