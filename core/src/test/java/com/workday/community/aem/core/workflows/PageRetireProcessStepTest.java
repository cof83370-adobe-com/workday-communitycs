package com.workday.community.aem.core.workflows;

import static com.workday.community.aem.core.constants.GlobalConstants.RETIREMENT_STATUS_PROP;
import static com.workday.community.aem.core.constants.GlobalConstants.RETIREMENT_STATUS_VAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.metadata.SimpleMetaDataMap;
import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class PageRetireProcessStepTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class PageRetireProcessStepTest {

    /** The context. */
    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    /** The session. */
    private final Session session = context.resourceResolver().adaptTo(Session.class);

    @InjectMocks
    PageRetireProcessStep retireProcessStep;

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

    /**
     * The resolver.
     */
    @Mock
    private ResourceResolver resolver;

    /** The work item. */
    @Mock
    private WorkItem workItem;

    /** The my workflow. */
    private PageRetireProcessStep pageRetireProcessStep = new PageRetireProcessStep();

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
                "/com/workday/community/aem/core/models/impl/PageRetireProcessStepTestData.json",
                "/content");
        context.registerService(ResourceResolver.class, resolver);
        context.registerService(QueryService.class, queryService);
        Page currentPage = context.currentResource("/content/page-no-retired-badge").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        lenient().when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resolver);
    }

    /**
     * My workflow successful session save.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void myWorkflowSuccessfulSessionSave() throws RepositoryException {
        assertNotNull(session);
        session.save();
        session.logout();
    }

    /**
     * My workflow with retired page tag true.
     *
     * @throws Exception the exception
     */
    @Test
    public void myWorkflowWithRetiredPageTagTrue() throws Exception {
        lenient().when(workflowData.getPayload()).thenReturn("/content/page-with-retired-badge");

        pageRetireProcessStep.execute(workItem, workflowSession, metaData);

        assertNotNull(session);
        Node node = session.getNode("/content/page-with-retired-badge/jcr:content");
        assertNotNull(node);

        Boolean actualResult = node.hasProperty(RETIREMENT_STATUS_PROP);
        String propVal = node.getProperty(RETIREMENT_STATUS_PROP).getString();
        assertTrue(actualResult);
        assertEquals(RETIREMENT_STATUS_VAL, propVal);
    }

    /**
     * Test remove book nodes.
     *
     * @throws Exception the exception
     */
    @Test
    void testRemoveBookNodes() throws Exception {
        List<String> pathList = new ArrayList<>();
        pathList.add("/content/book-1/jcr:content/root/container/container/book");
        lenient().when(queryService.getBookNodesByPath("/content/page-with-retired-badge", null)).thenReturn(pathList);
        lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
        pageRetireProcessStep.removeBookNodes(resolver, "/content/page-with-retired-badge");
        assertNotNull(resolver);
    }

    /**
     * Test add retirement badge true.
     *
     * @throws Exception the exception
     */
    @Test
    void testAddRetirementBadge() throws Exception {
        Resource resource = mock(Resource.class);
        lenient().when(resolver.getResource(anyString())).thenReturn(resource);
        ModifiableValueMap map = mock(ModifiableValueMap.class);
        lenient().when(resource.adaptTo(ModifiableValueMap.class)).thenReturn(map);
        retireProcessStep.addRetirementBadge(resolver, context.currentPage().getPath());
        verify(map, times(1)).put(RETIREMENT_STATUS_PROP, RETIREMENT_STATUS_VAL);
    }

    /**
     * Test replicate page.
     *
     * @throws Exception the exception
     */
    @Test
    void testReplicatePage() throws Exception {
        pageRetireProcessStep.replicatePage(resolver, workflowSession, context.currentPage().getPath());
        assertNotNull(resolver); 
    }
}
