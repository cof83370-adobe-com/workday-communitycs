package com.workday.community.aem.core.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;

/**
 * The Utility class for all OURM related Utility APIs
 */
public class OurmUtils {
  private final static Logger LOGGER = LoggerFactory.getLogger(OurmUtils.class);

  /**
   * Get the current user's Salesforce id.
   *
   * @param request     The current Request object.
   * @param userService The pass-in user service object.
   * @return The Salesforce id for current logged-in user.
   */
  public static String getSalesForceId(SlingHttpServletRequest request, UserService userService) {
    String sfId = "";
    try {
      User user = userService.getCurrentUser(request);
      if (user == null) {
        LOGGER.error("User returned from userService.getCurrentUser is null");
        return DEFAULT_SFID_MASTER;
      }

      Value[] sfIdObj = user.getProperty(SnapConstants.PROFILE_SOURCE_ID);
      if (sfIdObj == null || sfIdObj.length == 0) {
        LOGGER.error("Current user have no sfId, mostly because no Okta integration, use default.");
        return DEFAULT_SFID_MASTER;
      }

      sfId = sfIdObj[0].getString();
    } catch (RepositoryException re) {
      LOGGER.error(String.format("getSalesForceId call fails with Repository Exception: %s.", re.getMessage()));
    } catch (CacheException ce) {
      LOGGER.error("userService.getCurrentUser call fails");
    } catch (RuntimeException re) {
      LOGGER.error(String.format("Runtime exception: %s.", re.getMessage()));
    }

    if (StringUtils.isEmpty(sfId)) {
      // Default fallback
      LOGGER.debug("Salesforce Id is empty, please check with admin. Use default");
      sfId = DEFAULT_SFID_MASTER;
    }

    return sfId;
  }

  /**
   * Return user's email given user's salesforceId.
   *
   * @param sfId                   User's Salesforce id.
   * @param searchApiConfigService Pass-in SearchApiConfigService object.
   * @param drupalService          Pass-in DrupalService object
   * @return the user's email address.
   */
  public static String getUserEmail(
      String sfId, SearchApiConfigService searchApiConfigService, DrupalService drupalService) {
    boolean isDevMode = searchApiConfigService.isDevMode();
    String userData;
    try {
      userData = drupalService.getUserData(sfId);
      Gson gson = new Gson();
      JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);
      return userDataObject.has(EMAIL_NAME) ? userDataObject.get(EMAIL_NAME).getAsString()
          : (isDevMode ? searchApiConfigService.getDefaultEmail() : null);
    } catch (JsonSyntaxException e) {
      LOGGER.error("Exception in getUserEmail method in OurmUtils class: {}", e.getMessage());
      return null;
    }
  }

  public static boolean isMenuEmpty(Gson gson, String menuString) {
    JsonObject retAsJsonObject = gson.fromJson(menuString, JsonObject.class).getAsJsonObject("primary");
    boolean menuEmpty = retAsJsonObject == null || retAsJsonObject.isJsonNull();
    if (!menuEmpty) {
      JsonElement primary = retAsJsonObject.get("menu");
      menuEmpty = (primary == null) || primary.isJsonNull() ||
          (primary.isJsonArray() && primary.getAsJsonArray().size() == 0);
    }

    return menuEmpty;
  }
}
