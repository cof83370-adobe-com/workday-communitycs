package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNTID;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_ID;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ADOBE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_NUMBER;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_ROLE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.IS_NSC;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ORG;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.PAGE_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.TIMEZONE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.USER;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTEXT_INFO_KEY;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.constants.DrupalConstants;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.cache.LruCacheWithTimeout;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OSGi service implementation for Drupal APIs.
 */
@Component(service = DrupalService.class, property = {
    "service.pid=aem.core.services.drupalservice"
}, configurationPid = "com.workday.community.aem.core.config.DrupalConfig", immediate = true)
@Designate(ocd = DrupalConfig.class)
public class DrupalServiceImpl implements DrupalService {

  /**
   * The logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DrupalServiceImpl.class);

  /**
   * The drupal Config.
   */
  private DrupalConfig config;

  /**
   * The gson service.
   */
  private final Gson gson = new Gson();

  /**
   * LRU Cache for storing token value.
   */
  private LruCacheWithTimeout<String, String> drupalApiCache;

  /**
   * Cache manager service.
   */
  @Reference
  private CacheManagerService serviceCacheMgr;

  /**
   * The Run-mode configuration service.
   */
  @Reference
  private RunModeConfigService runModeConfigService;

  /**
   * This is used in testing.
   *
   * @param serviceCacheMgr the pass-in Cache manager object.
   */
  public void setServiceCacheMgr(CacheManagerService serviceCacheMgr) {
    this.serviceCacheMgr = serviceCacheMgr;
  }

  /**
   * This is used in testing.
   *
   * @param runModeConfigService the pass-in run mode config service object.
   */
  public void setRunModeConfigService(RunModeConfigService runModeConfigService) {
    this.runModeConfigService = runModeConfigService;
  }

  /**
   * Activates the Drupal Service class.
   */
  @Activate
  @Modified
  @Override
  public void activate(DrupalConfig config) {
    this.config = config;
    this.drupalApiCache = new LruCacheWithTimeout<>(config.drupalTokenCacheMax(), config.drupalTokenCacheTimeout());
    LOGGER.info("DrupalService is activated.");
  }

  /**
   * Makes Drupal API call and fetches the user data for logged in user.
   */
  @Override
  public String getUserData(String sfId) {
    String userDataCacheKey = String.format("user_data_%s_%s", getEnv(), sfId);
    if (!config.enableCache()) {
      serviceCacheMgr.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), userDataCacheKey);
    }
    String retValue = serviceCacheMgr.get(CacheBucketName.OBJECT_VALUE.name(), userDataCacheKey, () -> {
      try {
        if (StringUtils.isNotBlank(sfId)) {
          String drupalUrl = config.drupalApiUrl();
          String userDataPath = config.drupalUserDataPath();
          // Get the bearer token needed for user data API call.
          String bearerToken = getApiToken();
          if (StringUtils.isNotBlank(bearerToken)) {
            // Frame the request URL.
            String url = CommunityUtils.formUrl(drupalUrl, userDataPath);
            // Format the URL.
            url = String.format(url, sfId);
            // Execute the request.
            ApiResponse userDataResponse = RestApiUtil.doDrupalUserDataGet(url, bearerToken);
            if (userDataResponse == null || StringUtils.isEmpty(userDataResponse.getResponseBody())
                || userDataResponse.getResponseCode() != HttpStatus.SC_OK) {
              LOGGER.error("Drupal API user data response is empty.");
              return StringUtils.EMPTY;
            }

            return userDataResponse.getResponseBody();
          }
        }
        return StringUtils.EMPTY;
      } catch (DrupalException e) {
        LOGGER.error(
            String.format(
                "There is an error while fetching the user data. Please contact Community Admin. %s",
                e.getMessage()));
        return null;
      }
    });
    if (StringUtils.isEmpty(retValue)) {
      serviceCacheMgr.invalidateCache(CacheBucketName.OBJECT_VALUE.name(), userDataCacheKey);
    }
    return retValue;
  }

  /**
   * Gets the Drupal API Bearer Token required for user data API.
   */
  @Override
  public String getApiToken() throws DrupalException {
    String cachedResult = drupalApiCache.get(DrupalConstants.TOKEN_CACHE_KEY);
    if (StringUtils.isNotBlank(cachedResult)) {
      return cachedResult;
    }
    String drupalUrl = config.drupalApiUrl();
    String tokenPath = config.drupalTokenPath();
    String clientId = config.drupalApiClientId();
    String clientSecret = config.drupalApiClientSecret();

    if (StringUtils.isEmpty(drupalUrl) || StringUtils.isEmpty(tokenPath)
        || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
      // No Drupal configuration provided, just return the default one.
      LOGGER.debug(String.format("There is no value "
              + "for one or multiple configuration parameters: "
              + "drupalUrl=%s;tokenPath=%s;clientId=%s;clientSecret=%s",
          drupalUrl, tokenPath, clientId, clientSecret));
      return StringUtils.EMPTY;
    }

    try {
      String url = CommunityUtils.formUrl(drupalUrl, tokenPath);

      // Execute the request.
      ApiResponse drupalResponse = RestApiUtil.doDrupalTokenGet(url, clientId, clientSecret);
      if (drupalResponse == null || StringUtils.isEmpty(drupalResponse.getResponseBody())
          || drupalResponse.getResponseCode() != HttpStatus.SC_OK) {
        LOGGER.error("Drupal API token response is empty.");
        return StringUtils.EMPTY;
      }

      // Gson object for json handling of token response.
      JsonObject tokenResponse = gson.fromJson(drupalResponse.getResponseBody(), JsonObject.class);
      if (tokenResponse.get("access_token") == null || tokenResponse.get("access_token").isJsonNull()) {
        LOGGER.error("Drupal API token is empty.");
        return StringUtils.EMPTY;
      }

      // Update the cache with the bearer token.
      String bearerToken = tokenResponse.get("access_token").getAsString();
      drupalApiCache.put(DrupalConstants.TOKEN_CACHE_KEY, bearerToken);
      return bearerToken;
    } catch (DrupalException e) {
      throw new DrupalException(
          String.format(
              "Error while fetching the Drupal Api token. Please contact Community Admin. Error: %s",
              e.getMessage()));
    }
  }

  /**
   * Gets the user profile image data from drupal user data API.
   *
   * @param sfId SFID
   * @return image data as string
   */
  @Override
  public String getUserProfileImage(String sfId) {
    try {
      String userData = this.getUserData(sfId);
      if (StringUtils.isEmpty(userData)) {
        LOGGER.error("Error in getUserProfileImage method - empty user data response.");
        return StringUtils.EMPTY;
      }
      JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);
      JsonElement profileImageElement = userDataObject.get("profileImage");
      return (profileImageElement == null || profileImageElement.isJsonNull()) ? ""
          : profileImageElement.getAsString();
    } catch (JsonSyntaxException e) {
      LOGGER.error("Error in getUserProfileImage method, {} ", e.getMessage());
      return StringUtils.EMPTY;
    }
  }

  /**
   * Gets the adobe data to be set on digital data object on frontend.
   *
   * @param sfId        SFID.
   * @param pageTitle   Page title.
   * @param contentType Content type.
   * @return Adobe data.
   */
  @Override
  public String getAdobeDigitalData(String sfId, String pageTitle, String contentType) {
    try {
      JsonObject digitalData = new JsonObject();
      String userData = getUserData(sfId);
      if (StringUtils.isEmpty(userData)) {
        LOGGER.error("Error in getAdobeDigitalData method - empty user data response.");
      } else {
        digitalData = generateAdobeDigitalData(userData);
      }
      JsonObject pageProperties = new JsonObject();
      pageProperties.addProperty(CONTENT_TYPE, contentType);
      pageProperties.addProperty(PAGE_NAME, pageTitle);
      digitalData.add("page", pageProperties);
      return String.format("{\"%s\":%s}", "digitalData", gson.toJson(digitalData));
    } catch (JsonSyntaxException e) {
      LOGGER.error("Error in getAdobeDigitalData method, {} ", e.getMessage());
      return StringUtils.EMPTY;
    }
  }

  /**
   * Generate adobe digital data.
   *
   * @param userData The drupal user data api response as string.
   * @return The digital data.
   */
  private JsonObject generateAdobeDigitalData(String userData) {

    JsonObject userProperties = new JsonObject();

    String contactRole = StringUtils.EMPTY;
    String contactNumber = StringUtils.EMPTY;
    String accountId = StringUtils.EMPTY;
    String accountName = StringUtils.EMPTY;
    String accountType = StringUtils.EMPTY;
    boolean isNsc = false;
    String timeZoneStr = StringUtils.EMPTY;
    if (StringUtils.isNotBlank(userData)) {
      try {
        JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);
        JsonObject adobeObject = userDataObject.getAsJsonObject(ADOBE);

        // Process user data
        JsonObject userObject = adobeObject.getAsJsonObject(USER);
        JsonArray contactRoleElement = userObject.get(CONTACT_ROLE).getAsJsonArray();
        contactRole = (contactRoleElement == null || contactRoleElement.isJsonNull()
            || contactRoleElement.isEmpty())
            ? StringUtils.EMPTY
            : StringUtils.join(contactRoleElement, ",");
        JsonElement contactNumberElement = userObject.get(CONTACT_NUMBER);
        contactNumber = (contactRoleElement == null || contactNumberElement.isJsonNull()) ? StringUtils.EMPTY
            : contactNumberElement.getAsString();
        JsonElement isNscElement = userObject.get(IS_NSC);
        isNsc = isNscElement != null && !isNscElement.isJsonNull() && isNscElement.getAsBoolean();
        JsonElement timeZoneElement = userObject.get(TIMEZONE);
        timeZoneStr = (timeZoneElement == null || timeZoneElement.isJsonNull()) ? StringUtils.EMPTY
            : timeZoneElement.getAsString();

        // Process org data
        JsonObject orgObject = adobeObject.getAsJsonObject(ORG);
        JsonElement accountIdElement = orgObject.get(ACCOUNTID);
        accountId = (accountIdElement == null || accountIdElement.isJsonNull()) ? StringUtils.EMPTY
            : accountIdElement.getAsString();
        JsonElement accountNameElement = orgObject.get(ACCOUNT_NAME);
        accountName = (accountNameElement == null || accountNameElement.isJsonNull()) ? StringUtils.EMPTY
            : accountNameElement.getAsString();
        JsonElement accountTypeElement = orgObject.get(ACCOUNT_TYPE);
        accountType = (accountTypeElement == null || accountTypeElement.isJsonNull()) ? StringUtils.EMPTY
            : accountTypeElement.getAsString();
      } catch (JsonSyntaxException e) {
        LOGGER.error("Error in generateAdobeDigitalData method :: {}",
            e.getMessage());
      }
    }
    userProperties.addProperty(CONTACT_ROLE, contactRole);
    userProperties.addProperty(CONTACT_NUMBER, contactNumber);
    userProperties.addProperty(IS_NSC, isNsc);
    userProperties.addProperty(TIMEZONE, timeZoneStr);
    JsonObject orgProperties = new JsonObject();
    orgProperties.addProperty(ACCOUNT_ID, accountId);
    orgProperties.addProperty(ACCOUNT_NAME, accountName);
    orgProperties.addProperty(ACCOUNT_TYPE, accountType);
    JsonObject digitalData = new JsonObject();
    digitalData.add(USER, userProperties);
    digitalData.add(ORG, orgProperties);

    return digitalData;
  }

  /**
   * Gets the user timezone from drupal user data API.
   *
   * @param sfId SFID
   * @return image data as string
   */
  @Override
  public String getUserTimezone(String sfId) {
    try {
      String userData = this.getUserData(sfId);
      if (StringUtils.isEmpty(userData)) {
        LOGGER.error("Error in getUserTimezone method - empty user data response.");
        return StringUtils.EMPTY;
      }
      if (StringUtils.isNotBlank(userData)) {
        JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);
        JsonObject adobeObject = userDataObject.getAsJsonObject(ADOBE);
        // Process user data
        JsonObject userObject = adobeObject.getAsJsonObject(USER);
        JsonElement timeZoneElement = userObject.get(TIMEZONE);
        return (timeZoneElement == null || timeZoneElement.isJsonNull()) ? StringUtils.EMPTY
            : timeZoneElement.getAsString();
      }
    } catch (JsonSyntaxException e) {
      LOGGER.error("Error in getUserTimezone method, {} ", e.getMessage());
    }
    return StringUtils.EMPTY;
  }

  /**
   * Gets the user context from drupal user data API.
   *
   * @param sfId SFID
   * @return User context json object
   */
  @Override
  public JsonObject getUserContext(String sfId) {
    try {
      String userData = this.getUserData(sfId);
      if (StringUtils.isNotBlank(userData)) {
        JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);
        JsonArray context = userDataObject.getAsJsonArray(USER_CONTEXT_INFO_KEY);
        if (context == null || context.isJsonNull()
            || context.isEmpty()) {
          return new JsonObject();
        }
        return context.get(0).getAsJsonObject();
      }
    } catch (JsonSyntaxException e) {
      LOGGER.error("Error in getUserContext method, {} ", e.getMessage());
    }
    return new JsonObject();
  }

  /**
   * Search ourm user list.
   *
   * @param searchText the search text
   * @return the json object
   * @throws DrupalException drupal exception
   */
  @Override
  public JsonObject searchOurmUserList(String searchText) throws DrupalException {
    try {
      if (StringUtils.isNotBlank(searchText)) {
        String drupalUrl = config.drupalApiUrl();
        String userSearchPath = config.drupalUserSearchPath();
        // Get the bearer token needed for user data API call.
        String bearerToken = getApiToken();
        if (StringUtils.isNotBlank(bearerToken)) {
          // Frame the request URL.
          String url = CommunityUtils.formUrl(drupalUrl, userSearchPath);
          // Format the URL.
          url = String.format(url, searchText);
          // Execute the request.
          ApiResponse userSearchResponse = RestApiUtil.doDrupalUserSearchGet(url, bearerToken);
          if (StringUtils.isEmpty(userSearchResponse.getResponseBody())
              || userSearchResponse.getResponseCode() != HttpStatus.SC_OK) {
            LOGGER.error("Drupal API user search response is empty.");
            return new JsonObject();
          }

          return gson.fromJson(userSearchResponse.getResponseBody(), JsonObject.class);
        }
      }
      return new JsonObject();
    } catch (DrupalException e) {
      throw new DrupalException(
          String.format(
              "There is an error while fetching user search data. Please contact Community Admin. %s",
              e.getMessage()));
    }
  }

  /**
   * Returns the environment name.
   *
   * @return Environment name.
   */
  private String getEnv() {
    String env = this.runModeConfigService.getEnv();
    return (env == null) ? "local" : env;
  }
}
