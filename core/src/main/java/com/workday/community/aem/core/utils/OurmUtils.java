package com.workday.community.aem.core.utils;

import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;

/**
 * The Utility class for all OURM related Utility APIs.
 */
@Slf4j
public class OurmUtils {

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
        log.error("User returned from userService.getCurrentUser is null");
        return DEFAULT_SFID_MASTER;
      }

      Value[] sfIdObj = user.getProperty(SnapConstants.PROFILE_SOURCE_ID);
      if (sfIdObj == null || sfIdObj.length == 0) {
        log.info("Current user with user id {} have no sfId. Possible with no okta integration", user.getID());
        return DEFAULT_SFID_MASTER;
      }

      sfId = sfIdObj[0].getString();
    } catch (RepositoryException re) {
      log.error("getSalesForceId call fails with Repository Exception: {}.", re.getMessage());
    } catch (CacheException ce) {
      log.error("userService.getCurrentUser call fails, {}", ce.getMessage());
    } catch (RuntimeException re) {
      log.error("Runtime exception: {}.", re.getMessage());
    }

    if (StringUtils.isEmpty(sfId)) {
      // Default fallback
      log.debug("Salesforce Id is empty, please check with admin. Use default");
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
      return userDataObject != null && !userDataObject.isJsonNull() && userDataObject.has(EMAIL_NAME)
          ? userDataObject.get(EMAIL_NAME).getAsString()
          : (isDevMode ? searchApiConfigService.getDefaultEmail() : null);
    } catch (JsonSyntaxException e) {
      log.error("Exception in getUserEmail method in OurmUtils class: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Whether the menu is empty.
   *
   * @param gson       The Gson object.
   * @param menuString The menu string.
   * @return True if it's empty, otherwise false.
   */
  public static boolean isMenuEmpty(Gson gson, String menuString) {
    JsonObject retAsJsonObject =
        gson.fromJson(menuString, JsonObject.class).getAsJsonObject("primary");
    boolean menuEmpty = retAsJsonObject == null || retAsJsonObject.isJsonNull();
    if (!menuEmpty) {
      JsonElement primary = retAsJsonObject.get("menu");
      menuEmpty = (primary == null) || primary.isJsonNull()
          || (primary.isJsonArray() && primary.getAsJsonArray().isEmpty());
    }

    return menuEmpty;
  }
}
