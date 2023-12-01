package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL;
import static com.workday.community.aem.core.constants.WccConstants.ACCESS_CONTROL_TAG;
import static com.workday.community.aem.core.constants.WccConstants.AUTHENTICATED;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
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

  /**
   * The Constant PUBLIC_PATH_REGEX.
   */
  private static final Pattern PUBLIC_PATH_PATTERN = Pattern
      .compile("/content/workday-community/[a-z]{2}-[a-z]{2}/public/");
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
    List<String> groupIds = new ArrayList<>();
    String sfId = OurmUtils.getSalesForceId(request, userService);
    if (!StringUtils.isEmpty(sfId)) {
      log.debug(
          "---> UserGroupServiceImpl: getCurrentUserGroups - Trying to get Groups from Drupal");
      groupIds = getUserGroupsFromDrupal(sfId);
      if (null != groupIds && !groupIds.isEmpty()) {
        log.debug(
            "---> UserGroupServiceImpl: getCurrentUserGroups - Groups from Drupal ... {} ",
            StringUtils.join(groupIds, ";"));
      }
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
    Matcher matcher = PUBLIC_PATH_PATTERN.matcher(pagePath);
    return matcher.find();
  }
}