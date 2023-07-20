package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.services.UserGroupService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.workday.community.aem.core.constants.WccConstants.*;
import static org.apache.sling.api.SlingHttpServletResponse.SC_FORBIDDEN;
import static org.apache.sling.api.SlingHttpServletResponse.SC_OK;

@Component(service = Servlet.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Authenticate the page based on tags added on the page.",
        "sling.servlet.paths=" + "/bin/workday/community/authcheck"
})
public class RequestAuthenticationServlet extends SlingSafeMethodsServlet {

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RequestAuthenticationServlet.class);

    @Reference
    private transient UserGroupService userGroupService;

    @Reference
    private transient ResourceResolverFactory resolverFactory;

    @Override
    public void doHead(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        String uri = request.getParameter("uri").replace(".html", "");
        logger.debug("Request URL {}", uri);
        if (StringUtils.isNotBlank(uri) && uri.contains(WORKDAY_ROOT_PAGE_PATH) &&
                !uri.contains(WORKDAY_ERROR_PAGES_FORMAT) &&
                !uri.contains(WORKDAY_PUBLIC_PAGE_PATH)) {
            logger.debug("RequestAuthenticationServlet:Time before validating the user  is {}.", new Date().getTime());
            ResourceResolver requestResourceResolver = request.getResourceResolver();
            Session userSession = requestResourceResolver.adaptTo(Session.class);
            if (userSession == null) {
                response.setStatus(SC_FORBIDDEN);
                return;
            }
            Map<String, Object> serviceParams = new HashMap<>();
            serviceParams.put(ResourceResolverFactory.SUBSERVICE, WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
            ResourceResolver resourceResolver = null;
            try {
                logger.debug("Inside Try block of Auth_Checker_Servlet");

                resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);
                boolean isInValid = userGroupService.validateTheUser(resourceResolver, requestResourceResolver, uri);
                if (isInValid) {
                    logger.debug("user don't have access on the page {}", uri);
                    response.setStatus(SC_FORBIDDEN);
                    response.sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);

                } else {
                    logger.debug("user have access on the page {}", uri);
                    response.setStatus(SC_OK);
                }
            } catch (Exception e) {
                logger.error("auth checker says READ access DENIED!");
                response.setStatus(SC_FORBIDDEN);
                response.sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
            } finally {
                if (resourceResolver != null && resourceResolver.isLive()) {
                    resourceResolver.close();
                }
            }
            logger.debug("RequestAuthenticationServlet:Time after validating the user  is {}.", new Date().getTime());
        }
        else if(uri.contains(WORKDAY_PUBLIC_PAGE_PATH))
        {
            response.setStatus(SC_OK);
        }
        else {
            logger.debug("Requested page is not in correct format: {}", uri);
            response.setStatus(SC_FORBIDDEN);
        }
    }
}