package com.workday.community.aem.core.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;

/**
 * The Class CoveoEventsTypeServlet.
 *
 * @author Thabrez
 */
@Component(service = Servlet.class, property = {Constants.SERVICE_DESCRIPTION + "= Coveo Events type Dropdown Service",
        "sling.servlet.paths=" + "/bin/coveoEventsDropdown", "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class CoveoEventsTypeServlet extends SlingSafeMethodsServlet {
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoveoEventsTypeServlet.class);
    
    /** The resource resolver. */
    transient ResourceResolver resourceResolver;
    
    /** The path resource. */
    transient Resource pathResource;
    
    /** The value map. */
    transient ValueMap valueMap;
    
    /** The resource list. */
    transient List<Resource> resourceList;
    
    /**
     * Do get.
     *
     * @param request the request
     * @param response the response
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            resourceResolver = request.getResourceResolver();
            resourceList = new ArrayList<>();
            //TODO: Coveo Service Integration - to fetch the event types dynamically
            Map<String, String > coveoEventsType = new HashMap<>();
            coveoEventsType.put("eventtype1","Event Type 1");
            coveoEventsType.put("eventtype2","Event Type 2");
            coveoEventsType.put("eventtype3","Event Type 3");
            coveoEventsType.put("eventtype4","Event Type 4");
            coveoEventsType.put("eventtype5","Event Type 5");
            Iterator<String> eventsTypeKeys = coveoEventsType.keySet().iterator();
            //Iterating JSON Objects over key
            while (eventsTypeKeys.hasNext()) {
                String eventKey = eventsTypeKeys.next();
                String eventValue = coveoEventsType.get(eventKey);
                valueMap = new ValueMapDecorator(new HashMap<>());
                valueMap.put("value", eventKey);
                valueMap.put("text", eventValue);
                resourceList.add(new ValueMapResource(resourceResolver, new ResourceMetadata(), "nt:unstructured", valueMap));
            }
            /*Create a DataSource that is used to populate the drop-down control*/
            DataSource dataSource = new SimpleDataSource(resourceList.iterator());
            request.setAttribute(DataSource.class.getName(), dataSource);
        } catch (Exception exception) {
            LOGGER.error("Error Occured in DoGet Method : {}", exception.getMessage());
        }
    }
}