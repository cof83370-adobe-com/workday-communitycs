package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL;
import static com.workday.community.aem.core.constants.WccConstants.ACCESS_CONTROL_TAG;
import static com.workday.community.aem.core.constants.WccConstants.AUTHENTICATED;
import static com.workday.community.aem.core.constants.WccConstants.ROLES;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.PageUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class UserGroupServiceImpl.
 */
@Slf4j
@Component(
    service = UserGroupService.class,
    immediate = true,
    configurationPolicy = ConfigurationPolicy.OPTIONAL
)
public class UserGroupServiceImpl implements UserGroupService {

  /** The Constant PUBLIC_PATH_REGEX. */
  protected static final String PUBLIC_PATH_REGEX = "/content/workday-community/[a-z]{2}-[a-z]{2}/public/";

  /**
   * The snap service.
   */
  @Reference
  private SnapService snapService;

  /**
   * The drupal service.
   */
  @Reference
  private DrupalService drupalService;

  /**
   * The cache manager.
   */
  @Reference
  private CacheManagerService cacheManager;

  /**
   * The user service.
   */
  @Reference
  private UserService userService;

  /**
   * The evaluation process determines whether a user has the necessary permissions
   * to view a specified link. Public pages are accessible by default, while anonymous
   * or unavailable logged-in users can also access them. However, for secured internal
   * pages with Okta users, the ACL logic is evaluated to determine access.
   *
   * @param pagePath the page path
   * @param request  the request
   * @return true, if successful
   */
  @Override
  public boolean validateCurrentUser(SlingHttpServletRequest request, String pagePath) {
    log.debug(" inside validateTheUser method. -->");

    try {
      if (StringUtils.isBlank(pagePath)) {
        return false;
      }
      if (!pagePath.startsWith(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH)) {
        return true;
      }

      User user = userService.getCurrentUser(request);
      boolean isPublicPage = isPublicPage(pagePath);

      if (user == null) {
        return isPublicPage;
      }

      if (isPublicPage) {
        return true;
      }

      List<String> accessControlTagsList =
          PageUtils.getPageTagPropertyList(request.getResourceResolver(),
              pagePath,
              ACCESS_CONTROL_TAG, TAG_PROPERTY_ACCESS_CONTROL);
      log.debug("---> UserGroupServiceImpl: After Access control tag List");

      if (!accessControlTagsList.isEmpty()) {
        log.debug("---> UserGroupServiceImpl: Access control tag List: {}.", accessControlTagsList);

        if (accessControlTagsList.contains(AUTHENTICATED)) {
          return true;
        } else {
          List<String> groupsList = getCurrentUserGroups(request);
          log.debug("---> UserGroupServiceImpl: Groups List: {}.", groupsList);
          return !Collections.disjoint(accessControlTagsList, groupsList);
        }
      }
    } catch (RepositoryException | CacheException e) {
      log.error("---> Exception in validateTheUser function: {}.", e.getMessage());
    }
    return false;
  }

  @Override
  public boolean validateCurrentUser(SlingHttpServletRequest request,
                                     List<String> accessControlTags) {
    log.debug("Inside checkLoggedInUserHasAccessControlValues method. -->");

    if (accessControlTags == null || accessControlTags.isEmpty()) {
      return false;
    }
    if (accessControlTags.contains(AUTHENTICATED)) {
      return true;
    }
    List<String> groupsList = getCurrentUserGroups(request);

    log.debug("---> UserGroupServiceImpl: validateCurrentUser - Groups List..{}.", groupsList);
    return !Collections.disjoint(accessControlTags, groupsList);
  }

  /**
   * Returns current logged-in users groups.
   * Check whether user node has property roles. If it is there then return from
   * node property. If not, call API for roles.
   *
   * @param request current Sling request object.
   * @return User group list.
   */
  @Override
  public List<String> getCurrentUserGroups(SlingHttpServletRequest request) {
    log.debug("from  UserGroupServiceImpl.getLoggedInUsersGroups() ");
    String userRole;
    List<String> groupIds = new ArrayList<>();
    try {
      User user = userService.getCurrentUser(request);
      String sfId = OurmUtils.getSalesForceId(request, userService);
      if (sfId != null) {
        String userPath = user.getPath();
        log.debug("user  sfId {}, path: {} ", sfId, userPath);
        ResourceResolver jcrResolver = cacheManager.getServiceResolver(WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
        Resource resource = jcrResolver.getResource(user.getPath());
        if (resource == null) {
          log.error("can't find user with path: {}", userPath);
          return groupIds;
        }

        Node userNode = Objects.requireNonNull(resource).adaptTo(Node.class);
        if (Objects.requireNonNull(userNode).hasProperty(ROLES)
            && StringUtils.isNotBlank(userNode.getProperty(ROLES).getString())
            && userNode.getProperty(ROLES).getString().split(";").length > 0) {
          log.debug(
              "---> UserGroupServiceImpl: getCurrentUserGroups - User has Groups in CRX...{}",
              userNode.getProperty(ROLES).getString());
          userRole = userNode.getProperty(ROLES).getString();
          groupIds = List.of(userRole.split(";"));
        } else {
          log.debug(
              "---> UserGroupServiceImpl: getCurrentUserGroups - Trying to get Groups from SNAP");
          Session jcrSession = jcrResolver.adaptTo(Session.class);
          groupIds = getUserGroupsFromDrupal(sfId);
          userNode.setProperty(ROLES, StringUtils.join(groupIds, ";"));
          Objects.requireNonNull(jcrSession).save();
          if (null != groupIds && !groupIds.isEmpty()) {
            log.debug(
                "---> UserGroupServiceImpl: getCurrentUserGroups - Groups from SNAP ... {} ",
                StringUtils.join(groupIds, ";"));
          }
        }
        log.debug("Salesforce roles {}", groupIds);
      }

    } catch (RepositoryException | CacheException e) {
      log.error("---> Exception in AuthorizationFilter.. {}", e.getMessage());
    }
    return groupIds;
  }

  /**
   * This is used in testing.
   *
   * @param cacheManager the pass-in Cache manager object.
   */
  protected void setCacheManager(CacheManagerService cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Get user groups from Drupal API.
   *
   * @param sfId User's Salesforce id.
   * @return List of user groups from Drupal.
   */
  protected List<String> getUserGroupsFromDrupal(String sfId) {
    List<String> groups = new ArrayList<>();
    try {
      Gson gson = new Gson();
      String userData = drupalService.getUserData(sfId);
      if (StringUtils.isEmpty(userData)) {
        log.error("Error in getUserGroupsFromDrupal method - empty user data response.");
        return groups;
      }
      JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);
      JsonArray rolesArray = userDataObject.getAsJsonArray("roles");
      for (int i = 0; i < rolesArray.size(); i++) {
        groups.add(rolesArray.get(i).getAsString());
      }
    } catch (JsonSyntaxException e) {
      log.error("---> Exception in getUserGroupsFromDrupal method: {}.", e.getMessage());
    }
    return groups;
  }

  /**
   * Checks if is public page.
   *
   * @param pagePath the page path
   * @return true, if is public page
   */
  private boolean isPublicPage(String pagePath) {
    Pattern regex = Pattern.compile(PUBLIC_PATH_REGEX);
    Matcher matcher = regex.matcher(pagePath);
    return matcher.find();
  }
}