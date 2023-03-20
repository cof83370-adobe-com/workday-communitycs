package com.workday.community.aem.core.filters;


import java.io.IOException;

import javax.jcr.*;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;

import org.apache.sling.api.SlingHttpServletRequest;

import org.apache.sling.engine.EngineConstants;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;


import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

/**
 * Simple servlet filter component that logs incoming requests.
 */
@Component(service = Filter.class,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Demo to filter incoming requests",
        EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_REQUEST,
        Constants.SERVICE_RANKING + ":Integer=1"
      
    })
public class LoggingFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String groupName = "outside-okta";


    @Reference
    ResourceResolverFactory resourceResolverFactory;
    
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
      
      final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
      logger.error("request for {}, with selector {}",
              slingRequest.getRequestPathInfo().getResourcePath(),
              slingRequest.getRequestPathInfo().getSelectorString());

      if(slingRequest.getRequestPathInfo().getResourcePath().contains("/content/workday-community"))
      {
          ResourceResolver resolver = slingRequest.getResourceResolver();
          Session jcrSession = resolver.adaptTo(Session.class);
          String userId = jcrSession.getUserID();
          if(null!= userId && userId!= "admin" &&  userId!= "anonymous" )
          {
              assignTheUserToGroup(userId);
          }
          else {
              logger.error("current user  {}", userId);
          }

      }

      filterChain.doFilter(request, response);
    }
    
    @Override
    public void init(FilterConfig filterConfig) {
    }
    
    @Override
    public void destroy() {
    }


    private void assignTheUserToGroup(String userName)
    {
        logger.error(" inside assignTheUserToGroup");
        Session session = null;
        ResourceResolver resourceResolver = null;
        try {

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
