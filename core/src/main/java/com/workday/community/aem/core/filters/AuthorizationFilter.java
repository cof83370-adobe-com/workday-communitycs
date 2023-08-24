package com.workday.community.aem.core.filters;

import com.day.cq.wcm.api.constants.NameConstants;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import org.apache.jackrabbit.api.security.user.User;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.workday.community.aem.core.constants.WccConstants.*;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;


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

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private transient OktaService oktaService;

    @Reference
    private transient UserGroupService userGroupService;

    @Reference
    private transient UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.debug("AuthorizationFilter is initialized.");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        String pagePath = slingRequest.getRequestPathInfo().getResourcePath();
        logger.debug("Request for {}, with selector {}.", pagePath, slingRequest.getRequestPathInfo().getSelectorString());
        logger.debug("AuthorizationFilter: Time before validating the user is {}.", new Date().getTime());
        if (oktaService.isOktaIntegrationEnabled() &&
            pagePath.contains(WORKDAY_ROOT_PAGE_PATH) &&
            !pagePath.contains(WORKDAY_ERROR_PAGES_FORMAT) &&
            !pagePath.contains(WORKDAY_PUBLIC_PAGE_PATH)) {
            Map<String, Object> serviceParams = new HashMap<>();
            serviceParams.put(ResourceResolverFactory.SUBSERVICE, WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
            ResourceResolver requestResourceResolver = slingRequest.getResourceResolver();
            Session userSession = requestResourceResolver.adaptTo(Session.class);
            if (userSession == null) {
                ((SlingHttpServletResponse) response).sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
                return;
            }
            String userId = userSession.getUserID();
            logger.debug("Current user is {}.", userId);
            boolean isInValid = true;
            ResourceResolver resourceResolver = null;
            try {
                resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);
                User user = userService.getUser(resourceResolver, userId);
                if (null != user && user.getPath().contains(WORKDAY_OKTA_USERS_ROOT_PATH)) {
                    isInValid = userGroupService.validateTheUser(resourceResolver, requestResourceResolver, pagePath);
                }
                if (isInValid) {
                    ((SlingHttpServletResponse) response).setStatus(SC_FORBIDDEN);
                    ((SlingHttpServletResponse) response).sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
                }
            } catch (LoginException | RepositoryException e) {
                logger.error("---> Exception occurred in AuthorizationFilter: {}.", e.getMessage());
                ((SlingHttpServletResponse) response).setStatus(SC_INTERNAL_SERVER_ERROR);
                ((SlingHttpServletResponse) response).sendRedirect(WccConstants.ERROR_PAGE_PATH);
            } finally {
                if (resourceResolver != null && resourceResolver.isLive()) {
                    resourceResolver.close();
                }
            }
        }
        logger.debug("AuthorizationFilter:Time after validating the user  is {}.", new Date().getTime());
        filterChain.doFilter(request, response);
    }


    @Override
    public void destroy() {
        logger.debug("Destroy AuthorizationFilter.");
    }

}
