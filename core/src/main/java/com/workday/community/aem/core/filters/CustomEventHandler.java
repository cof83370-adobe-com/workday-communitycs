package com.workday.community.aem.core.filters;
import java.util.HashMap;
import java.util.Map;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

@Component(immediate = true, service = EventHandler.class, property = {
        Constants.SERVICE_DESCRIPTION + "= This event handler listens the events on page activation",
        EventConstants.EVENT_TOPIC + "=org/apache/sling/api/resource/Resource/ADDED",
        EventConstants.EVENT_TOPIC + "=org/apache/sling/api/resource/Resource/CHANGED",
        EventConstants.EVENT_FILTER + "=(path=/home/users/workdaycommunity/okta/*)"

})
public class CustomEventHandler  implements EventHandler {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CustomEventHandler.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    private String groupName = "outside-okta";
    private ResourceResolver resolver;

    @Reference
    JobManager jobManager;

    @Override
    public void handleEvent(Event event) {

        Session session = null;
        ResourceResolver resourceResolver = null;
        try {

            logger.info("Event properties : {}", event.getPropertyNames());
            String[] props = event.getPropertyNames();
            String resourcePath = event.getProperty("path").toString();




        Map<String, Object> param = new HashMap<String, Object>();
        param.put(ResourceResolverFactory.SUBSERVICE, "workday-content-writer-service");
        resourceResolver = resourceResolverFactory.getServiceResourceResolver(param);
        session = resourceResolver.adaptTo(Session.class);

        logger.error(" inside assignTheUserToGroup session created");
        // Create UserManager Object
        final UserManager userManager = AccessControlUtil.getUserManager(session);

        // Create a Group
        Group group = null;
        if (userManager.getAuthorizable(groupName) == null) {
            group = userManager.createGroup(groupName);
            logger.error(" inside assignTheUserToGroup userManager");
            ValueFactory valueFactory = session.getValueFactory();
            Value groupNameValue = valueFactory.createValue(groupName, PropertyType.STRING);
            group.setProperty("./profile/givenName", groupNameValue);
            session.save();

            logger.error("---> {} Group successfully created.", group.getID());
        } else {
            logger.error("---> Group already exist..");
        }

        // Add User to Group
        Group addUserToGroup = (Group) (userManager.getAuthorizable(groupName));
        addUserToGroup.addMember(userManager.getAuthorizable(userName));
        session.save();

        logger.error("---> {} User successfully created and added into group.", userName);


    } catch (Exception e) {
        logger.error("---> Not able to perform User Management..");
        logger.error("---> Exception.." + e.getMessage());
    } finally {
        if(resourceResolver!= null & resourceResolver.isLive())
        {
            resourceResolver.close();
            resourceResolver =null;
        }
        if(session!= null & session.isLive())
        {
            session.logout();
            session =null;
        }
    }


    }

}