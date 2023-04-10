package com.workday.community.aem.core.services.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * The Class UserGroupServiceImpl.
 */
@Component(
        service = UserGroupService.class,
        immediate = true
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
    @Reference
    private SnapConfig config;

    /** The user service user. */
    public static final String USER_SERVICE_USER = "adminusergroup";

    /** The user service user. */
    public static final String READ_SERVICE_USER = "readserviceuser";

    /**
     * Returns current logged-in users groups.
     *
     * @return User group list.
     */
    public List<String> getLoggedInUsersGroups() {
        List<String> groupIds = new ArrayList<>();
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, USER_SERVICE_USER)) {
            User user = CommonUtils.getLoggedInUser(resourceResolver);
            Iterator<Group> groups = user.memberOf();
            if (groups == null || !groups.hasNext()) {
                String sfId = user.getProperty(WccConstants.PROFILE_SOURCE_ID) != null ?
                        user.getProperty(WccConstants.PROFILE_SOURCE_ID)[0].getString() : null;
                if (sfId != null) {
                    groupIds = this.getUserGroupsFromSnap(sfId);
                    if (groupIds.size() > 0) {
                        groupIds = this.convertSfGroupsToAemGroups(groupIds);
                        userService.updateUser(user.getID(), Map.<String, String>of(), groupIds);
                    }
                }
            }
            else {
                Group group;
                while(groups.hasNext()) {
                    group = groups.next();
                    groupIds.add(group.getID());
                }
            }
        } catch (LoginException | RepositoryException e) {
            throw new RuntimeException(e);
        }
        return groupIds;
    }

    /**
     * Get user groups groups from API.
     *
     * @param sfId
     * @return
     */
    protected List<String> getUserGroupsFromSnap(String sfId) {
        JsonObject context = snapService.getUserContext(sfId);
        JsonElement contextInfo = context.get("contextInfo");
        JsonObject contextInfoObj  = contextInfo.getAsJsonObject();
        JsonElement groups = contextInfoObj.get("contactRole");
        Optional<String> groupsString = Optional.ofNullable(groups.getAsString());
        List<String> groupsArray = groupsString.map(value -> List.of(value.split(";")))
                        .orElseGet(() -> {
                            logger.info("value not found");
                            return new ArrayList<String>();
                        });
        return groupsArray;
    }

    /**
     * Map the SF roles to aem roles.
     *
     * @param groups
     * @return AEM roles
     */
    protected List<String> convertSfGroupsToAemGroups(List<String> groups) {
        List<String> groupIds = new ArrayList<>();
        // Reading the JSON File from DAM
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER)) {
            JsonObject json = DamUtils.readJsonFromDam(resourceResolver, config.sfToAemUserGroupMap());
            for (String sfGroup : groups) {
                if (!json.get(sfGroup).isJsonNull()) {
                    if (json.get(sfGroup).isJsonArray()) {
                        for (JsonElement aemGroup : json.getAsJsonArray(sfGroup)) {
                            String aemGroupId = aemGroup.getAsString();
                            if (aemGroupId.length() > 0) {
                                groupIds.add(aemGroupId);
                            }
                        }
                    }
                    else {
                        String aemGroupId = json.get(sfGroup).getAsString();
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
