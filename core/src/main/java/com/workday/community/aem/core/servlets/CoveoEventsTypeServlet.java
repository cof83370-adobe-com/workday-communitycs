package com.workday.community.aem.core.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.workday.community.aem.core.services.SearchApiConfigService;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import static com.workday.community.aem.core.constants.RestApiConstants.APPLICATION_SLASH_JSON;
import static com.workday.community.aem.core.constants.RestApiConstants.CONTENT_TYPE;

/**
 * The Class CoveoEventsTypeServlet.
 *
 * @author Thabrez
 */
@Component(service = Servlet.class, property = {Constants.SERVICE_DESCRIPTION + "= Coveo Events type Dropdown Service",
        "sling.servlet.paths=" + "/bin/eventTypes", "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class CoveoEventsTypeServlet extends SlingSafeMethodsServlet {
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoveoEventsTypeServlet.class);
    private static final String EVENT_TYPE_CRITERIA = "?field=@commoneventtype";

    /** The path resource. */
    private transient Resource pathResource;

    @OSGiService
    private transient SearchApiConfigService searchConfigService;

    private transient ObjectMapper objectMapper = new ObjectMapper();

    /** The value map. */
    private transient ValueMap valueMap;
    
    /** The resource list. */
    private transient List<Resource> resourceList;

    @OSGiService
    private transient SearchApiConfigService searchApiConfigService;
    
    /**
     * Do get.
     *
     * @param request the request
     * @param response the response
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            ResourceResolver resourceResolver = request.getResourceResolver();
            resourceList = new ArrayList<>();

            CloseableHttpClient httpClient = HttpClients.createDefault();

            try {
                JsonArray eventTypes = getEventTypes(httpClient);
                while (eventTypes != null && eventTypes.iterator().hasNext()) {
                    valueMap = new ValueMapDecorator(new HashMap<>());
                    valueMap.put("value", eventTypes.iterator().next());
                    valueMap.put("text", eventTypes.iterator().next());
                    resourceList.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), "nt:unstructured", valueMap));
                }
            } finally {
                httpClient.close();
            }

            /*Create a DataSource that is used to populate the drop-down control*/
            DataSource dataSource = new SimpleDataSource(resourceList.iterator());
            request.setAttribute(DataSource.class.getName(), dataSource);
        } catch (Exception exception) {
            LOGGER.error("Error Occured in DoGet Method : {}", exception.getMessage());
        }
    }

    private JsonArray getEventTypes(CloseableHttpClient httpClient)  throws IOException  {
        String endpoint = this.searchApiConfigService.getSearchFieldLookupAPI();
        endpoint += EVENT_TYPE_CRITERIA;
        HttpGet request = new HttpGet(endpoint);
        request.addHeader(HttpConstants.HEADER_ACCEPT, APPLICATION_SLASH_JSON);
        request.addHeader(CONTENT_TYPE, APPLICATION_SLASH_JSON);
        HttpResponse response = httpClient.execute(request);
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK) {
            return objectMapper.readValue(response.getEntity().getContent(),
                JsonArray.class);
        }

        LOGGER.error("Retrieve event type returns empty");
        return new JsonArray();
    }
}