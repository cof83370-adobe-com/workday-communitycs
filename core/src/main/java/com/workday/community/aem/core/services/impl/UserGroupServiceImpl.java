package com.workday.community.aem.core.services.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.*;

import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTACT_ROLE_KEY;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTEXT_INFO_KEY;

/**
 * The Class UserGroupServiceImpl.
 */
@Component(
        service = UserGroupService.class,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.OPTIONAL
)
public class UserGroupServiceImpl implements UserGroupService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * The snap service.
     */
    @Reference
    SnapService snapService;

    /**
     * The user service.
     */
    @Reference
    UserService userService;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** The snap Config. */
    private SnapConfig config;

    /** The user service user. */
    public static final String USER_SERVICE_USER = "adminusergroup";

    /** The user service user. */
    public static final String READ_SERVICE_USER = "readserviceuser";

    /** The AEM default user groups. */
    protected static final String[] AEM_DEFAULT_GROUPS = { "everyone" };

    /** The group map json */
    JsonObject groupMap = null;

    @Activate
    @Modified
    @Override
    public void activate(SnapConfig config) {
        this.config = config;
    }

    /**
     * Returns current logged-in users groups.
     *
     * @return User group list.
     */
    public List<String> getLoggedInUsersGroups() throws OurmException {
        List<String> groupIds = new ArrayList<>();
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, USER_SERVICE_USER)) {
            User user = CommonUtils.getLoggedInUser(resourceResolver);

            Iterator<Group> groups = user.memberOf();
            boolean hasSfRoles = false;
            while(groups.hasNext()) {
                String groupId = groups.next().getID();
                groupIds.add(groupId);
                if (!ArrayUtils.contains(AEM_DEFAULT_GROUPS, groupId)) {
                    hasSfRoles = true;
                }
            }

            if (!hasSfRoles) {
                Value[] values = user.getProperty(WccConstants.PROFILE_SOURCE_ID);
                String sfId = values != null && values.length > 0 ? values[0].getString() : null;
                if (sfId != null) {
                    List<String> sfGroupsIds =  this.getUserGroupsFromSnap(sfId);
                    logger.info("Salesforce roles "+ StringUtils.join(",",sfGroupsIds));
                    if (!sfGroupsIds.isEmpty()) {
                        groupIds.addAll(this.convertSfGroupsToAemGroups(sfGroupsIds));
                        userService.updateUser(user.getID(), Map.<String, String>of(), groupIds);
                    }
                }
            }
        } catch (LoginException | RepositoryException e) {
            throw new OurmException(e.getMessage());
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
        JsonObject contextInfoObj  = contextInfo.getAsJsonObject();
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
                    }
                    else {
                        String aemGroupId = groupMap.get(sfGroup).getAsString();
                        if (aemGroupId.length() > 0) {
                            groupIds.add(aemGroupId);
                        }
                    }
                }
            }
        }
        catch (RuntimeException | LoginException e) {
            logger.error(String.format("Exception in SnaServiceImpl while getFailStateHeaderMenu, error: %s", e.getMessage()));
        }
        return groupIds;
    }

}
