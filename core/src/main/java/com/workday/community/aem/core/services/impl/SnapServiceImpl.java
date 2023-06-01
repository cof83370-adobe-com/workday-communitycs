package com.workday.community.aem.core.services.impl;

import com.adobe.xfa.ut.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.LRUCacheWithTimeout;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import org.apache.sling.api.resource.LoginException;
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

import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.PAGE_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_NUMBER;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_ROLE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.IS_NSC;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.NSC;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_ID;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_TYPE;

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

  private JsonObject defaultMenu;

  private LRUCacheWithTimeout<String, String> snapCache;

  /** The resource resolver factory. */
  @Reference
  ResourceResolverFactory resResolverFactory;

  /** The snap Config. */
  private SnapConfig config;

  /** The ObjectMapper service. */
  private ObjectMapper objectMapper;

  /** The gson service. */
  private final Gson gson = new Gson();

  @Activate
  @Modified
  @Override
  public void activate(SnapConfig config) {
    this.config = config;
    this.snapCache = new LRUCacheWithTimeout<>(config.menuCacheMax(), config.menuCacheTimeout());
    this.objectMapper = new ObjectMapper();
    logger.info("SnapService is activated.");
  }

  @Override
  public void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
    this.resResolverFactory = resourceResolverFactory;
  }

  @Override
  public String getUserHeaderMenu(String sfId) {
    String cacheKey = String.format("menu_%s", sfId);
    String cachedResult = snapCache.get(cacheKey);
    if (cachedResult != null) {
      return cachedResult;
    }

    String snapUrl = config.snapUrl(), navApi = config.navApi(),
        apiToken = config.navApiToken(), apiKey = config.navApiKey();

    if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(navApi) ||
        StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
      // No Snap configuration provided, just return the default one.
      return gson.toJson(this.getDefaultHeaderMenu());
    }

    try {
      String url = CommunityUtils.formUrl(snapUrl, navApi);
      url = String.format(url, sfId);

      String traceId = "Community AEM-" + new Date().getTime();
      // Execute the request.
      APIResponse snapRes = RestApiUtil.doGetMenu(url, apiToken, apiKey, traceId);
      JsonObject defaultMenu = this.getDefaultHeaderMenu();
      if (snapRes == null || StringUtils.isEmpty(snapRes.getResponseBody())
          || snapRes.getResponseCode() != HttpStatus.SC_OK) {
        logger.error("Sfdc menu fetch is empty, fallback to use local default");
        return gson.toJson(defaultMenu);
      }

      // Gson object for json handling.
      JsonObject sfMenu = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);
      // Need to make merge sfMenu with local cache with beta experience.
      if (config.beta()) {
        String finalMenu = this.getMergedHeaderMenu(sfMenu, defaultMenu);
        snapCache.put(cacheKey, finalMenu);
        return finalMenu;
      }

      // Non-Beta will directly return the sf menu
      return gson.toJson(sfMenu);

    } catch (SnapException | JsonSyntaxException e) {
      logger.error("Error in getNavUserData method call :: {}", e.getMessage());
    }

    return gson.toJson(this.getDefaultHeaderMenu());
  }

  @Override
  public JsonObject getUserContext(String sfId) {
    try {
      logger.debug("SnapImpl: Calling SNAP getUserContext()...");
      String url = CommunityUtils.formUrl(config.snapUrl(), config.snapContextPath());
      if (url == null) {
        return new JsonObject();
      }

      url = String.format(url, sfId);
      String jsonResponse = RestApiUtil.doSnapGet(url, config.snapContextApiToken(), config.snapContextApiKey());
      return gson.fromJson(jsonResponse, JsonObject.class);
    } catch (SnapException | JsonSyntaxException e) {
      logger.error("Error in getUserContext method :: {}", e.getMessage());
    }

    logger.error("User context is not fetched from the snap context API call without error, please contact admin.");
    return new JsonObject();
  }

  @Override
  public ProfilePhoto getProfilePhoto(String userId) {
    String snapUrl = config.snapUrl(), avatarUrl = config.sfdcUserAvatarUrl();

    String url = CommunityUtils.formUrl(snapUrl, avatarUrl);
    url = String.format(url, userId);

    try {
      logger.info("SnapImpl: Calling SNAP getProfilePhoto(), url is {}", url);
      String jsonResponse = RestApiUtil.doSnapGet(url, config.sfdcUserAvatarToken(), config.sfdcUserAvatarApiKey());
      if (jsonResponse != null) {
        return objectMapper.readValue(jsonResponse, ProfilePhoto.class);
      }
    } catch (SnapException | JsonProcessingException e) {
      logger.error("Error in getProfilePhoto method, {} ", e.getMessage());
    }
    return null;
  }

  /**
   * Get default header menu.
   *
   * @return The menu.
   */
  private JsonObject getDefaultHeaderMenu() {
    if (defaultMenu != null) {
      return defaultMenu;
    }

    // Reading the JSON File from DAM
    try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resResolverFactory,
        config.navFallbackMenuServiceUser())) {
      defaultMenu = DamUtils.readJsonFromDam(resourceResolver, config.navFallbackMenuData());
    } catch (RuntimeException | LoginException e) {
      logger
          .error(String.format("Exception in SnaServiceImpl while getFailStateHeaderMenu, error: %s", e.getMessage()));
      return new JsonObject();
    }

    return defaultMenu;
  }

  /**
   * Get merged header menu.
   * 
   * @param sfNavObj The json object of nav.
   * @return The menu.
   */
  private String getMergedHeaderMenu(JsonObject sfNavObj, JsonObject defaultMenu) {
    if (sfNavObj != null && config.beta()) {
      CommonUtils.updateSourceFromTarget(sfNavObj, defaultMenu, "id");
      return gson.toJson(sfNavObj);
    }

    return gson.toJson(sfNavObj);
  }

  @Override
  public String getUserProfile(String sfId) {
    String cacheKey = String.format("profile_%s", sfId);
    String cachedResult = snapCache.get(cacheKey);
    if (cachedResult != null) {
      return cachedResult;
    }
    try {
      String url = CommunityUtils.formUrl(config.snapUrl(), config.snapProfilePath());
      url = String.format(url, sfId);
      String jsonResponse = RestApiUtil.doSnapGet(url, config.snapProfileApiToken(), config.snapProfileApiKey());
      snapCache.put(cacheKey, jsonResponse);
      return jsonResponse;
    } catch (SnapException | JsonSyntaxException e) {
      logger.error("Error in getUserProfile method :: {}", e.getMessage());
    }

    logger
        .error("User profile data is not fetched from the snap profile API call without error, please contact admin.");
    return null;
  }

  @Override
  public String getAdobeDigitalData(String sfId, String pageTitle, String contentType) {
    String cacheKey = String.format("adobeAnalytics_%s", sfId);
    String cachedResult = snapCache.get(cacheKey);
    JsonObject digitalData;
    if (cachedResult != null) {
      digitalData = gson.fromJson(cachedResult, JsonObject.class);
    } else {
      String profileData = getUserProfile(sfId);
      digitalData = generateAdobeDigitalData(profileData);
      snapCache.put(cacheKey, gson.toJson(digitalData));
    }
    JsonObject pageProperties = new JsonObject();
    pageProperties.addProperty(CONTENT_TYPE, contentType);
    pageProperties.addProperty(PAGE_NAME, pageTitle);
    digitalData.add("page", pageProperties);
    return String.format("{\"%s\":%s}", "digitalData", gson.toJson(digitalData));
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
        boolean isWorkdayMate = isWorkmateElement != null && !isWorkmateElement.isJsonNull()
            && isWorkmateElement.getAsBoolean();
        JsonElement typeElement = profileObject.get("type");
        accountType = isWorkdayMate ? "workday"
            : (typeElement == null || typeElement.isJsonNull() ? "" : typeElement.getAsString().toLowerCase());
      } catch (JsonSyntaxException e) {
        logger.error("Error in generateAdobeDigitalData method :: {}",
            e.getMessage());
      }
    }
    userProperties.addProperty(CONTACT_ROLE, contactRole);
    userProperties.addProperty(CONTACT_NUMBER, contactNumber);
    userProperties.addProperty(IS_NSC, isNSC);
    orgProperties.addProperty(ACCOUNT_ID, accountID);
    orgProperties.addProperty(ACCOUNT_NAME, accountName);
    orgProperties.addProperty(ACCOUNT_TYPE, accountType);
    digitalData.add("user", userProperties);
    digitalData.add("org", orgProperties);

    return digitalData;
  }
}
