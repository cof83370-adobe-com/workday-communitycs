package com.workday.community.aem.core.servlets;

import java.util.HashMap;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.pojos.OurmUsers;
import com.workday.community.aem.core.services.OurmUsersApiConfigService;
import com.workday.community.aem.core.utils.OAuth10AHeaderGenerator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

import static com.workday.community.aem.core.constants.RestApiConstants.AUTHORIZATION;

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

    /** The object mapper. */
    private transient ObjectMapper objectMapper = new ObjectMapper();

    /** The OurmUsers api config service. */
    @Reference
    private transient OurmUsersApiConfigService ourmUsersApiConfigService;

    /**
     * Sets the object mapper.
     *
     * @param objectMapper the new object mapper
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Do get.
     *
     * @param request  the request
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        JsonObject jsonResponse = new JsonObject();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String searchText = request.getParameter("searchText");
            OurmUsers ourmUsers = getOurmUsers(httpClient, searchText);

            if (ourmUsers != null && ourmUsers.getUsers().size() > 0) {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(ourmUsers);
                jsonResponse.addProperty("hits", json);
            }

            response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
            response.getWriter().write(jsonResponse.toString());
        } catch (IOException e) {
            LOGGER.error("Error Occurred in DoGet Method in OurmUsersServlet : {}", e.getMessage());
        }

    }

    /**
     * Gets the ourmUsers.
     *
     * @param httpClient the http client
     * @param searchText the search text
     * @return the ourmUsers
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private OurmUsers getOurmUsers(CloseableHttpClient httpClient, String searchText) throws IOException {
        String endpoint = this.ourmUsersApiConfigService.getSearchFieldLookupAPI();
        String consumerKey = this.ourmUsersApiConfigService.getSearchFieldConsumerKey();
        String consumerSecret = this.ourmUsersApiConfigService.getSearchFieldConsumerSecret();
        OAuth10AHeaderGenerator header = new OAuth10AHeaderGenerator(consumerKey, consumerSecret);

        try {
            HttpGet request = new HttpGet(endpoint + searchText);

            String headerString = header.generateHeader("GET", endpoint + searchText, new HashMap<>());
            request.addHeader(AUTHORIZATION, headerString);

            HttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                return objectMapper.readValue(response.getEntity().getContent(),
                        OurmUsers.class);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            LOGGER.error("Error Occurred in DoGet Method in OurmUsersServlet : {}", e.getMessage());
        }
        LOGGER.error("Retrieve event type returns empty");
        return new OurmUsers();
    }
}