package com.workday.community.aem.core.servlets;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.pojos.OurmUserList;
import com.workday.community.aem.core.services.OurmUserService;
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

    /** The OurmUsers api config service. */
    @Reference
    private transient OurmUserService ourmUserService;

    /**
     * Do get.
     *
     * @param request  the request
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException      Signals that an I/O exception has occurred.
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        JsonObject jsonResponse = new JsonObject();

        try {
            String searchText = request.getParameter("searchText");
            OurmUserList ourmUsers = ourmUserService.searchOurmUserList(searchText);

            if (ourmUsers != null && ourmUsers.getUsers().size() > 0) {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(ourmUsers);
                jsonResponse.addProperty("hits", json);
            }

            response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
            response.getWriter().write(jsonResponse.toString());
        } catch (IOException | OurmException e) {
            LOGGER.error("Error Occurred in DoGet Method in OurmUsersServlet : {}", e.getMessage());
        }
    }
}