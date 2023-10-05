package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.ServletCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The search Token servlet class.
 */
@Component(
    service = Servlet.class,
    property = {
        org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Search Token Servlet",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=" + "/bin/search/token"
    }
)
public class SearchTokenServlet extends SlingAllMethodsServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchTokenServlet.class);

  @Reference
  private transient SearchApiConfigService searchApiConfigService;

  @Reference
  private transient SnapService snapService;

  @Reference
  private transient UserService userService;

  private transient ObjectMapper objectMapper = new ObjectMapper();

  private final transient Gson gson = new Gson();

  /**
   * Pass in ObjectMapper for the search service.
   * @param objectMapper the pass-in ObjectMapper object.
   */
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void init() throws ServletException {
    super.init();
    LOGGER.debug("initialize Search token service");
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * Implementation of the servlet GET method
   * @param request The HttpServletRequest object.
   * @param response The HttpServletResponse object.
   * @throws ServletException if the method call fails with ServletException.
   */
  @Override
  protected void doGet(SlingHttpServletRequest request,
                       SlingHttpServletResponse response) {
    LOGGER.debug("Get search token call, method {}", request.getMethod());
    ServletCallback servletCallback = (SlingHttpServletRequest req,
        SlingHttpServletResponse res, String body) -> {
      LOGGER.debug("inside getToken API callback with response; {}", body);
      response.setStatus(HttpStatus.SC_OK);
      response.setContentType("application/json");
      response.setCharacterEncoding(StandardCharsets.UTF_8.name());
      response.getWriter().write(body);
      return body;
    };

    try {
      CoveoUtils.executeSearchForCallback(request, response, searchApiConfigService, snapService, userService, gson, objectMapper, servletCallback);
    } catch (IOException | ServletException e) {
      LOGGER.error("get Token fails with error: {}", e.getMessage());
    }
  }
}
