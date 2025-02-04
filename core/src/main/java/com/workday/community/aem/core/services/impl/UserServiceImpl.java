package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.OKTA_USER_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.SERVICE_USER_GROUP;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.UuidUtil;
import java.util.Objects;
import java.util.UUID;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class UserServiceImpl.
 */
@Slf4j
@Component(service = UserService.class, immediate = true)
public class UserServiceImpl implements UserService {

  /**
   * The cache manager.
   */
  @Reference
  private CacheManagerService cacheManager;

  @Reference
  private SearchApiConfigService searchConfigService;

  @Reference
  private RunModeConfigService runModeConfigService;

  @Reference
  private DrupalService drupalService;

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized User getCurrentUser(SlingHttpServletRequest request) throws CacheException {
    ResourceResolver resourceResolver = request.getResourceResolver();
    Session session = Objects.requireNonNull(resourceResolver).adaptTo(Session.class);
    String userSessionId = Objects.requireNonNull(session).getUserID();
    if (StringUtils.isEmpty(userSessionId)) {
      return null;
    }

    UserManager userManager = Objects.requireNonNull(resourceResolver.adaptTo(UserManager.class));
    try {
      User user = (User) userManager.getAuthorizable(userSessionId);
      if (user != null && !(UserConstants.DEFAULT_ANONYMOUS_ID).equals(userSessionId)) {
        return user;
      }
      log.error("Cannot find logged in user with id {}.", userSessionId);
      return null;
    } catch (RepositoryException e) {
      log.error("Exception occurred when fetch user with Id {}: msg: {}.", userSessionId, e.getMessage());
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUserUuid(String sfId) {
    if (StringUtils.isEmpty(sfId)) {
      return "";
    }

    String cacheKey = String.format("user_uuid_%s", sfId);
    String ret = cacheManager.get(CacheBucketName.UUID_VALUE.name(), cacheKey, () -> {
      String email = OurmUtils.getUserEmail(sfId, searchConfigService, drupalService);
      UUID uuid = UuidUtil.getUserClientId(email);
      return uuid == null ? null : uuid.toString();
    });

    return (ret == null) ? "" : ret;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invalidCurrentUser(SlingHttpServletRequest request, boolean isPath)
      throws CacheException {
    ResourceResolver resourceResolver = request.getResourceResolver();
    Session session = resourceResolver.adaptTo(Session.class);
    if (session == null || !session.isLive()) {
      return;
    }

    // Delete user on publish instance.
    String ins = runModeConfigService.getInstance();

    if (ins != null && ins.equals(GlobalConstants.PUBLISH)) {
      String userId = session.getUserID();
      ResourceResolver serviceResolver = cacheManager.getServiceResolver(SERVICE_USER_GROUP);

      log.debug("Start to delete user with param {}.", userId);
      UserManager userManager = Objects.requireNonNull(serviceResolver.adaptTo(UserManager.class));
      User user;
      try {
        user = isPath
            ? (User) userManager.getAuthorizableByPath(userId)
            : (User) userManager.getAuthorizable(userId);

        if (user != null) {
          String path = user.getPath();
          if (path.contains(OKTA_USER_PATH)) {
            user.remove();
            session.logout();
          } else {
            log.error("User with userID {} cannot be deleted.", userId);
          }
        } else {
          log.error("Cannot find user with userID {}.", userId);
        }
        session.save();
      } catch (RepositoryException e) {
        log.error("invalidate current user session failed. {}", e.getMessage());
      } finally {
        if (resourceResolver.isLive()) {
          resourceResolver.close();
        }

        if (session.isLive()) {
          session.logout();
        }
      }
    }
  }

  /**
   * Used in testing only.
   *
   * @param cacheManager the pass-in CacheManager object.
   */
  protected void setCacheManager(CacheManagerService cacheManager) {
    this.cacheManager = cacheManager;
  }
}
