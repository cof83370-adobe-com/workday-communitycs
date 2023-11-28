package com.workday.community.aem.core.servlets;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.RunModeConfigService;
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
import org.osgi.service.component.annotations.Reference;

/**
 * The Class AuthorshipOptionsControlServlet.
 *
 * @author gopal.ramachandran
 */
@Slf4j
@Component(immediate = true, service = Servlet.class, property = {
    "sling.servlet.paths=/bin/workday/community/authorship", "sling.servlet.methods=GET" })
public class AuthorshipOptionsControlServlet extends SlingSafeMethodsServlet {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The user groups that should see table option in text component.
   */
  private static final String[] accessGroupsTextTable = { "CMTY CC Admin" };

  /**
   * The run mode config service.
   */
  @Reference
  private transient RunModeConfigService runModeConfigService;

  /**
   * Do get.
   *
   * @param request       the sling http servlet request
   * @param response      the sling http servlet response
   * @throws IOException  the IO exception
   */
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
    boolean isAllowed = false;
    UserManager userManager = request.getResourceResolver().adaptTo(UserManager.class);
    Session userSession = request.getResourceResolver().adaptTo(Session.class);
    String userId = requireNonNull(userSession).getUserID();
    String env = runModeConfigService.getEnv();
    env = env == null ? "local" : env;
    Authorizable auth;
    try {
      auth = requireNonNull(userManager).getAuthorizable(userId);
      Iterator<Group> currentUserGroups = requireNonNull(auth).memberOf();
      isAllowed = evaluateTableAccess(env, currentUserGroups);
    } catch (RepositoryException e) {
      log.error("User not found");
    }
    JsonObject jsonResponse = new JsonObject();
    response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
    jsonResponse.addProperty("render", isAllowed);
    response.getWriter().write(jsonResponse.toString());
  }

  /**
   * Evaluate access to enable table option for current user.
   *
   * @param environment           current AEM environment
   * @param currentUserGroups     user group iterator of current user
   * @param isAllowed             permission for viewing table option
   * @throws RepositoryException  repository exception
   */
  private boolean evaluateTableAccess(String environment, Iterator<Group> currentUserGroups)
      throws RepositoryException {
    while (currentUserGroups != null && currentUserGroups.hasNext()) {
      Group currentUserGroup = currentUserGroups.next();
      for (String accessGroup : accessGroupsTextTable) {
        accessGroup = accessGroup.concat(" {").concat(environment).concat("}");
        String currentUserGroupId = currentUserGroup.getID();
        if (currentUserGroupId != null && currentUserGroupId.equalsIgnoreCase(accessGroup)) {
          return true;
        }
      }
    }
    return false;
  }
}
