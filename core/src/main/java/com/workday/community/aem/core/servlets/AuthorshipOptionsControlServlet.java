package com.workday.community.aem.core.servlets;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserService;
import java.io.IOException;
import java.util.Iterator;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
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
   * The run mode config service.
   */
  @Reference
  private transient UserService userService;

  /**
   * Do get method evaluates permissions of compoment controls 
   * and returns json containing 'render' key with boolean value true or false
   * in servlet ressponse.
   *
   * @param request       the sling http servlet request
   * @param response      the sling http servlet response
   * @throws IOException  the IO exception
   */
  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
    boolean isAllowed = false;
    String env = runModeConfigService.getEnv();
    env = env == null ? "local" : env;
    try {
      User user = userService.getCurrentUser(request);
      if (user != null) {
        isAllowed = hasAccessToTable(env, user.memberOf(), accessGroupsTextTable);
      }
    } catch (RepositoryException re) {
      log.error("Exception occurred while accessing user group ID {}", re);
    } catch (CacheException ce) {
      log.error("Exception occurred while getting the current user {}", ce);
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
   * @param tableAccessGroups     user groups that has access to table option
   * @throws RepositoryException  repository exception
   */
  private boolean hasAccessToTable(String environment, Iterator<Group> currentUserGroups, String[] tableAccessGroups)
      throws RepositoryException {
    while (currentUserGroups != null && currentUserGroups.hasNext()) {
      Group currentUserGroup = currentUserGroups.next();
      String currentUserGroupId = currentUserGroup.getID();
      for (String accessGroup : tableAccessGroups) {
        accessGroup = accessGroup.concat(" {").concat(environment).concat("}");
        if (currentUserGroupId != null && currentUserGroupId.equalsIgnoreCase(accessGroup)) {
          return true;
        }
      }
    }
    return false;
  }
}
