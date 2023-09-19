package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import org.apache.sling.api.auth.Authenticator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.workday.community.aem.core.constants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.HttpConstants.LOGIN_COOKIE_NAME;
import static org.apache.oltu.oauth2.common.OAuth.ContentType.JSON;

/**
 * The user logout servlet class to redirect the action to okta logout.
 */
@Component(service = Servlet.class, property = {
    org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Logout Servlet",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/user/logout"
})
public class LogoutServlet extends SlingAllMethodsServlet {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(LogoutServlet.class);

  /** The OktaService. */
  @Reference
  private transient OktaService oktaService;

  @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
  private transient volatile Authenticator authenticator;

  /** The UserService. */
  @Reference
  private transient UserService userService;

  /** The RunModeConfigService. */
  @Reference
  private transient RunModeConfigService runModeConfigService;

  private transient final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void init() throws ServletException {
    super.init();
    LOGGER.debug("initialize Logout service");
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws IOException {
    String utfName = StandardCharsets.UTF_8.name();
    response.setContentType(JSON);
    response.setCharacterEncoding(utfName);

    String oktaDomain = oktaService.getCustomDomain();
    boolean isOktaEnabled = oktaService.isOktaIntegrationEnabled();

    if (isOktaEnabled && StringUtils.isEmpty(oktaDomain)) {
      LOGGER.error("Okta domain and logout redirect Url are not configured, please contact admin.");
      return;
    }

    String logoutUrl = String.format("%s/bin/user/logout", oktaDomain);

    // 1: Drop cookies
    String[] deleteList = new String[] { LOGIN_COOKIE_NAME, COVEO_COOKIE_NAME };
    int count = HttpUtils.dropCookies(request, response, "/", deleteList);
    if (count == 0) {
      LOGGER.debug("no custom cookie to be dropped");
    }

    // 2: Invalid current AEM session
    try {
      userService.invalidCurrentUser(request, false);
    } catch (CacheException e) {
      LOGGER.error("delete user from JCR cache failed during logout");
    }
    if (isOktaEnabled) {
      // case 3: Redirect to okta logout directly in case session expired.
      response.sendRedirect(logoutUrl);
    }
    if (this.authenticator != null) {
      authenticator.logout(request, response);
    }
  }
}
