package com.workday.community.aem.core.filters;


import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.constants.NameConstants;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserGroupService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.*;
import java.io.IOException;
import java.util.*;


/**
 * Authorization servlet filter component that checks the authorization for incoming requests.
 */

@Component(service = Filter.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "= Workday Authorization filter incoming requests",
                Constants.SERVICE_RANKING + ":Integer=1",
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_REQUEST,
                EngineConstants.SLING_FILTER_RESOURCETYPES + "=" + NameConstants.NT_PAGE,
                EngineConstants.SLING_FILTER_PATTERN + "=/content/workday-community/(.*)",
                EngineConstants.SLING_FILTER_EXTENSIONS + "=html"
        })
public class AuthorizationFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private ResourceResolverFactory resolverFactory;

    private transient ResourceResolver resourceResolver;

    private transient Session session;

    @Reference
    transient OktaService oktaService;

    @Reference
    transient UserGroupService userGroupService;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {

        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        String pagePath = slingRequest.getRequestPathInfo().getResourcePath();
        logger.error("request for {}, with selector {}", pagePath, slingRequest.getRequestPathInfo().getSelectorString());

        if (oktaService.isOktaIntegrationEnabled() && pagePath.contains("/content/workday-community") && !pagePath.contains("/errors/")) {

            Map<String, Object> serviceParams = new HashMap<>();
            serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
            Session jcrSession = slingRequest.getResourceResolver().adaptTo(Session.class);
            String userId = jcrSession.getUserID();
            boolean isValid = true;
            try {
                resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);

                UserManager userManager = resourceResolver.adaptTo(UserManager.class);
                Authorizable user = userManager.getAuthorizable(userId);

                if (null != user) {
                    isValid = validateTheUser(pagePath);
                } else {
                    logger.error("current user  {}", userId);
                }

                if (!isValid) {
                    SlingHttpServletResponse slingHttpServletResponse = (SlingHttpServletResponse) response;
                    slingHttpServletResponse.sendRedirect("/content/workday-community/en-us/errors/403.html");
                }

            } catch (LoginException | RepositoryException e) {
                logger.error("---> Exception.. {}", e.getMessage());
            } finally {
                if (resourceResolver != null && resourceResolver.isLive()) {
                    resourceResolver.close();
                    resourceResolver = null;
                }
                if (session != null && session.isLive()) {
                    session.logout();
                    session = null;
                }
                if (jcrSession != null && jcrSession.isLive()) {
                    jcrSession.logout();
                    jcrSession = null;
                }

            }

        }

        filterChain.doFilter(request, response);
    }


    private boolean validateTheUser(String pagePath) {
        logger.error(" inside assignTheUserToGroup");
        boolean isValid = true;
        try {
            session = resourceResolver.adaptTo(Session.class);
            if (session.itemExists(pagePath)) {

                PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                Page pageObject = pageManager.getPage(pagePath);
                if (null != pageObject) {
                    List<String> tagsList = getTagIds(pageObject);
                    logger.error("---> Tags List.. {}", StringUtils.join(tagsList,","));
                    List<String> groupsList = userGroupService.getLoggedInUsersGroups();
                    logger.error("---> Groups List.. {}", StringUtils.join(groupsList,","));
                    if (!(tagsList.contains("everyone") || !Collections.disjoint(tagsList,groupsList))) {
                        isValid = false;
                    }
                }

            }


        } catch (Exception e) {
            logger.error("---> Not able to perform User Management..");
            logger.error("---> Exception.. {}", e.getMessage());
        }

        return isValid;
    }

    private List<String> getTagIds(Page page) {
        final List<String> tagIds = new ArrayList<>();
        for (Tag tag : page.getTags()) {
            tagIds.add(tag.getName());
        }
        return tagIds;
    }

    @Override
    public void destroy() {
        // do nothing
    }


}