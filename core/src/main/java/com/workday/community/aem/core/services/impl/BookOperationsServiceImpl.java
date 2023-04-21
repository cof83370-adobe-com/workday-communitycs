package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.BookOperationsService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.utils.CommonUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class BookOperationsServiceImpl.
 */
@Component(
        // Provide the service property, and list of service interfaces if this
        // @Component should be registered as a service
        service = { BookOperationsService.class },

        // Set the configurationPolicy
        configurationPolicy = ConfigurationPolicy.OPTIONAL

)
public class BookOperationsServiceImpl implements BookOperationsService {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(BookOperationsServiceImpl.class);

    /** The Constant SERVICE_USER. */
    public static final String SERVICE_USER = "adminusergroup";

    /** The query service. */
    @Reference
    QueryService queryService;

    /**
     * Process book paths.
     *
     * @param resolver           the resolver
     * @param bookResourcePath   the book resource path
     * @param bookRequestJsonStr the book request json str
     * @return the sets the
     */
    @Override
    public Set<String> processBookPaths(ResourceResolver resolver, String bookResourcePath, String bookRequestJsonStr) {
        Set<String> activatePaths = new HashSet<>();
        try {
            Resource bookResource = resolver.getResource(bookResourcePath);
            if (bookResource != null) {
                bookResourcePath = bookResource.getPath().split(GlobalConstants.JCR_CONTENT_PATH)[0];
                // check incoming json String and create a JSON List object.
                if (StringUtils.isNotBlank(bookRequestJsonStr)) {
                    List<String> bookPathDataList = CommonUtils.getPathListFromJsonString(bookRequestJsonStr);

                    if (bookPathDataList == null || bookPathDataList.size() == 0) {
                        return activatePaths;
                    }

                    for (String bookPagePath : bookPathDataList) {
                        if (queryService != null) {
                            List<String> paths = queryService.getBookNodesByPath(bookPagePath, bookResourcePath);
                            for (String path : paths) {
                                if (resolver.getResource(path) != null) {
                                    Node root = resolver.getResource(path).adaptTo(Node.class);
                                    if (root != null) {
                                        activatePaths.add(root.getPath().split(GlobalConstants.JCR_CONTENT_PATH)[0]);
                                        root.remove();
                                    }
                                }
                            }
                        }
                    }
                    if (resolver.hasChanges()) {
                        resolver.commit();
                    }
                    logger.trace("processBook...completeBookData ", bookPathDataList);
                }
            }
        } catch (RepositoryException | PersistenceException e) {
            logger.error("Exception occurred when update book: {} ", e.getMessage());
        }
        return activatePaths;
    }
}