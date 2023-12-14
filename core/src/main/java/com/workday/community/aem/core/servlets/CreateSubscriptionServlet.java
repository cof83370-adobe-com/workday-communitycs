package com.workday.community.aem.core.servlets;

import static com.workday.community.aem.core.utils.HttpUtils.getSubscriptionRequest;

import com.google.gson.Gson;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.SubscriptionRequest;
import com.workday.community.aem.core.pojos.SubscriptionResponse;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The subscription servlet.
 */
@Slf4j
@Component(service = Servlet.class, property = {
    org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Subscribe Servlet",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/subscribe/create"
})
public class CreateSubscriptionServlet extends SlingAllMethodsServlet {
  @Reference
  private transient DrupalService drupalService;

  @Reference
  private transient UserService userService;

  @Reference
  private transient SearchApiConfigService searchApiConfigService;

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  private final transient Gson gson = new Gson();

  @Override
  public void init() throws ServletException {
    super.init();
    log.debug("initialize Logout service");
  }


  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
    SubscriptionRequest requestObj;
    try {
      requestObj = getSubscriptionRequest(
          request, userService, searchApiConfigService, drupalService, resourceResolverFactory);
    } catch (URISyntaxException | LoginException e) {
      throw new IOException(e);
    }
    if (!requestObj.isEmpty()) {
      try {
        boolean ret = drupalService.subscribe(requestObj.getId(), requestObj.getEmail());
        response.setStatus(HttpStatus.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(gson.toJson(new SubscriptionResponse(ret)));
      } catch (DrupalException | IOException e) {
        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(new SubscriptionResponse(false)));
      }
    } else {
      response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().write(gson.toJson(new SubscriptionResponse(false)));
    }
  }
}
