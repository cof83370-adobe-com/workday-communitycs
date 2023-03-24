package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.utils.HttpUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.workday.community.aem.core.constants.RestApiConstants.APPLICATION_SLASH_JSON;

/**
 * The user logout servlet class to redirect the action to okta logout.
 */
@Component(
  service = Servlet.class,
  property = {
    org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Logout Servlet",
      "sling.servlet.methods=" + HttpConstants.METHOD_GET,
      "sling.servlet.paths=" + "/user/logout"
  }
)
public class LogoutServlet extends SlingAllMethodsServlet {
  private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);

  @Reference
  transient OktaService oktaService;

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

    String logoutUrl = String.format("%s/login/signout?fromURI=%s",
      oktaService.getCustomDomain(),
      oktaService.getRedirectUri()
    );
    response.sendRedirect(logoutUrl);
    int count = HttpUtils.dropCookies(request, response, "/");
    if (count == 0) {
      logger.debug("no custom cookie to be dropped");
    }
  }
}
