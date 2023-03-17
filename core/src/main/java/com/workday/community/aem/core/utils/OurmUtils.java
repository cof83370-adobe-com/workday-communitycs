package com.workday.community.aem.core.utils;

import com.workday.community.aem.core.constants.GlobalConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;

import static com.workday.community.aem.core.constants.GlobalConstants.WRCConstants.DEFAULT_SFID_MASTER;

public class OurmUtils {
  private final static Logger logger = LoggerFactory.getLogger(OurmUtils.class);

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

        sfId = user.getProperty(GlobalConstants.WRCConstants.PROFILE_SOURCE_ID) != null ?
            user.getProperty(GlobalConstants.WRCConstants.PROFILE_SOURCE_ID)[0].getString() : null;
      } catch (Exception e) {
        logger.error(String.format("Exception in init HeaderModelImpl method: %s", e.getMessage()));
      }
    }

    if (StringUtils.isBlank(sfId)) {
      // Default fallback
      logger.debug("Salesforce Id for current user is unavailable");
      sfId = DEFAULT_SFID_MASTER;
    }

    return sfId;
  }
}
