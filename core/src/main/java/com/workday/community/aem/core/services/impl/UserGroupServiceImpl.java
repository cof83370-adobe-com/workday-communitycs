package com.workday.community.aem.core.services.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.List;

public class UserGroupServiceImpl implements UserGroupService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /**
     * The snap service.
     */
    @Reference
    SnapService snapService;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** The service user. */
    public static final String SERVICE_USER = "readserviceuser";

    public List<String> getUserGroupsBySfId(String sfId, SlingHttpServletRequest request) {
        try {
            HttpSession session = request.getSession();
            List<String> userGroups = (List<String>) session.getAttribute("userGroups");
            if (!userGroups.isEmpty()) {
                return userGroups;
            }

            if (userGroups.isEmpty()) {
                userGroups = getUserGroupsFromSnap(sfId);
            }

            if (!userGroups.isEmpty()) {
                UserManager userManager = ((JackrabbitSession) session).getUserManager();
                Authorizable user = userManager.getAuthorizable(((JackrabbitSession) session).getUserID());
                Iterator<Group> groups = user.memberOf();
                while (groups.hasNext()) {
                    Group group = groups.next();
                    group.removeMember(user);
                }
                for(String groupId: userGroups) {
                    Group group = (Group) userManager.getAuthorizable(groupId);
                    if (group == null) {
                        group = userManager.createGroup(groupId);
                    }
                    group.addMember(user);
                }
            }
            session.setAttribute("userGroups", userGroups);
        } catch (Exception e) {
            logger.error("Exception in Loading the user", e);
        }

    }

    public List<String> getUserGroupsFromSnap(String sfId) {
        JsonObject context = snapService.getUserContext(sfId);
        JsonElement contextInfo = context.get("contextInfo");
        JsonObject contextInfoObj  = contextInfo.getAsJsonObject();
        JsonElement groups = contextInfoObj.get("contactRole");
        String[] groupsArray = groups.getAsString().split(",");
        return List.of(groupsArray);
    }
}
