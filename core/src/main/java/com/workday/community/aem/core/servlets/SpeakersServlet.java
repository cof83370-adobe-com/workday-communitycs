package com.workday.community.aem.core.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.pojos.SpeakerPojo;
import com.workday.community.aem.core.pojos.Speakers;
import com.workday.community.aem.core.services.SpeakersApiConfigService;
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
 * The Class SpeakersServlet.
 *
 * @author Uttej
 */
@Component(service = Servlet.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Speakers Autocomplete Dropdown Service",
        "sling.servlet.paths=" + "/bin/speakers", "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class SpeakersServlet extends SlingSafeMethodsServlet {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SpeakersServlet.class);

    private transient ObjectMapper objectMapper = new ObjectMapper();

    private final transient Gson gson = new Gson();

    @Reference
    private transient SpeakersApiConfigService speakersApiConfigService;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Do get.
     *
     * @param request  the request
     * @param response the response
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        String json=null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String searchText = request.getParameter("searchText");
            Speakers speakers = getSpeakers(httpClient, searchText);

            if (speakers != null && speakers.getUsers().size() > 0) {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                 json = ow.writeValueAsString(speakers);
            }
            JsonObject jsonResponse = new JsonObject();
            response.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
            jsonResponse.addProperty("hits", json);
            response.getWriter().write(jsonResponse.toString());
        } catch (IOException e) {
            LOGGER.error("Error Occurred in DoGet Method in SpeakersServlet : {}", e.getMessage());
        }

    }

    private Speakers getSpeakers(CloseableHttpClient httpClient, String searchText) throws IOException {
        String endpoint = this.speakersApiConfigService.getSearchFieldLookupAPI();
        String consumerKey = this.speakersApiConfigService.getSearchFieldConsumerKey();
        String consumerSecret = this.speakersApiConfigService.getSearchFieldConsumerSecret();
        OAuth10AHeaderGenerator header = new OAuth10AHeaderGenerator(consumerKey, consumerSecret);

        try {
            HttpGet request = new HttpGet(endpoint+searchText);

            String headerString = header.generateHeader("GET", endpoint+searchText, new HashMap<>());
            request.addHeader(AUTHORIZATION, headerString);

            HttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                return objectMapper.readValue(response.getEntity().getContent(),
                        Speakers.class);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            LOGGER.error("Error Occurred in DoGet Method in SpeakersServlet : {}", e.getMessage());
        }
        LOGGER.error("Retrieve event type returns empty");
        return new Speakers();
    }
}