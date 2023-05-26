package com.workday.community.aem.core.services.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.*;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.ResolverUtil;
import static com.workday.community.aem.core.constants.GlobalConstants.OKTA_USER_PATH;

/**
 * The Class UserServiceImpl.
 */
@Component(
    service = UserService.class,
    immediate = true
)
public class UserServiceImpl implements UserService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The resource resolver factory. */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** The service user. */
    public static final String SERVICE_USER = "adminusergroup";

    @Override
    public User getUser(String userId) {
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, SERVICE_USER)) {
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            User user = (User) userManager.getAuthorizable(userId);
            if (user != null) {
                return user;
            }
            logger.error("Cannot find user with id {}.", userId);
            return null;
        }
        catch (LoginException | RepositoryException e) {
            logger.error("Exception occurred when fetch user {}: {}.", userId, e.getMessage());
            return null;
        }
    }

    @Override
    public void updateUser(String userId, Map<String, String> fields, List<String> groups) {
        Session session = null;
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, SERVICE_USER)) {
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            session = resourceResolver.adaptTo(Session.class);
            User user = (User) userManager.getAuthorizable(userId);
            if (user != null) {
                ValueFactory valueFactory = session.getValueFactory();
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    Value fieldValue = valueFactory.createValue(entry.getValue(), PropertyType.STRING);
                    user.setProperty(entry.getKey(), fieldValue);
                }  
                if (!groups.isEmpty()) {
                    Iterator<Group> groupsIt = user.memberOf();
                    while (groupsIt.hasNext()) {
                        Group group = groupsIt.next();
                        group.removeMember(user);
                    }
                    for(String groupId: groups) {
                        Group group = (Group) userManager.getAuthorizable(groupId);
                        if (group == null) {
                            group = userManager.createGroup(groupId);
                        }
                        group.addMember(user);
                    }
                }
            }
            else {
                logger.error("Cannot find user with id {}.", userId);
            }
        } 
        catch (LoginException | RepositoryException e) {
            logger.error("Exception occurred when update user {}: {}.", userId, e.getMessage());
        }
        finally {
            if (session != null && session.isLive()) {
				session.logout();
			}
        }
    }

    @Override
    public void deleteUser(String userParam, boolean isPath) {
        Session session = null;
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, SERVICE_USER)) {
            logger.info("Start to delete user with param {}.", userParam);
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            session = resourceResolver.adaptTo(Session.class);
            User user;
            if (isPath) {
                user = (User) userManager.getAuthorizableByPath(userParam);
            }
            else {
               user = (User) userManager.getAuthorizable(userParam);
            }
            if (user != null) {
                String path = user.getPath();
                if (path.contains(OKTA_USER_PATH)) {
                    user.remove();
                }
                else {
                    logger.error("User with param {} cannot be deleted.", userParam);
                }
            }
            else {
                logger.error("Cannot find user with param {}.", userParam);
            }  
            session.save(); 
        } 
        catch (LoginException | RepositoryException e) {
            logger.error("Exception occurred when delete user with param {}: {}.", userParam, e.getMessage());
        }
        finally {
            if (session != null && session.isLive()) {
				session.logout();
			}
        }
    }

    @Override
    public User getUser(ResourceResolver resourceResolver, String userId) {
        try {
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            User user = (User) userManager.getAuthorizable(userId);
            if (user != null) {
                return user;
            }
            logger.error("Cannot find user with id {}.", userId);
            return null;
        }
        catch (RepositoryException e) {
            logger.error("Exception occurred when fetch user {}: {}.", userId, e.getMessage());
            return null;
        }
    }
    
}
