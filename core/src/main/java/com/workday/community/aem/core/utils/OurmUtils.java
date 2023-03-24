package com.workday.community.aem.core.utils;

import com.workday.community.aem.core.constants.SnapConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;

/**
 * The Utility class for all OURM related Utility APIs
 */
public class OurmUtils {
  private final static Logger logger = LoggerFactory.getLogger(OurmUtils.class);

  /**
   * Get the Salesforce id.
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
          throw new RuntimeException("User is not in userManager");
        }

        sfId = user.getProperty(SnapConstants.PROFILE_SOURCE_ID) != null ?
            user.getProperty(SnapConstants.PROFILE_SOURCE_ID)[0].getString() : null;
      } catch (Exception e) {
        logger.error(String.format("getSalesForceId fails with error: %s", e.getMessage()));
      }
    }

    if (StringUtils.isBlank(sfId)) {
      // Default fallback
      logger.debug("Salesforce Id for current user is unavailable, please check with admin.");
      sfId = DEFAULT_SFID_MASTER;
    }

    return sfId;
  }
}
