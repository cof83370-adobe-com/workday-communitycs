package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.services.CoveoPushApiService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.PrintWriter;

@Component(service = {Servlet.class}, property = {"sling.servlet.paths=" + CoveoDeleteAllServlet.RESOURCE_PATH, "sling.servlet.methods=DELETE"})
public class CoveoDeleteAllServlet extends SlingAllMethodsServlet {

    /** The Constant RESOURCE_PATH. */
	public static final String RESOURCE_PATH = "/bin/coveo/delete-all";

    /** The push API service. */
    @Reference
    private CoveoPushApiService coveoPushApiService;

    /**
     * Delete all contents from coveo.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException
     */
    @Override
    public void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        PrintWriter printOut = response.getWriter();
        response.setContentType("text/html");
        coveoPushApiService.callDeleteAllItemsUri();
        printOut.append("Delete all request sent.");
    }

}
