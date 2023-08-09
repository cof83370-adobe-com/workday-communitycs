package com.workday.community.aem.core.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.PageUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
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
@Component(
        service = UserGroupService.class,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.OPTIONAL
)
public class UserGroupServiceImpl implements UserGroupService {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * The snap service.
     */
    @Reference
    SnapService snapService;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /**
     * The snap Config.
     */
    private SnapConfig config;

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

    /**
     * SFDC Role mapping json object.
     */
    private JsonObject sfdcRoleMap;

    /**
     * The group map json
     */
    JsonObject groupMap = null;

    @Activate
    @Modified
    @Override
    public void activate(SnapConfig config) {
        this.config = config;
    }

    /**
     * Returns current logged-in users groups.
     * Check whether user node has property roles. If it is there then return from node property. If not, call API for roles.
     * @param resourceResolver:  User's request resourceResolver.
     * @return User group list.
     */
    public List<String> getLoggedInUsersGroups(ResourceResolver resourceResolver) throws OurmException {
        ResourceResolver jcrSessionResourceResolver = null;
        Session jcrSession = null;
        logger.info("from  UserGroupServiceImpl.getLoggedInUsersGroups() ");
        String userRole = StringUtils.EMPTY;
        List<String> groupIds = new ArrayList<>();
        try {
            User user = CommonUtils.getLoggedInUser(resourceResolver);
            Value[] values = user.getProperty(WccConstants.PROFILE_SOURCE_ID);
            String sfId = values != null && values.length > 0 ? values[0].getString() : null;
            if (sfId != null) {
                logger.debug("user  sfid {} ", sfId);
                Node userNode = resourceResolver.getResource(user.getPath()).adaptTo(Node.class);
                if (userNode.hasProperty(ROLES) && StringUtils.isNotBlank(userNode.getProperty(ROLES).getString()) &&
                        userNode.getProperty(ROLES).getString().split(";").length > 0) {
                    userRole = userNode.getProperty(ROLES).getString();
                    groupIds = List.of(userRole.split(";"));
                } else {
                    Map<String, Object> serviceParams = new HashMap<>();
                    serviceParams.put(ResourceResolverFactory.SUBSERVICE, WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
                    jcrSessionResourceResolver = resourceResolverFactory.getServiceResourceResolver(serviceParams);
                    jcrSession = jcrSessionResourceResolver.adaptTo(Session.class);
                    groupIds = this.getUserGroupsFromSnap(sfId);
                    userNode.setProperty(ROLES, StringUtils.join(groupIds, ";"));
                    jcrSession.save();
                }
                logger.info("Salesforce roles {}", groupIds);
            }

        } catch (LoginException | RepositoryException e) {
            logger.error("---> Exception in AuthorizationFilter.. {}", e.getMessage());
        } finally {
            if (jcrSessionResourceResolver != null && jcrSessionResourceResolver.isLive()) {
                jcrSessionResourceResolver.close();
            }
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
            }
        }
        return groupIds;
    }

    /**
     * Validates the user based on Roles tagged to the page and User roles from Salesforce.
     *
     * @param resourceResolver: the Request resource Resolver
     * @param requestResourceResolver: the Request resource Resolver
     * @param pagePath : The Requested page path.
     * @return boolean: True if user has permissions otherwise false.
     * @throws LoginException
     */
    public  boolean validateTheUser(ResourceResolver resourceResolver, ResourceResolver requestResourceResolver,String pagePath) {
        logger.debug(" inside validateTheUser method. -->");
        boolean isInValid = true;
        try{
            logger.debug("---> UserGroupServiceImpl: Before Access control tag List");
            List<String> accessControlTagsList = PageUtils.getPageTagPropertyList(resourceResolver, pagePath, ACCESS_CONTROL_TAG, ACCESS_CONTROL_PROPERTY);
            logger.debug("---> UserGroupServiceImpl: After Access control tag List");
            if (!accessControlTagsList.isEmpty()) {
                logger.debug("---> UserGroupServiceImpl:Access control tag List.. {}.", accessControlTagsList);
                if (accessControlTagsList.contains(AUTHENTICATED)) {
                    isInValid = false;
                } else {
                    List<String> groupsList = getLoggedInUsersGroups(requestResourceResolver);
                    logger.debug("---> UserGroupServiceImpl: Groups List..{}.", groupsList);
                    if (!Collections.disjoint(accessControlTagsList, groupsList)) {
                        isInValid = false;
                    }
                }
            }
        } catch (RepositoryException  | OurmException e) {
            logger.error("---> Exception in validateTheUser function: {}.", e.getMessage());
        }
        return isInValid;
    }

    /**
     * Get user groups from API.
     *
     * @param sfId User's Salesforce id.
     * @return List of user groups from snap.
     */
    protected List<String> getUserGroupsFromSnap(String sfId) {
        List<String> groups = new ArrayList<>();
        JsonObject context = snapService.getUserContext(sfId);
        setSfdcRoleMap();
        
        JsonObject contactInformation = context.get(USER_CONTACT_INFORMATION_KEY).getAsJsonObject();
        JsonElement propertyAccess = contactInformation.get(PROPERTY_ACCESS_KEY);
        boolean hasCommunityAccess = false;
        if (!propertyAccess.isJsonNull() && propertyAccess.getAsString().contains(PROPERTY_ACCESS_COMMUNITY)) {
            groups.add(AUTHENTICATED);
            hasCommunityAccess = true;
        }
        JsonElement wsp = contactInformation.get(WSP_KEY);
        if (!wsp.isJsonNull()) {
            String wspString = wsp.getAsString();
            for(Map.Entry<String, String> entry: wspMapping.entrySet()) {
                if (wspString.contains(entry.getKey())) {
                    groups.add(entry.getValue());
                }
            }
        }
        JsonObject contextInfo = context.get(USER_CONTEXT_INFO_KEY).getAsJsonObject();
        JsonElement contactRolesObj = contextInfo.get(USER_CONTACT_ROLE_KEY);
        if (!contactRolesObj.isJsonNull()) {
            String contactRoles = contactRolesObj.getAsString();
            for (Map.Entry<String, String> entry : customerRoleMapping.entrySet()) {
                if (contactRoles.contains(entry.getKey())) {
                    groups.add(entry.getValue());
                }
            }
        }
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
        }
        else {
            if (hasCommunityAccess && !type.isJsonNull()) {
                groups.add(type.getAsString() + "_all");
            }
        }
        return groups;
    }

    /**
     * Set sfdc role map json object.
     */
    protected void setSfdcRoleMap() {
        if (sfdcRoleMap == null) {
            try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER)) {
                sfdcRoleMap = DamUtils.readJsonFromDam(resourceResolver, config.sfToAemUserGroupMap());
            }
            catch (LoginException | DamException e) {
                logger.error("Error reading sfdc role map json file: {}.", e.getMessage());
            }
        }
        if (sfdcRoleMap != null) {
            Gson g = new Gson();
            customerRoleMapping = g.fromJson(sfdcRoleMap.get("customerRoleMapping").toString(), HashMap.class);
            customerOfMapping = g.fromJson(sfdcRoleMap.get("customerOfMapping").toString(), HashMap.class);
            wspMapping = g.fromJson(sfdcRoleMap.get("wspMapping").toString(), HashMap.class);
            partnerTrackMapping = g.fromJson(sfdcRoleMap.get("partnerTrackMapping").toString(), HashMap.class);
        }

    }
}
