package com.workday.community.aem.core.servlets;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Iterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;
import org.osgi.service.component.annotations.Component;

/**
 * The Class TemplatesListProviderServlet.
 *
 * @author pepalla
 */
@Slf4j
@Component(immediate = true, service = Servlet.class, property = {
  "sling.servlet.paths=/bin/renderrtetable",
  "sling.servlet.methods=GET"})
public class RteTableControlServlet extends SlingSafeMethodsServlet {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * {@inheritDoc}
   *
   * @throws IOException the IO exception
   * 
   */
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

    boolean allowed = false;
    UserManager userManager = request.getResourceResolver().adaptTo(UserManager.class);
    Session userSession = request.getResourceResolver().adaptTo(Session.class);
    String userId = requireNonNull(userSession).getUserID();
    Authorizable auth;
    try {
      auth = requireNonNull(userManager).getAuthorizable(userId);
      Iterator<Group> groups = requireNonNull(auth).memberOf();
      while (groups.hasNext() && !allowed) {
        Group g = groups.next();
        if (g.getID().equalsIgnoreCase("CMTY CC Admin {DEV}")) {
          allowed = true;
          break;
        }
      }
    } catch (RepositoryException e) {
      log.error("User not found");
    }
    JsonObject jsonResponse = new JsonObject();
    response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
    if (allowed) {
      jsonResponse.addProperty("renderTable", true);
    } else {
      jsonResponse.addProperty("renderTable", false);
    }
    response.getWriter().write(jsonResponse.toString());
  }
}
