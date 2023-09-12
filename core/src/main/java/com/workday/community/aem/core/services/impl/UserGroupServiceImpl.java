package com.workday.community.aem.core.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.CacheBucketName;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.PageUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.*;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static com.workday.community.aem.core.constants.WccConstants.AUTHENTICATED;
import static com.workday.community.aem.core.constants.WccConstants.INTERNAL_WORKMATES;
import static com.workday.community.aem.core.constants.WccConstants.ROLES;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTACT_ROLE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTEXT_INFO_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_TYPE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTACT_INFORMATION_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.PROPERTY_ACCESS_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.IS_WORKMATE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.CUSTOMER_OF_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.PARTNER_TRACK_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.WSP_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.PROPERTY_ACCESS_COMMUNITY;
import static com.workday.community.aem.core.constants.WccConstants.ACCESS_CONTROL_PROPERTY;
import static com.workday.community.aem.core.constants.WccConstants.ACCESS_CONTROL_TAG;

/**
 * The Class UserGroupServiceImpl.
 */
@Component(service = UserGroupService.class, immediate = true, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class UserGroupServiceImpl implements UserGroupService {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupServiceImpl.class);

    /**
     * The snap service.
     */
    @Reference
    SnapService snapService;
    /**
     * The drupal service.
     */
    @Reference
    DrupalService drupalService;

    /** The cache manager */
    @Reference
    CacheManagerService cacheManager;

    @Reference
    UserService userService;

    /**
     * The customer_role_mapping.
     */
    private HashMap<String, String> customerRoleMapping = new HashMap<>();

    /**
     * The customer_of_mapping.
     */
    private HashMap<String, String> customerOfMapping = new HashMap<>();

    /**
     * The wsp_mapping.
     */
    private HashMap<String, String> wspMapping = new HashMap<>();

    /**
     * The partner_track_mapping.
     */
    private HashMap<String, String> partnerTrackMapping = new HashMap<>();

    @Activate
    @Modified
    protected void activate(SnapConfig config) throws CacheException, DamException {
        ResourceResolver resourceResolver = cacheManager.getServiceResolver(READ_SERVICE_USER);
        /*
         * SFDC Role mapping json object.
         */
        JsonObject sfdcRoleMap = DamUtils.readJsonFromDam(resourceResolver, config.sfToAemUserGroupMap());
        if (sfdcRoleMap != null) {
            Gson g = new Gson();
            customerRoleMapping = g.fromJson(sfdcRoleMap.get("customerRoleMapping").toString(), HashMap.class);
            customerOfMapping = g.fromJson(sfdcRoleMap.get("customerOfMapping").toString(), HashMap.class);
            wspMapping = g.fromJson(sfdcRoleMap.get("wspMapping").toString(), HashMap.class);
            partnerTrackMapping = g.fromJson(sfdcRoleMap.get("partnerTrackMapping").toString(), HashMap.class);
        }
    }

    @Override
    public boolean validateCurrentUser(SlingHttpServletRequest request, String pagePath) {
        LOGGER.debug(" inside validateTheUser method. -->");

        boolean isValid = false;
        try {
            LOGGER.debug("---> UserGroupServiceImpl: Before Access control tag List");
            List<String> accessControlTagsList = PageUtils.getPageTagPropertyList(request.getResourceResolver(),
                    pagePath,
                    ACCESS_CONTROL_TAG, ACCESS_CONTROL_PROPERTY);
            LOGGER.debug("---> UserGroupServiceImpl: After Access control tag List");
            if (!accessControlTagsList.isEmpty()) {
                LOGGER.debug("---> UserGroupServiceImpl:Access control tag List.. {}.", accessControlTagsList);
                if (accessControlTagsList.contains(AUTHENTICATED)) {
                    isValid = true;
                } else {
                    List<String> groupsList = getCurrentUserGroups(request);
                    LOGGER.debug("---> UserGroupServiceImpl: Groups List..{}.", groupsList);
                    isValid = !Collections.disjoint(accessControlTagsList, groupsList);
                }
            }
        } catch (RepositoryException e) {
            LOGGER.error("---> Exception in validateTheUser function: {}.", e.getMessage());
        }
        return isValid;
    }

    @Override
    public boolean validateCurrentUser(SlingHttpServletRequest request, List<String> accessControlTags) {
        LOGGER.debug("Inside checkLoggedInUserHasAccessControlValues method. -->");

        if (accessControlTags == null || accessControlTags.isEmpty())
            return false;
        if (accessControlTags.contains(AUTHENTICATED))
            return true;
        List<String> groupsList = getCurrentUserGroups(request);

        LOGGER.debug("---> UserGroupServiceImpl: validateCurrentUser - Groups List..{}.", groupsList);
        return !Collections.disjoint(accessControlTags, groupsList);
    }

    /**
     * Returns current logged-in users groups.
     * Check whether user node has property roles. If it is there then return from
     * node property. If not, call API for roles.
     *
     * @param request: current Sling request object.
     * @return User group list.
     */
    @Override
    public List<String> getCurrentUserGroups(SlingHttpServletRequest request) {
        LOGGER.info("from  UserGroupServiceImpl.getLoggedInUsersGroups() ");
        String userRole;
        List<String> groupIds = new ArrayList<>();
        try {
            User user = userService.getCurrentUser(request);
            String sfId = OurmUtils.getSalesForceId(request, userService);
            if (sfId != null) {
                LOGGER.debug("user  sfid {} ", sfId);
                ResourceResolver jcrResolver = cacheManager
                        .getServiceResolver(WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
                Node userNode = Objects.requireNonNull(jcrResolver.getResource(user.getPath())).adaptTo(Node.class);
                if (Objects.requireNonNull(userNode).hasProperty(ROLES) &&
                        StringUtils.isNotBlank(userNode.getProperty(ROLES).getString()) &&
                        userNode.getProperty(ROLES).getString().split(";").length > 0) {
                    userRole = userNode.getProperty(ROLES).getString();
                    groupIds = List.of(userRole.split(";"));
                } else {
                    Session jcrSession = jcrResolver.adaptTo(Session.class);
                    groupIds = getUserGroupsFromDrupal(sfId);
                    userNode.setProperty(ROLES, StringUtils.join(groupIds, ";"));
                    Objects.requireNonNull(jcrSession).save();
                }
                LOGGER.info("Salesforce roles {}", groupIds);
            }

        } catch (RepositoryException | CacheException e) {
            LOGGER.error("---> Exception in AuthorizationFilter.. {}", e.getMessage());
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
            JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);
            JsonArray rolesArray = userDataObject.getAsJsonArray("roles");
            for (int i = 0; i < rolesArray.size(); i++) {
                groups.add(rolesArray.get(i).getAsString());
            }
        } catch (DrupalException e) {
            LOGGER.error("---> Exception in getUserGroupsFromDrupal method: {}.", e.getMessage());
        }
        return groups;
    }
}
