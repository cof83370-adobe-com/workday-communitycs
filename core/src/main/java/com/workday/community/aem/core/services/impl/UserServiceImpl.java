package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.OKTA_USER_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.SERVICE_USER_GROUP;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.UuidUtil;
import java.util.Objects;
import java.util.UUID;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class UserServiceImpl.
 */
@Slf4j
@Component(
    service = UserService.class,
    immediate = true
)
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
  private SnapService snapService;

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized User getCurrentUser(SlingHttpServletRequest request) throws CacheException {
    Session session = Objects.requireNonNull(request.getResourceResolver()).adaptTo(Session.class);
    ResourceResolver serviceResolver = cacheManager.getServiceResolver(SERVICE_USER_GROUP);
    return getUser(serviceResolver, Objects.requireNonNull(session).getUserID());
  }

  /**
   * {@inheritDoc}
   */
  private User getUser(final ResourceResolver resourceResolver, String userSessionId) {
    if (userSessionId == null) {
      return null;
    }

    UserManager userManager = Objects.requireNonNull(resourceResolver.adaptTo(UserManager.class));
    try {
      User user = (User) userManager.getAuthorizable(userSessionId);
      if (user != null) {
        return user;
      }
      log.error("Cannot find user with id {}.", userSessionId);
      return null;
    } catch (RepositoryException e) {
      log.error("Exception occurred when fetch user {}: {}.", userSessionId, e.getMessage());
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUserUuid(String sfId) {
    String cacheKey = String.format("user_uuid_%s", sfId);
    String ret = cacheManager.get(CacheBucketName.UUID_VALUE.name(), cacheKey, (key) -> {
      String email = OurmUtils.getUserEmail(sfId, searchConfigService, snapService);
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

      log.info("Start to delete user with param {}.", userId);
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
        log.error("invalidate current user session failed.");
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
