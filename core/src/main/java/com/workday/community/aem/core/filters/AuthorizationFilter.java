package com.workday.community.aem.core.filters;

import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_ERROR_PAGES_FORMAT;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_OKTA_USERS_ROOT_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_PUBLIC_PAGE_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_ROOT_PAGE_PATH;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import com.day.cq.wcm.api.constants.NameConstants;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import java.io.IOException;
import java.util.Date;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.EngineConstants;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

  @Reference
  private transient OktaService oktaService;

  @Reference
  private transient UserGroupService userGroupService;

  @Reference
  private transient UserService userService;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    LOGGER.debug("AuthorizationFilter is initialized.");
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
                       final FilterChain filterChain) throws IOException, ServletException {
    final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
    String pagePath = slingRequest.getRequestPathInfo().getResourcePath();
    LOGGER.debug("Request for {}, with selector {}.", pagePath,
        slingRequest.getRequestPathInfo().getSelectorString());
    LOGGER.debug("AuthorizationFilter: Time before validating the user is {}.",
        new Date().getTime());
    if (oktaService.isOktaIntegrationEnabled() &&
        pagePath.contains(WORKDAY_ROOT_PAGE_PATH) &&
        !pagePath.contains(WORKDAY_ERROR_PAGES_FORMAT) &&
        !pagePath.contains(WORKDAY_PUBLIC_PAGE_PATH)) {
      ResourceResolver requestResourceResolver = slingRequest.getResourceResolver();
      Session userSession = requestResourceResolver.adaptTo(Session.class);
      if (userSession == null) {
        ((SlingHttpServletResponse) response).sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
        return;
      }
      String userId = userSession.getUserID();
      LOGGER.debug("Current user is {}.", userId);
      boolean isValid = false;
      try {
        User user = userService.getCurrentUser(slingRequest);
        if (null != user && user.getPath().contains(WORKDAY_OKTA_USERS_ROOT_PATH)) {
          isValid = userGroupService.validateCurrentUser(slingRequest, pagePath);
        }
        if (!isValid) {
          ((SlingHttpServletResponse) response).setStatus(SC_FORBIDDEN);
          ((SlingHttpServletResponse) response).sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
        }
      } catch (CacheException | RepositoryException e) {
        LOGGER.error("---> Exception occurred in AuthorizationFilter: {}.", e.getMessage());
        ((SlingHttpServletResponse) response).setStatus(SC_INTERNAL_SERVER_ERROR);
        ((SlingHttpServletResponse) response).sendRedirect(WccConstants.ERROR_PAGE_PATH);
      }
    }
    LOGGER.debug("AuthorizationFilter:Time after validating the user  is {}.",
        new Date().getTime());
    filterChain.doFilter(request, response);
  }


  @Override
  public void destroy() {
    LOGGER.debug("Destroy AuthorizationFilter.");
  }

}
