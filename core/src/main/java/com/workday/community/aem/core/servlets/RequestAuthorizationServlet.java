package com.workday.community.aem.core.servlets;

import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_ERROR_PAGES_FORMAT;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_OKTA_USERS_ROOT_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_PUBLIC_ASSETS_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_PUBLIC_PAGE_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_ROOT_PAGE_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_SECURED_ASSETS_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_SECURED_DOCUMENTS_PATH;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.apache.sling.api.SlingHttpServletResponse.SC_FORBIDDEN;
import static org.apache.sling.api.SlingHttpServletResponse.SC_OK;

import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Session;
import javax.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Request authorization servlet.
 */
@Slf4j
@Component(service = Servlet.class, property = {
    Constants.SERVICE_DESCRIPTION + "= Authenticate the page based on tags added on the page.",
    "sling.servlet.paths=" + "/bin/workday/community/authcheck"
})
public class RequestAuthorizationServlet extends SlingSafeMethodsServlet {

  @Reference
  private transient UserGroupService userGroupService;

  @Reference
  private transient ResourceResolverFactory resolverFactory;

  /**
   * The UserService.
   */
  @Reference
  private transient UserService userService;

  @Override
  public void doHead(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws IOException {

    String uri = request.getParameter("uri").replace(".html", "");
    log.debug("Request URL {}", uri);
    if (StringUtils.isNotBlank(uri) && uri.contains(WORKDAY_ROOT_PAGE_PATH)
        && !uri.contains(WORKDAY_ERROR_PAGES_FORMAT)
        && !uri.contains(WORKDAY_PUBLIC_PAGE_PATH)) {
      log.debug("RequestAuthenticationServlet:Time before validating the user  is {}.",
          new Date().getTime());
      ResourceResolver requestResourceResolver = request.getResourceResolver();
      Session userSession = requestResourceResolver.adaptTo(Session.class);
      if (userSession == null) {
        response.setStatus(SC_FORBIDDEN);
        return;
      }
      Map<String, Object> serviceParams = new HashMap<>();
      serviceParams.put(ResourceResolverFactory.SUBSERVICE,
          WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE);
      ResourceResolver resourceResolver = null;
      try {
        log.debug("Inside Try block of Auth_Checker_Servlet");

        resourceResolver = resolverFactory.getServiceResourceResolver(serviceParams);
        boolean isValid = userGroupService.validateCurrentUser(request, uri);
        if (!isValid) {
          log.debug("user don't have access on the page {}", uri);
          response.setStatus(SC_FORBIDDEN);
          response.sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);

        } else {
          log.debug("user have access on the page {}", uri);
          response.setStatus(SC_OK);
        }
      } catch (LoginException e) {
        log.error("---> Exception occurred in RequestAuthenticationServlet: {}", e.getMessage());
        response.setStatus(SC_INTERNAL_SERVER_ERROR);
        response.sendRedirect(WccConstants.ERROR_PAGE_PATH);
      } finally {
        if (resourceResolver != null && resourceResolver.isLive()) {
          resourceResolver.close();
        }
      }
      log.debug("RequestAuthenticationServlet:Time after validating the user  is {}.",
          new Date().getTime());
    } else {
      handlePublicPagesAndAssets(uri, request, response);
    }

  }

  /**
   * Handles public pages and assets authentication.
   *
   * @param uri      URL of page/asset.
   * @param request  The Request Object.
   * @param response The Response Object.
   * @throws IOException Throws IOException.
   */
  private void handlePublicPagesAndAssets(String uri, SlingHttpServletRequest request,
                                          SlingHttpServletResponse response)
      throws IOException {
    if (isPublicPath(uri)) {
      log.debug("Requested page/asset is public page: {}", uri);
      response.setStatus(SC_OK);
    } else if (isPrivatePath(uri)) {
      log.debug("Requested Asset is Secured Asset: {}", uri);
      User user = null;
      try {
        user = userService.getCurrentUser(request);
        if (null != user && StringUtils.isNotBlank(user.getPath())
            && user.getPath().contains(WORKDAY_OKTA_USERS_ROOT_PATH)) {
          log.debug("Requested user has access on the page/asset: {}", uri);
          response.setStatus(SC_OK);
        } else {
          log.debug("Requested user has access on the page/asset: {}", uri);
          log.debug("Requested page/Asset is not in correct format: {}", uri);
          response.setStatus(SC_FORBIDDEN);
          response.sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
        }
      } catch (Exception e) {
        log.error("Getting the error While checking the user user authentication {}",
            e.getStackTrace());
      }
    } else {
      log.debug("Requested page/Asset is not in correct format: {}", uri);
      response.setStatus(SC_FORBIDDEN);
      response.sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
    }
  }

  /**
   * Checks Whether the give url is Public path or not.
   *
   * @param uri URL of page/asset.
   * @return boolean TRUE if it is public path. FALSE if it is not a public path.
   */
  private boolean isPublicPath(String uri) {
    return StringUtils.isNotBlank(uri) && (uri.contains(WORKDAY_PUBLIC_PAGE_PATH)
        || uri.contains(WORKDAY_PUBLIC_ASSETS_PATH)
        || uri.contains(WORKDAY_ERROR_PAGES_FORMAT));
  }


  /**
   * Checks Whether the give url is Private path or not.
   *
   * @param uri URL of page/asset.
   * @return boolean TRUE if it is private path. FALSE if it is not a private path.
   */
  private boolean isPrivatePath(String uri) {
    return (StringUtils.isNotBlank(uri) && uri.contains(WORKDAY_SECURED_ASSETS_PATH)
        || uri.contains(WORKDAY_SECURED_DOCUMENTS_PATH));
  }

}
