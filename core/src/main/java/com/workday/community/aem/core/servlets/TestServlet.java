package com.workday.community.aem.core.servlets;

import javax.servlet.Servlet;

import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * The Class CoveoIndexAllServlet.
 */
@Component(service = {Servlet.class}, property = {"sling.servlet.paths=" + TestServlet.RESOURCE_PATH, "sling.servlet.methods=GET"})
public class TestServlet extends SlingAllMethodsServlet {

    /** The Constant RESOURCE_PATH. */
    public static final String RESOURCE_PATH = "/bin/testpage";

    @Reference
    private transient ExtractPagePropertiesService extractPagePropertiesService;

    /**
     * Servlet Get request handler.
     * <p>
     * For index all.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException
     */
    @Override
    public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        PrintWriter printOut = response.getWriter();
        response.setContentType("text/html");
        extractPagePropertiesService.extractPageProperties("/content/workday-community/en-us/test-event-2");
        printOut.append(String.format("Test page"));
    }

}
