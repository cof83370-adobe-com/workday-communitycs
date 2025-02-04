package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.IndexServices;
import com.workday.community.aem.core.services.QueryService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.Servlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class CoveoIndexAllServlet.
 */
@Component(service = {Servlet.class}, property = {
    "sling.servlet.paths=" + CoveoIndexAllServlet.RESOURCE_PATH, "sling.servlet.methods=POST"})
public class CoveoIndexAllServlet extends SlingAllMethodsServlet {

  /**
   * The Constant RESOURCE_PATH.
   */
  public static final String RESOURCE_PATH = "/bin/coveo/index-all";

  /**
   * Index service.
   */
  @Reference
  private transient IndexServices indexServices;

  /**
   * Query service.
   */
  @Reference
  private transient QueryService queryService;

  /**
   * The CoveoIndexApiConfigService service.
   */
  @Reference
  private transient CoveoIndexApiConfigService coveoIndexApiConfigService;

  /**
   * Servlet POST request handler.
   * For index all.
   *
   * @param request  The HTTP request
   * @param response The HTTP response
   * @throws IOException throws IOException.
   */
  @Override
  public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws IOException {
    if (Boolean.FALSE.equals(coveoIndexApiConfigService.isCoveoIndexEnabled())) {
      return;
    }
    PrintWriter printOut = response.getWriter();
    response.setContentType("text/html");
    String[] templates = request.getParameterValues("templates");
    if (templates == null || templates.length == 0) {
      printOut.append("Missing template value.");
      return;
    }
    List<String> paths = queryService.getPagesByTemplates(templates);
    if (paths.isEmpty()) {
      printOut.append("No matching items found.");
      return;
    }

    indexServices.indexPages(paths);
    printOut.append(String.format("%d number of content(s) indexed.", paths.size()));
  }

}
