package com.workday.community.aem.core.filters;

import com.day.cq.wcm.api.constants.NameConstants;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.PageUtils;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.workday.community.aem.core.constants.WccConstants.*;


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

    private transient ResourceResolver resourceResolver;

    private transient ResourceResolver requestResourceResolver;

    @Reference
    transient OktaService oktaService;

    @Reference
    transient UserGroupService userGroupService;

    @Reference
    transient UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.debug("AuthorizationFilter is initialized");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        String pagePath = slingRequest.getRequestPathInfo().getResourcePath();
        logger.debug("request for {}, with selector {}", pagePath, slingRequest.getRequestPathInfo().getSelectorString());
        if (oktaService.isOktaIntegrationEnabled() && pagePath.contains(WORKDAY_ROOT_PAGE_PATH) &&
                !pagePath.contains(WORKDAY_ERROR_PAGES_FORMAT)) {
            Map<String, Object> serviceParams = new HashMap<>();
            serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
            requestResourceResolver = slingRequest.getResourceResolver();
            Session userSession = requestResourceResolver.adaptTo(Session.class);
            String userId = userSession.getUserID();
            logger.debug("current user  {}", userId);
            boolean isInValid = true;
            try {
                resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);
                User user = userService.getUser(resourceResolver, userId);
                if (null != user && user.getPath().contains(WORKDAY_OKTA_USERS_ROOT_PATH)) {
                    isInValid = validateTheUser(pagePath);
                }
                if (isInValid) {
                    ((SlingHttpServletResponse) response).sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
                }
            } catch (LoginException | RepositoryException e) {
                logger.error("---> Exception in AuthorizationFilter.. {}", e.getMessage());
            } finally {
                if (resourceResolver != null && resourceResolver.isLive()) {
                    resourceResolver.close();
                    resourceResolver = null;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Validates the user based on Roles tagged to the page and User roles from Salesforce.
     *
     * @param pagePath : The Requested page path.
     * @return boolean: True if user has permissions otherwise false.
     */
    private boolean validateTheUser(String pagePath) {
        logger.debug(" inside validateTheUser method-->");
        boolean isInValid = true;
        try {
            List<String> pageTagsTitlesList = PageUtils.getPageTagsTitleList(pagePath, resourceResolver);
            if (!pageTagsTitlesList.isEmpty()) {
                logger.debug("---> Tags List.. {}", pageTagsTitlesList);
                if (pageTagsTitlesList.contains(EVERYONE)) {
                    isInValid = false;
                } else {
                    List<String> groupsList = userGroupService.getLoggedInUsersGroups(requestResourceResolver);
                    logger.debug("---> Groups List..{}", groupsList);
                    if (!Collections.disjoint(pageTagsTitlesList, groupsList)) {
                        isInValid = false;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("---> Exception.. {}", e.getMessage());
        }

        return isInValid;
    }

    @Override
    public void destroy() {
        logger.debug("calling AuthorizationFilter destroy()");
        if (resourceResolver != null && resourceResolver.isLive()) {
            resourceResolver.close();
            resourceResolver = null;
        }
    }

}