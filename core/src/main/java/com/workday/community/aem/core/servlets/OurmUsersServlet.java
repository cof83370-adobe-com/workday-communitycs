package com.workday.community.aem.core.servlets;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.DrupalService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OurmUsersServlet.
 *
 * @author Uttej
 */
@Component(service = Servlet.class, property = {
        Constants.SERVICE_DESCRIPTION + "= OurmUsers Autocomplete Dropdown Service",
        "sling.servlet.paths=" + "/bin/ourmUsers", "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class OurmUsersServlet extends SlingSafeMethodsServlet {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OurmUsersServlet.class);

    /** The DrupalService object. */
    @Reference
    private transient DrupalService drupalService;

    /**
     * Do get.
     *
     * @param request  the request
     * @param response the response
     * @throws ServletException the servlet exception
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException {
        try {
            String searchText = request.getParameter("searchText");
            JsonObject jsonObject = drupalService.searchOurmUserList(searchText);

            response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
            response.getWriter().write(jsonObject.toString());
        } catch (IOException | DrupalException e) {
            LOGGER.error("Error Occurred in DoGet Method in OurmUsersServlet : {}", e.getMessage());
        }
    }
}