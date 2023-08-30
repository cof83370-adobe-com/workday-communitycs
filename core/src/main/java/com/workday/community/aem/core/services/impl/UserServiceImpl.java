package com.workday.community.aem.core.services.impl;

import java.util.Objects;

import javax.jcr.*;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.UUIDUtil;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.services.UserService;

import static com.workday.community.aem.core.constants.GlobalConstants.OKTA_USER_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE;

/**
 * The Class UserServiceImpl.
 */
@Component(
    service = UserService.class,
    immediate = true
)
public class UserServiceImpl implements UserService {

    /** The logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    /** The cache manager */
  @Reference
  CacheManagerService cacheManager;

  @Reference
  SearchApiConfigService searchConfigService;

  @Reference
  RunModeConfigService runModeConfigService;

  @Reference
  SnapService snapService;

  @Override
  public User getCurrentUser(SlingHttpServletRequest request) throws CacheException {
    Session session = Objects.requireNonNull(request.getResourceResolver()).adaptTo(Session.class);
    ResourceResolver serviceResolver = cacheManager.getServiceResolver(WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
    return getUser(serviceResolver, Objects.requireNonNull(session).getUserID());
  }

  @Override
  public User getUser(String serviceUserId, String userSessionId) throws CacheException {
    ResourceResolver resourceResolver = cacheManager.getServiceResolver(serviceUserId);
    return getUser(resourceResolver, userSessionId);
  }

  @Override
  public String getUserUUID(String sfId) {
    String cacheKey = String.format("user_client_id_%s", sfId);
    return cacheManager.get(CacheBucketName.UUID_VALUE.name(), cacheKey, (key) -> {
      String email = OurmUtils.getUserEmail(sfId, searchConfigService, snapService);
      return UUIDUtil.getUserClientId(email).toString();
    });
  }

  @Override
  public void invalidCurrentUser(SlingHttpServletRequest request, boolean isPath) throws CacheException {
    ResourceResolver resourceResolver = request.getResourceResolver();
    Session session = resourceResolver.adaptTo(Session.class);
    // Delete user on publish instance.
    if (session != null) {
      String ins = runModeConfigService.getInstance();

      if (ins != null && ins.equals(GlobalConstants.PUBLISH)) {
        String userId = session.getUserID();

        ResourceResolver serviceResolver =
            cacheManager.getServiceResolver(WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);

        LOGGER.info("Start to delete user with param {}.", userId);
        UserManager userManager = serviceResolver.adaptTo(UserManager.class);
        User user;
        try {
          if (isPath) {
            user = (User) Objects.requireNonNull(userManager).getAuthorizableByPath(userId);
          } else {
            user = (User) Objects.requireNonNull(userManager).getAuthorizable(userId);
          }

          if (user != null) {
            String path = user.getPath();

            if (path.contains(OKTA_USER_PATH)) {
              user.remove();
              session.logout();
            } else {
              LOGGER.error("User with param {} cannot be deleted.", userId);
            }
          } else {
            LOGGER.error("Cannot find user with param {}.", userId);
          }
          Objects.requireNonNull(session).save();
        } catch (RepositoryException e) {
          LOGGER.error("invalidate current user session failed.");
        }
      }
    }
  }

  /**
   * Used in testing only.
   * @param cacheManager the pass-in CacheManager object.
   */
  protected void setCacheManager(CacheManagerService cacheManager) {
    this.cacheManager = cacheManager;
  }

  private User getUser(final ResourceResolver resourceResolver, String userSessionId) {
    if (userSessionId == null) return null;

    String cacheKey = String.format("sessionUser-%s", userSessionId);
    User retUser = cacheManager.get(CacheBucketName.JCR_USER.name(), cacheKey, (key) -> {
      try {
        UserManager userManager = resourceResolver.adaptTo(UserManager.class);
        User user = (User) Objects.requireNonNull(userManager).getAuthorizable(userSessionId);
        if (user != null) {
          return user;
        }
        LOGGER.error("Cannot find user with id {}.", userSessionId);
        return null;
      } catch (RepositoryException e) {
        LOGGER.error("Exception occurred when fetch user {}: {}.", userSessionId, e.getMessage());
        return null;
      }
    });

    try {
      if (retUser.isDisabled()) {
        cacheManager.invalidateCache(CacheBucketName.JCR_USER.name(), cacheKey);
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

    return retUser;
  }
}
