package com.workday.community.aem.core.listeners;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.QueryService;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PageResourceListener.
 */
@Component(service = ResourceChangeListener.class, immediate = true, property = {
        ResourceChangeListener.PATHS + "=" + GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH,
        ResourceChangeListener.CHANGES + "=" + "REMOVED",
})

@ServiceDescription("PageResourceListener")
public class PageResourceListener implements ResourceChangeListener {
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(PageResourceListener.class);

    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;

    /** The resolver. */
    ResourceResolver resolver;

    /** The query service. */
    @Reference
    QueryService queryService;

    @Override
    public void onChange(List<ResourceChange> changes) {

        changes.forEach(change -> {
            removeBookNodes(change.getPath());
        });
    }

    /**
     * Removes the book nodes.
     *
     * @param pagePath the page path
     */
    public void removeBookNodes(String pagePath) {
        try {
            Map<String, Object> serviceParams = new HashMap<>();
            serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
            resolver = resolverFactory.getServiceResourceResolver(serviceParams);
            List<String> paths = queryService.getBookNodesByPath(pagePath, null);
            for (String path : paths) {
                if (resolver.getResource(path) != null) {
                    Node root = resolver.getResource(path).adaptTo(Node.class);
                    if (root != null) {
                        root.remove();
                    }
                }
            }
            if (resolver.hasChanges()) {
                resolver.commit();
            }
            logger.info("Removed node for page {}", pagePath);
        } catch (PersistenceException | RepositoryException | LoginException e) {
            logger.error("Can't remove found nodes for page {}", pagePath);
        } finally {
            if (resolver != null && resolver.isLive()) {
                logger.info("Final Block PageResourceListener");
                resolver.close();
                resolver = null;
            }
        }
    }
}