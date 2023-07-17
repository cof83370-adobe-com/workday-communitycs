package com.workday.community.aem.core.utils;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.OurmException;

import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;

/**
 * The Utility class for all OURM related Utility APIs
 */
public class OurmUtils {
  private final static Logger LOGGER = LoggerFactory.getLogger(OurmUtils.class);

  /**
   * Get the user's Salesforce id.
   *
   * @param resourceResolver the Resource Resolver object
   * @return the Salesforce id from the session.
   */
  public static String getSalesForceId(ResourceResolver resourceResolver) {
    String sfId = "";

    Session session = resourceResolver.adaptTo(Session.class);
    UserManager userManager = resourceResolver.adaptTo(UserManager.class);

    if (userManager != null && session != null) {
      try {
        User user = (User) userManager.getAuthorizable(session.getUserID());
        if (user == null) {
          LOGGER.error("User is not in userManager");
          throw new OurmException("User is not in userManager.");
        }

        Value[] sfIdObj = user.getProperty(SnapConstants.PROFILE_SOURCE_ID);
        if (sfIdObj == null || sfIdObj.length == 0) {
          LOGGER.error("Returned User object in JCR session doesn't have salesforceId");
          return DEFAULT_SFID_MASTER;
        }

        sfId = sfIdObj[0].getString();
      } catch (RepositoryException | RuntimeException | OurmException e) {
        LOGGER.error(String.format("getSalesForceId fails with error: %s.", e.getMessage()));
      }
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
   * @param sfId User's Salesforce id.
   * @param searchApiConfigService Pass-in SearchApiConfigService object.
   * @param snapService  Pass-in snapService object
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
}
