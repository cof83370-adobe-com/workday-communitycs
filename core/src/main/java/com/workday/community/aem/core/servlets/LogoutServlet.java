package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.workday.community.aem.core.constants.GlobalConstants;
import static com.workday.community.aem.core.constants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.HttpConstants.LOGIN_COOKIE_NAME;
import static com.workday.community.aem.core.constants.RestApiConstants.APPLICATION_SLASH_JSON;

/**
 * The user logout servlet class to redirect the action to okta logout.
 */
@Component(
  service = Servlet.class,
  property = {
    org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Logout Servlet",
      "sling.servlet.methods=" + HttpConstants.METHOD_GET,
      "sling.servlet.paths=" + "/bin/user/logout"
  }
)
public class LogoutServlet extends SlingAllMethodsServlet {

  /** The logger. */
  private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);

  /** The OktaService. */
  @Reference
  transient OktaService oktaService;

  /** The UserService. */
  @Reference
  UserService userService;

  /** The RunModeConfigService. */
  @Reference
  RunModeConfigService runModeConfigService;

  private transient final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void init() throws ServletException {
    super.init();
    logger.debug("initialize Logout service");
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws IOException {
    String utfName = StandardCharsets.UTF_8.name();
    response.setContentType(APPLICATION_SLASH_JSON);
    response.setCharacterEncoding(utfName);

    String oktaDomain = oktaService.getCustomDomain();
    String redirectUri = oktaService.getRedirectUri();

    if (StringUtils.isEmpty(oktaDomain) || StringUtils.isEmpty(redirectUri)) {
      logger.error("Okta domain and logout redirect Url are not configured, please contact admin.");
      return;
    }

    String logoutUrl = String.format("%s/login/signout?fromURI=%s", oktaDomain, redirectUri);

    // 1: Drop cookies
    // TODO need check in the future in case needs add more inclusion here.
    String[] deleteList = new String[] { LOGIN_COOKIE_NAME, COVEO_COOKIE_NAME };
    int count = HttpUtils.dropCookies(request, response, "/", deleteList);
    if (count == 0) {
      logger.debug("no custom cookie to be dropped");
    }

    // 2: Invalid current AEM session
    ResourceResolver resourceResolver = request.getResourceResolver();
    if (resourceResolver != null) {
      Session session = resourceResolver.adaptTo(Session.class);
      // Delete user on pubilsh instance.
      if (session != null) {
        if (runModeConfigService.getInstance().equals(GlobalConstants.PUBLISH)) { 
          String userId = session.getUserID();
          userService.deleteUser(userId);
        }
        session.logout();
      }
    }

    // 3: Redirect to Okta logout to invalid Okta session.
    response.sendRedirect(logoutUrl);
  }
}
