package com.workday.community.aem.core.servlets;

import com.adobe.xfa.ut.StringUtils;
import com.google.gson.Gson;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.SubscriptionRequest;
import com.workday.community.aem.core.pojos.SubscriptionResponse;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.PageUtils;
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
 * The subscription servlet.
 */
@Slf4j
@Component(service = Servlet.class, property = {
    org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Subscribe Servlet",
    "sling.servlet.methods=[" + HttpConstants.METHOD_GET + "," + HttpConstants.METHOD_POST + "]",
    "sling.servlet.paths=" + "/bin/subscribe"
})
public class SubscribeServlet extends SlingAllMethodsServlet {
  @Reference
  private transient DrupalService drupalService;

  @Reference
  private transient UserService userService;

  @Reference
  private transient SearchApiConfigService searchApiConfigService;

  private final transient Gson gson = new Gson();

  @Override
  public void init() throws ServletException {
    super.init();
    log.debug("initialize Logout service");
  }

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws IOException {
    SubscriptionRequest requestObj = getSubscriptionRequest(request);
    if (!requestObj.isEmpty()) {
      try {
        boolean ret = drupalService.isSubscribed(requestObj.getId(), requestObj.getEmail());
        response.setStatus(HttpStatus.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(gson.toJson(new SubscriptionResponse(ret)));
      } catch (DrupalException e) {
        response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(new SubscriptionResponse(false)));
      }
    }
  }

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
    SubscriptionRequest requestObj = getSubscriptionRequest(request);
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
    }
  }

  private SubscriptionRequest getSubscriptionRequest(SlingHttpServletRequest request) {
    String pagePath = request.getPathInfo();
    pagePath = pagePath.substring(0, pagePath.indexOf("."));
    String pageUuid = PageUtils.getPageUuid(request.getResourceResolver(), pagePath);
    String sfId = OurmUtils.getSalesForceId(request, userService);
    String email = OurmUtils.getUserEmail(sfId, searchApiConfigService, drupalService);
    if (!StringUtils.isEmpty(pageUuid) && !StringUtils.isEmpty(email)) {
      return new SubscriptionRequest(pageUuid, email);
    }

    return new SubscriptionRequest();
  }
}
