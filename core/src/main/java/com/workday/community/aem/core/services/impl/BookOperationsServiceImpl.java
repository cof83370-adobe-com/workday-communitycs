package com.workday.community.aem.core.services.impl;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.workday.community.aem.core.services.BookOperationsService;
import com.workday.community.aem.core.services.QueryService;

import acscommons.io.jsonwebtoken.lang.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(
        // Provide the service property, and list of service interfaces if this
        // @Component should be registered as a service
        service = { BookOperationsService.class },

        // Set the configurationPolicy
        configurationPolicy = ConfigurationPolicy.OPTIONAL

)
public class BookOperationsServiceImpl implements BookOperationsService {
    private static final Logger LOG = LoggerFactory.getLogger(BookOperationsServiceImpl.class);
    /**
     * The service user.
     */
    public static final String SERVICE_USER = "adminusergroup";

    /**
     * The resource resolver factory.
     */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Reference
    QueryService queryService;

    @Override
    public Set<String> processBookPaths(SlingHttpServletRequest req) {
        Set<String> activatePaths = new HashSet<>();
        boolean success = true; // We will set success as false on any failures or issues.
        try (ResourceResolver resourceResolver = req.getResourceResolver()) {
            LOG.trace("start processing book paths - success status: " + success);

            String bookResourcePath = req.getParameter("bookResPath");
            Resource bookResource = resourceResolver.getResource(bookResourcePath);
            if (bookResource != null) {
                bookResourcePath = bookResource.getPath().split("/jcr:content")[0];
                LOG.trace("processBookPaths...bookResPath ", bookResourcePath);

                // Get Book Path info from request.
                String bookRequestJsonStr = req.getParameter("bookPathData");

                LOG.trace("bookRequestJsonStr as request parameter: " + bookRequestJsonStr);
                // check incoming json String and create a JSON List object.
                if (StringUtils.isNotBlank(bookRequestJsonStr)) {
                    try {
                        List<String> bookPathDataList = getBookPathListFromJson(bookRequestJsonStr);

                        if (Collections.isEmpty(bookPathDataList)) {
                            success = false;
                        }

                        for (String bookPagePath : bookPathDataList) {
                            List<String> paths = queryService.getBookNodesByPath(bookPagePath, bookResourcePath);
                            for (String path : paths) {
                                if (resourceResolver.getResource(path) != null) {
                                    Node root = resourceResolver.getResource(path).adaptTo(Node.class);
                                    if (root != null) {
                                        activatePaths.add(root.getPath().split("/jcr:content")[0]);
                                        root.remove();
                                    }
                                }
                            }
                        }
                        if (resourceResolver.hasChanges()) {
                            resourceResolver.commit();
                        }
                        LOG.trace("processBook...completeBookData ", bookPathDataList);
                    } catch (Exception e) {
                        success = false;
                        LOG.trace("Exception while getting Json from String. success status: " + success);
                        LOG.error("Error creating json object.", e);
                    }
                } else {
                    success = false;
                    LOG.trace("bookRequestJsonStr is blank. success status: " + success);
                }
            }
        } catch (Exception e) {
            LOG.error("Exception occurred when update book: {} ", e.getMessage());
        }
        return activatePaths;
    }

    protected List<String> getBookPathListFromJson(String bookRequestJsonStr) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {
        }.getType();
        List<String> bookPathDataList = gson.fromJson(bookRequestJsonStr, type);
        return Optional.ofNullable(bookPathDataList).orElse(new ArrayList<>());
    }
}