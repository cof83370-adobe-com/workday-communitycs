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
import static com.workday.community.aem.core.constants.SnapConstants.NSC_SUPPORTING_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.PROPERTY_ACCESS_COMMUNITY;

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
     * The user service user.
     */
    public static final String READ_SERVICE_USER = "readserviceuser";

    /**
     * The customer_role_mapping.
     */
    private HashMap<String, String> customerRoleMapping = new HashMap<>();

    /**
     * The nsc_supporting_mapping.
     */
    private HashMap<String, String> nscSupportingMapping = new HashMap<>();

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
        JsonElement nscSupporting = contactInformation.get(NSC_SUPPORTING_KEY);
        if (!nscSupporting.isJsonNull()) {
            String nscSupportingString = nscSupporting.getAsString();
            for (Map.Entry<String, String> entry : nscSupportingMapping.entrySet()) {
                if (nscSupportingString.contains(entry.getKey())) {
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
        JsonElement isWorkmate = contextInfo.get(IS_WORKMATE_KEY);
        if (!isWorkmate.isJsonNull() && isWorkmate.getAsBoolean()) {
            groups.add(INTERNAL_WORKMATES);
        }
        else {
            JsonElement type = contextInfo.get(USER_TYPE_KEY);
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
            nscSupportingMapping = g.fromJson(sfdcRoleMap.get("nscSupportingMapping").toString(), HashMap.class);
        }

    }
}
