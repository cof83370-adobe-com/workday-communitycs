package com.workday.community.aem.core.workflows;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.metadata.SimpleMetaDataMap;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.AssetReferenceSearch;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class ContentPublishingWorkflowProcessTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class ContentPublishingWorkflowProcessTest {

    /** The context. */
    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    /** The session. */
    private final Session session = context.resourceResolver().adaptTo(Session.class);

    /** The retire process step. */
    @InjectMocks
    ContentPublishingWorkflowProcess cpwProcessStep;

    /** The workflow session. */
    @Mock
    private WorkflowSession workflowSession;

    /** The workflow. */
    @Mock
    private Workflow workflow;

    /** The workflow data. */
    @Mock
    private WorkflowData workflowData;

    /** The query service. */
    @Mock
    private QueryService queryService;

    /** The cache manager. */
    @Mock
    CacheManagerService cacheManager;

    /** The replicator. */
    @Mock
    Replicator replicator;

    /** The resolver. */
    @Mock
    private ResourceResolver resolver;

    /** The work item. */
    @Mock
    private WorkItem workItem;

    /** The meta data. */
    private final MetaDataMap metaData = new SimpleMetaDataMap();

    /**
     * Setup.
     *
     * @throws LoginException the login exception
     */
    @BeforeEach
    void setup() throws LoginException {
        lenient().when(workflowSession.adaptTo(Session.class)).thenReturn(session);
        lenient().when(workflow.getMetaDataMap()).thenReturn(metaData);
        lenient().when(workflowData.getPayloadType()).thenReturn("JCR_PATH");
        lenient().when(workItem.getWorkflow()).thenReturn(workflow);
        lenient().when(workItem.getWorkflowData()).thenReturn(workflowData);

        assertNotNull(session);

        context.registerAdapter(WorkflowSession.class, Session.class, session);
        context.load().json(
                "/com/workday/community/aem/core/models/impl/ContentPublishingWorkflowProcessTestData.json",
                "/content");
        context.registerService(ResourceResolver.class, resolver);
        context.registerService(QueryService.class, queryService);
        context.registerService(Replicator.class, replicator);
        Page currentPage = context.currentResource("/content/process-publish-content").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        lenient().when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resolver);
    }

    /**
     * My workflow with retired page tag true.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteMethod() throws Exception {
        lenient().when(workflowData.getPayload()).thenReturn("/content/process-publish-content");
        Node node = session.getNode("/content/process-publish-content/jcr:content");
        assertNotNull(node);
        cpwProcessStep.execute(workItem, workflowSession, metaData);
        assertNotNull(session);

    }

    /**
     * Test replicate page.
     *
     * @throws Exception the exception
     */
    @Test
    void testReplicatePage() throws Exception {
    	Resource resource = mock(Resource.class);
        lenient().when(resolver.getResource(anyString())).thenReturn(resource);
    	Node node = mock(Node.class);
        lenient().when(resource.adaptTo(Node.class)).thenReturn(node);
        AssetReferenceSearch ref = new AssetReferenceSearch(node,DamConstants.MOUNTPOINT_ASSETS,resolver);
        Property pt = mock(Property.class);
        PropertyIterator pIter = mock(PropertyIterator.class);
        lenient().when(node.setProperty("test", "testing")).thenReturn(pt);
        lenient().when(node.getProperties()).thenReturn(pIter);
        NodeIterator nIter = mock(NodeIterator.class);
        lenient().when(node.getNodes()).thenReturn(nIter);

    	cpwProcessStep.replicatePage(session, context.currentPage().getPath(), resolver);
        assertNotNull(session);
        assertNotNull(replicator);
    }

    /**
     * Test replicate referenced assets.
     *
     * @throws Exception the exception
     */
    @Test
    void testReplicateReferencedAssets() throws Exception {
    	Resource resource = mock(Resource.class);
        lenient().when(resolver.getResource(anyString())).thenReturn(resource);
    	Node node = mock(Node.class);
        lenient().when(resource.adaptTo(Node.class)).thenReturn(node);
        AssetReferenceSearch ref = new AssetReferenceSearch(node,DamConstants.MOUNTPOINT_ASSETS,resolver);
        Property pt = mock(Property.class);
        PropertyIterator pIter = mock(PropertyIterator.class);
        lenient().when(node.setProperty("test", "testing")).thenReturn(pt);
        lenient().when(node.getProperties()).thenReturn(pIter);
        NodeIterator nIter = mock(NodeIterator.class);
        lenient().when(node.getNodes()).thenReturn(nIter);

    	cpwProcessStep.replicateReferencedAssets(session, context.currentPage().getPath(), resolver);
    	lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);

        assertNotNull(resolver);
        assertNotNull(replicator);
    }

    /**
     * Test replicate book nodes.
     *
     * @throws Exception the exception
     */
    @Test
    void testreplicateBookNodes() throws Exception {
        List<String> pathList = new ArrayList<>();
        pathList.add("/content/book-1/jcr:content/root/container/container/book");
        lenient().when(queryService.getBookNodesByPath("/content/process-publish-content", null)).thenReturn(pathList);
        lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
        Resource resource = mock(Resource.class);
        lenient().when(resolver.getResource(anyString())).thenReturn(resource);
        Node node = mock(Node.class);
        lenient().when(resource.adaptTo(Node.class)).thenReturn(node);
        Node parentNode = mock(Node.class);
        lenient().when(node.getParent()).thenReturn(parentNode);
        lenient().when(parentNode.getPath()).thenReturn("/content/sample/node/path");
        cpwProcessStep.replicateBookNodes(context.currentPage().getPath(), session, resolver);
        assertNotNull(replicator);
    }

    /**
     * Test replicate book nodes.
     *
     * @throws Exception the exception
     */
    @Test
    void testreplicateBookNodes2() throws Exception {
        lenient().when(queryService.getBookNodesByPath("/content/process-publish-content", null)).thenReturn(null);
        lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(null);
        Resource resource = mock(Resource.class);
        lenient().when(resolver.getResource(anyString())).thenReturn(null);
        Node node = mock(Node.class);
        lenient().when(resource.adaptTo(Node.class)).thenReturn(null);
        Node parentNode = mock(Node.class);
        lenient().when(node.getParent()).thenReturn(null);
        lenient().when(parentNode.getPath()).thenReturn(null);
        cpwProcessStep.replicateBookNodes(context.currentPage().getPath(), session, resolver);
        assertNotNull(replicator);
    }

    /**
     * Test update page properties.
     *
     * @throws Exception the exception
     */
    @Test
    void testUpdatePagePropertiesWithEventTemplate() throws Exception {
    	PageManager pm = mock(PageManager.class);
    	lenient().when(resolver.adaptTo(PageManager.class)).thenReturn(pm);
    	Page page = mock(Page.class);
    	lenient().when(pm.getPage(context.currentPage().getPath())).thenReturn(page);
    	Template tp = mock(Template.class);
    	lenient().when(page.getTemplate()).thenReturn(tp);
    	Node node = session.getNode("/content/process-publish-content/jcr:content");
        assertNotNull(node);
        lenient().when(tp.getPath()).thenReturn(WorkflowConstants.EVENT_TEMPLATE_PATH);
        Boolean actualResultRevRemDt = node.hasProperty(WorkflowConstants.REVIEW_REMINDER_DATE);
        assertTrue(actualResultRevRemDt);
        Boolean actualResultRetNtDt = node.hasProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE);
        assertTrue(actualResultRetNtDt);
        Boolean actualResultSdRtDt = node.hasProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE);
        assertTrue(actualResultSdRtDt);

    	cpwProcessStep.updatePageProperties(context.currentPage().getPath(), session, resolver);
    	assertNotNull(page);
    }

    /**
     * Test update page properties.
     *
     * @throws Exception the exception
     */
    @Test
    void testUpdatePagePropertiesWithoutEventTemplate() throws Exception {
    	PageManager pm = mock(PageManager.class);
    	lenient().when(resolver.adaptTo(PageManager.class)).thenReturn(pm);
    	Page page = mock(Page.class);
    	lenient().when(pm.getPage(context.currentPage().getPath())).thenReturn(page);
    	Template tp = mock(Template.class);
    	lenient().when(page.getTemplate()).thenReturn(tp);
    	Node node = session.getNode("/content/process-publish-content/jcr:content");
        assertNotNull(node);
        lenient().when(tp.getPath()).thenReturn("");
        Boolean actualResultRevRemDt = node.hasProperty(WorkflowConstants.REVIEW_REMINDER_DATE);
        assertTrue(actualResultRevRemDt);
        Property propertyRevRemDt = node.getProperty(WorkflowConstants.REVIEW_REMINDER_DATE);
        assertNotNull(propertyRevRemDt);
        Boolean actualResultRetNtDt = node.hasProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE);
        assertTrue(actualResultRetNtDt);
        Property propertyRetNtDt = node.getProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE);
        assertNotNull(propertyRetNtDt);
        Boolean actualResultSdRtDt = node.hasProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE);
        assertTrue(actualResultSdRtDt);
        Property propertySdRtDt = node.getProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE);
        assertNotNull(propertySdRtDt);

    	cpwProcessStep.updatePageProperties(context.currentPage().getPath(), session, resolver);
    	assertNotNull(page);
    }
}
