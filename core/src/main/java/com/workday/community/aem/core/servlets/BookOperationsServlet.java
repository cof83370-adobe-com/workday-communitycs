package com.workday.community.aem.core.servlets;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.BookOperationsService;
import java.io.IOException;
import java.util.Set;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class BookOperationsServlet.
 */
@Slf4j
@Component(service = Servlet.class, property = {
    Constants.SERVICE_DESCRIPTION + "= Update Book Paths on the given page.",
    "sling.servlet.methods=" + HttpConstants.METHOD_POST,
    "sling.servlet.resourceTypes=" + "workday-community/components/common/book",
    "sling.servlet.extensions=" + "json"
})
public class BookOperationsServlet extends SlingAllMethodsServlet {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The book operations service.
   */
  @Reference
  private transient BookOperationsService bookOperationsService;


  /**
   * {@inheritDoc}
   */
  @Override
  protected void doPost(final SlingHttpServletRequest req, final SlingHttpServletResponse resp)
      throws ServletException, IOException {
    log.debug("Starting to process Book Paths");
    boolean success = true;

    // Get Book Resource Path info from request.
    String bookResourcePath = req.getParameter("bookResPath");
    // Get Book Path info from request.
    String bookRequestJsonStr = req.getParameter("bookPathData");

    Set<String> activatePaths =
        bookOperationsService.processBookPaths(req.getResourceResolver(), bookResourcePath,
            bookRequestJsonStr);

    JsonObject jsonResponse = new JsonObject();
    resp.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
    jsonResponse.addProperty("success", success);
    jsonResponse.addProperty("pagePaths", String.join(",", activatePaths));
    log.debug("Finished processing Book Paths");
    resp.getWriter().write(jsonResponse.toString());
  }
}
