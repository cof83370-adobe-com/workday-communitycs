package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.ServletCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The search Token servlet class.
 */
@Slf4j
@Component(service = Servlet.class, property = {
    org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Search Token Servlet",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/search/token"
})
public class SearchTokenServlet extends SlingAllMethodsServlet {

  private final transient Gson gson = new Gson();

  @Reference
  private transient SearchApiConfigService searchApiConfigService;

  @Reference
  private transient DrupalService drupalService;

  @Reference
  private transient UserService userService;

  private transient ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Pass in ObjectMapper for the search service.
   *
   * @param objectMapper the pass-in ObjectMapper object.
   */
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws ServletException {
    super.init();
    log.debug("initialize Search token service");
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
    log.debug("Get search token call, method {}", request.getMethod());

    // In case user is not logged in, response with forbidden.
    if (HttpUtils.forbiddenResponse(request, response, this.userService)) {
      return;
    }

    ServletCallback servletCallback = (SlingHttpServletRequest req,
                                       SlingHttpServletResponse res, String body) -> {
      log.debug("inside getToken API callback with response; {} for request path: {}", body,
          req.getRequestPathInfo().getResourcePath());
      response.setStatus(HttpStatus.SC_OK);
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.getWriter().write(body);
      return body;
    };

    try {
      CoveoUtils.executeSearchForCallback(request, response, searchApiConfigService, drupalService,
          userService, gson, objectMapper, servletCallback);
    } catch (IOException | ServletException | DrupalException e) {
      log.error("get Token fails with error: {}", e.getMessage());
    }
  }

}
