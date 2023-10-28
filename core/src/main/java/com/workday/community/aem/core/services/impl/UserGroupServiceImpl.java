package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL;
import static com.workday.community.aem.core.constants.SnapConstants.CUSTOMER_OF_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.IS_WORKMATE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.PARTNER_TRACK_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.PROPERTY_ACCESS_COMMUNITY;
import static com.workday.community.aem.core.constants.SnapConstants.PROPERTY_ACCESS_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTACT_INFORMATION_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTACT_ROLE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTEXT_INFO_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_TYPE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.WSP_KEY;
import static com.workday.community.aem.core.constants.WccConstants.ACCESS_CONTROL_TAG;
import static com.workday.community.aem.core.constants.WccConstants.AUTHENTICATED;
import static com.workday.community.aem.core.constants.WccConstants.INTERNAL_WORKMATES;
import static com.workday.community.aem.core.constants.WccConstants.ROLES;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.PageUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
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
   * The customer_role_mapping.
   */
  private Map<String, String> customerRoleMapping = new HashMap<>();

  /**
   * The customer_of_mapping.
   */
  private Map<String, String> customerOfMapping = new HashMap<>();

  /**
   * The wsp_mapping.
   */
  private Map<String, String> wspMapping = new HashMap<>();

  /**
   * The partner_track_mapping.
   */
  private Map<String, String> partnerTrackMapping = new HashMap<>();

  @Activate
  @Modified
  protected void activate(SnapConfig config) throws CacheException, DamException {
    ResourceResolver resourceResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER);
    JsonObject sfdcRoleMap =
        DamUtils.readJsonFromDam(resourceResolver, config.sfToAemUserGroupMap());
    if (sfdcRoleMap != null) {
      Gson g = new Gson();
      customerRoleMapping =
          g.fromJson(sfdcRoleMap.get("customerRoleMapping").toString(), HashMap.class);
      customerOfMapping =
          g.fromJson(sfdcRoleMap.get("customerOfMapping").toString(), HashMap.class);
      wspMapping = g.fromJson(sfdcRoleMap.get("wspMapping").toString(), HashMap.class);
      partnerTrackMapping =
          g.fromJson(sfdcRoleMap.get("partnerTrackMapping").toString(), HashMap.class);
    }
  }

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
      if (pagePath.startsWith(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH)) {
        User user = userService.getCurrentUser(request);
        boolean isPublicPage = isPublicPage(pagePath);
        if (user == null) {
          return isPublicPage;
        } else if (isPublicPage) {
          return true;
        } else {
          log.debug("---> UserGroupServiceImpl: Before Access control tag List and userPath is: {}", user.getPath());
          List<String> accessControlTagsList = PageUtils.getPageTagPropertyList(request.getResourceResolver(), pagePath,
              ACCESS_CONTROL_TAG, TAG_PROPERTY_ACCESS_CONTROL);
          log.debug("---> UserGroupServiceImpl: After Access control tag List");
          if (!accessControlTagsList.isEmpty()) {
            log.debug("---> UserGroupServiceImpl:Access control tag List.. {}.", accessControlTagsList);
            if (accessControlTagsList.contains(AUTHENTICATED)) {
              return true;
            } else {
              List<String> groupsList = getCurrentUserGroups(request);
              log.debug("---> UserGroupServiceImpl: Groups List..{}.", groupsList);
              return !Collections.disjoint(accessControlTagsList, groupsList);
            }
          }
        }
      } else {
        // For External pages
        return true;
      }
    } catch (Exception exec) {
      log.error("---> Exception in validateTheUser function: {}", exec);
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
   *
   * @return User group list.
   */
  @Override
  public List<String> getCurrentUserGroups(SlingHttpServletRequest request) {
    log.info("from  UserGroupServiceImpl.getLoggedInUsersGroups() ");
    String userRole;
    List<String> groupIds = new ArrayList<>();
    try {
      User user = userService.getCurrentUser(request);
      String sfId = OurmUtils.getSalesForceId(request, userService);
      if (sfId != null) {
        log.debug("user  sfid {} ", sfId);
        ResourceResolver jcrResolver =
            cacheManager.getServiceResolver(WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
        Node userNode =
            Objects.requireNonNull(jcrResolver.getResource(user.getPath())).adaptTo(Node.class);
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
          groupIds = getUserGroupsFromSnap(sfId);
          userNode.setProperty(ROLES, StringUtils.join(groupIds, ";"));
          Objects.requireNonNull(jcrSession).save();
          if (null != groupIds && !groupIds.isEmpty()) {
            log.debug(
                "---> UserGroupServiceImpl: getCurrentUserGroups - Groups from SNAP ... {} ",
                StringUtils.join(groupIds, ";"));
          }
        }
        log.info("Salesforce roles {}", groupIds);
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
   * Get user groups from API.
   *
   * @param sfId User's Salesforce id.
   * @return List of user groups from snap.
   */
  protected List<String> getUserGroupsFromSnap(String sfId) {
    List<String> groups = new ArrayList<>();
    if (StringUtils.isBlank(sfId)) {
      return groups;
    }
    String cacheKey = String.format("sf-user-groups-%s", sfId);
    List<String> ret = cacheManager.get(CacheBucketName.SF_USER_GROUP.name(), cacheKey, () -> {
      JsonObject context = snapService.getUserContext(sfId);

      if (null == context || context.isJsonNull() || context.size() == 0) {
        return groups;
      }

      JsonObject contactInformation = context.get(USER_CONTACT_INFORMATION_KEY).getAsJsonObject();

      JsonElement propertyAccess = contactInformation.get(PROPERTY_ACCESS_KEY);
      boolean hasCommunityAccess = false;
      if (!propertyAccess.isJsonNull()
          && propertyAccess.getAsString().contains(PROPERTY_ACCESS_COMMUNITY)) {
        groups.add(AUTHENTICATED);
        hasCommunityAccess = true;
      }
      addGroups(groups, contactInformation, WSP_KEY, wspMapping);

      JsonObject contextInfo = context.get(USER_CONTEXT_INFO_KEY).getAsJsonObject();
      addGroups(groups, contextInfo, USER_CONTACT_ROLE_KEY, customerRoleMapping);

      JsonElement type = contextInfo.get(USER_TYPE_KEY);
      JsonElement customerOf = contactInformation.get(CUSTOMER_OF_KEY);
      JsonElement partnerTrack = contactInformation.get(PARTNER_TRACK_KEY);
      JsonElement isWorkmate = contextInfo.get(IS_WORKMATE_KEY);
      boolean isWorkmateUser = false;
      if (!isWorkmate.isJsonNull()) {
        isWorkmateUser = isWorkmate.getAsBoolean();
      }
      if (!type.isJsonNull()) {
        String typeString = type.getAsString();
        if (typeString.equals("customer") && !isWorkmateUser && !customerOf.isJsonNull()) {
          String customerOfString = customerOf.getAsString();
          for (Map.Entry<String, String> entry : customerOfMapping.entrySet()) {
            if (customerOfString.contains(entry.getKey())) {
              groups.add(entry.getValue());
            }
          }
        }
        if (typeString.equals("partner") && !partnerTrack.isJsonNull()) {
          String partnerTrackString = partnerTrack.getAsString();
          for (Map.Entry<String, String> entry : partnerTrackMapping.entrySet()) {
            if (partnerTrackString.contains(entry.getKey())) {
              groups.add(entry.getValue());
            }
          }
        }
      }
      if (isWorkmateUser) {
        groups.add(INTERNAL_WORKMATES);
      } else {
        if (hasCommunityAccess && !type.isJsonNull()) {
          groups.add(type.getAsString() + "_all");
        }
      }
      return groups;
    });

    if (ret != null && ret.isEmpty()) {
      cacheManager.invalidateCache(CacheBucketName.SF_USER_GROUP.name(), cacheKey);
    }

    return ret;
  }

  private void addGroups(List<String> groups, JsonObject contactInformation, String key,
                         Map<String, String> valueMap) {
    JsonElement wsp = contactInformation.get(key);
    if (!wsp.isJsonNull()) {
      String wspString = wsp.getAsString();
      for (Map.Entry<String, String> entry : valueMap.entrySet()) {
        if (wspString.contains(entry.getKey())) {
          groups.add(entry.getValue());
        }
      }
    }
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