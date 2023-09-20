package com.workday.community.aem.core.workflows;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.GlobalConstants.RETIREMENT_STATUS_PROP;
import static com.workday.community.aem.core.constants.GlobalConstants.RETIREMENT_STATUS_VAL;

import java.util.List;
import java.util.Objects;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;

/**
 * The Class PageRetireProcessStep.
 * 
 * This step inclueds three things.
 * 1. Remove the payload page from book, if already part of any book.
 * 2. Add retirement badge.
 * 3. Replicate the page to publisher.
 */
@Component(property = {
        Constants.SERVICE_DESCRIPTION + "=Process to retire the given page",
        Constants.SERVICE_VENDOR + "=Workday Community",
        "process.label" + "=Retire the page"
})
public class PageRetireProcessStep implements WorkflowProcess {

    /** The Constant log. */
    private static final Logger logger = LoggerFactory.getLogger(PageRetireProcessStep.class);

    /** The cache manager. */
    @Reference
    CacheManagerService cacheManager;

    /** The query service. */
    @Reference
    QueryService queryService;

    /** The replicator. */
    @Reference
    private Replicator replicator;

    /**
     * Execute.
     *
     * @param workItem        the work item
     * @param workflowSession the workflow session
     * @param metaDataMap     the meta data map
     * @throws WorkflowException the workflow exception
     */
    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
            throws WorkflowException {
        final String payloadType = workItem.getWorkflowData().getPayloadType();
        logger.info("Payload type: {}", payloadType);
        if (StringUtils.equals(payloadType, "JCR_PATH")) {
            // Get the JCR path from the payload
            String path = workItem.getWorkflowData().getPayload().toString();
            try (ResourceResolver resolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
                removeBookNodes(resolver, path);
                addRetirementBadge(resolver, path);
                replicatePage(resolver, workflowSession, path);
            } catch (Exception e) {
                logger.error("payload type - {} is not valid", payloadType);
            }
        }
    }

    /**
     * Removes the book nodes.
     *
     * @param resolver the resolver
     * @param pagePath the page path
     */
    public void removeBookNodes(ResourceResolver resolver, String pagePath) {
        try {
            if (!pagePath.contains(GlobalConstants.JCR_CONTENT_PATH)) {
                List<String> paths = queryService.getBookNodesByPath(pagePath, null);
                paths.stream().filter(item -> resolver.getResource(item) != null)
                        .forEach(path -> {
                            try {
                                Node root = Objects.requireNonNull(resolver.getResource(path)).adaptTo(Node.class);
                                if (root != null) {
                                    root.remove();
                                }
                            } catch (Exception e) {
                                logger.error("Exception occured while removing the node: {}", path);
                            }
                        });
                logger.debug("Removed node for page {}", pagePath);
            }
        } catch (Exception exec) {
            logger.error("Exception occured while removing the: {} page from book node. Exception was: {} :", pagePath,
                    exec.getMessage());
        }
    }

    /**
     * Adds the retirement badge.
     *
     * @param resolver the resolver
     * @param pagePath the page path
     */
    public void addRetirementBadge(ResourceResolver resolver, String pagePath) {
        Resource resource = Objects.requireNonNull(resolver.getResource(pagePath + GlobalConstants.JCR_CONTENT_PATH));
        try {
            ModifiableValueMap map = resource.adaptTo(ModifiableValueMap.class);
            // Add retirement badge.
            map.put(RETIREMENT_STATUS_PROP, RETIREMENT_STATUS_VAL);
            resolver.commit();
        } catch (Exception exec) {
            logger.error("Exception occured while addRetirementBadge: {}", exec.getMessage());
        }
    }

    /**
     * Replicate page.
     *
     * @param resourceResolver the resource resolver
     * @param workflowSession  the workflow session
     * @param pagePath         the page path
     */
    public void replicatePage(ResourceResolver resourceResolver, WorkflowSession workflowSession, String pagePath) {
        try {
            if (replicator != null) {
                Session jcrSession = workflowSession.adaptTo(Session.class);
                replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, pagePath);
                resourceResolver.commit();
            }
        } catch (Exception e) {
            logger.info("Exception occured while replicatePage method: {}", e.getMessage());
        }
    }
}
