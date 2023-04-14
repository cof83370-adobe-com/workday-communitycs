package com.workday.community.aem.core.servlets;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.google.gson.JsonObject;

import com.workday.community.aem.core.services.BookOperationsService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.post.JSONResponse;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Component(service = Servlet.class, 	property={
        Constants.SERVICE_DESCRIPTION + "= Update Book Paths on the given page.",
        "sling.servlet.methods=" + HttpConstants.METHOD_POST,
        "sling.servlet.paths="+ "/bin/processBookPages",
        "sling.servlet.extensions=" + "json"
})

public class BookOperationsServlet extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(BookOperationsServlet.class);

    private static final long serialVersionUID = 1L;

    @Reference
    private BookOperationsService bookOperationsService;

    @Override
    protected void doGet(final SlingHttpServletRequest req, final SlingHttpServletResponse resp) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        LOG.debug("Starting to process Book Paths");
        boolean success = true;
        Set<String> activatePaths = new HashSet<>();
        LOG.trace("Initial success status: " + success);
        try {
            activatePaths = bookOperationsService.processBookPaths(req);
        } catch (Exception e) {
            success = false;
            LOG.debug("Error while processing Book Paths - success status: " + success);
            LOG.error("Error while processing Book Paths :" + req.getResource().getPath());
        }

        long end = System.currentTimeMillis();
        LOG.debug("Time for Book Paths processing: " + Long.toString(end-start) + " ms");
        JsonObject jsonResponse = new JsonObject();
        resp.setContentType(JSONResponse.RESPONSE_CONTENT_TYPE);
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("pagePaths", String.join(",", activatePaths));
        LOG.debug("Finished processing Book Paths");
        resp.getWriter().write(jsonResponse.toString());
    }

    @Override
    protected void doPost(final SlingHttpServletRequest req, final SlingHttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

}