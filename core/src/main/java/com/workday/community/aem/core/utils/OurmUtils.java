package com.workday.community.aem.core.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.OurmException;

import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
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
   * @param request
   * @param userService
   * @return
   */
  public static String getSalesForceId(SlingHttpServletRequest request, UserService userService) {
    String sfId = "";
    try {
      User user = userService.getCurrentUser(request);
      if (user == null) {
        throw new OurmException("User is not in userManager.");
      }

      Value[] sfIdObj = user.getProperty(SnapConstants.PROFILE_SOURCE_ID);
      if (sfIdObj == null || sfIdObj.length == 0) {
        LOGGER.error("Returned User object in JCR session doesn't have salesforceId");
        return DEFAULT_SFID_MASTER;
      }

      sfId = sfIdObj[0].getString();
    } catch (RepositoryException | RuntimeException | OurmException | CacheException e) {
      LOGGER.error(String.format("getSalesForceId fails with error: %s.", e.getMessage()));
    }

    if (StringUtils.isEmpty(sfId)) {
      // Default fallback
      LOGGER.debug("Salesforce Id for current user is unavailable, please check with admin.");
      sfId = DEFAULT_SFID_MASTER;
    }

    return sfId;
  }

  /**
   * Return user's email given user's salesforceId.
   *
   * @param sfId                   User's Salesforce id.
   * @param searchApiConfigService Pass-in SearchApiConfigService object.
   * @param snapService            Pass-in snapService object
   * @return the user's email address.
   */
  public static String getUserEmail(
      String sfId, SearchApiConfigService searchApiConfigService, SnapService snapService
  ) {
    boolean isDevMode = searchApiConfigService.isDevMode();
    JsonObject userContext = snapService.getUserContext(sfId);
    return userContext.has(EMAIL_NAME) ? userContext.get(EMAIL_NAME).getAsString()
        : (isDevMode ? searchApiConfigService.getDefaultEmail() : null);
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
