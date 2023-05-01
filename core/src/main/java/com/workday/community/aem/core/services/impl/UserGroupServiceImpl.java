package com.workday.community.aem.core.services.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
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

import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTACT_ROLE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTEXT_INFO_KEY;
import static com.workday.community.aem.core.constants.WccConstants.ROLES;

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

    private transient Session jcrSession;

    private transient ResourceResolver jcrSessionResourceResolver;

    /**
     * The snap Config.
     */
    private SnapConfig config;

    /**
     * The user service user.
     */
    public static final String USER_SERVICE_USER = "adminusergroup";

    /**
     * The user service user.
     */
    public static final String READ_SERVICE_USER = "readserviceuser";

    /**
     * The AEM default user groups.
     */
    protected static final String[] AEM_DEFAULT_GROUPS = {"everyone"};

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
        logger.info("from  UserGroupServiceImpl.getLoggedInUsersGroups() ");
        String userRole = StringUtils.EMPTY;
        List<String> groupIds = new ArrayList<>();
        try {
            User user = CommonUtils.getLoggedInUser(resourceResolver);
            Value[] values = user.getProperty(WccConstants.PROFILE_SOURCE_ID);
            String sfId = values != null && values.length > 0 ? values[0].getString() : null;
            if (sfId != null) {
                logger.info("user  sfid {} ", sfId);
                Node userNode = resourceResolver.getResource(user.getPath()).adaptTo(Node.class);
                if (userNode.hasProperty(ROLES) && StringUtils.isNotBlank(userNode.getProperty(ROLES).getString()) &&
                        userNode.getProperty(ROLES).getString().split(";").length > 0) {
                    userRole = userNode.getProperty(ROLES).getString();
                    groupIds = List.of(userRole.split(";"));
                } else {
                    Map<String, Object> serviceParams = new HashMap<>();
                    serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
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
                jcrSessionResourceResolver = null;
            }
            if (jcrSession != null && jcrSession.isLive()) {
                jcrSession.logout();
                jcrSession = null;
            }

        }
        return groupIds;
    }


    /**
     * Get user groups groups from API.
     *
     * @param sfId User's Salesforce id.
     * @return List of user groups from snap.
     */
    protected List<String> getUserGroupsFromSnap(String sfId) {
        JsonObject context = snapService.getUserContext(sfId);
        JsonElement contextInfo = context.get(USER_CONTEXT_INFO_KEY);
        JsonObject contextInfoObj = contextInfo.getAsJsonObject();
        JsonElement groups = contextInfoObj.get(USER_CONTACT_ROLE_KEY);
        Optional<String> groupsString = Optional.ofNullable(groups.getAsString());
        return groupsString.map(value -> List.of(value.split(";")))
                .orElseGet(() -> {
                    logger.info("Value not found");
                    return new ArrayList<>();
                });
    }

    /**
     * Map the SF roles to aem roles.
     *
     * @param groups List user groups object
     * @return AEM roles
     */
    protected List<String> convertSfGroupsToAemGroups(List<String> groups) {
        List<String> groupIds = new ArrayList<>();
        // Reading the JSON File from DAM
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER)) {
            if (groupMap == null) {
                groupMap = DamUtils.readJsonFromDam(resourceResolver, config.sfToAemUserGroupMap());
            }
            for (String sfGroup : groups) {
                assert groupMap != null;
                if (!groupMap.get(sfGroup).isJsonNull()) {
                    if (groupMap.get(sfGroup).isJsonArray()) {
                        for (JsonElement aemGroup : groupMap.getAsJsonArray(sfGroup)) {
                            String aemGroupId = aemGroup.getAsString();
                            if (aemGroupId.length() > 0) {
                                groupIds.add(aemGroupId);
                            }
                        }
                    } else {
                        String aemGroupId = groupMap.get(sfGroup).getAsString();
                        if (aemGroupId.length() > 0) {
                            groupIds.add(aemGroupId);
                        }
                    }
                }
            }
        } catch (RuntimeException | LoginException e) {
            logger.error(String.format("Exception in SnaServiceImpl while getFailStateHeaderMenu, error: %s", e.getMessage()));
        }
        return groupIds;
    }

}
