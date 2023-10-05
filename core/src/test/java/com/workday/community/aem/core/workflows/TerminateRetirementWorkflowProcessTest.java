package com.workday.community.aem.core.workflows;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.metadata.SimpleMetaDataMap;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import javax.jcr.Session;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class TerminateRetirementWorkflowProcessTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class TerminateRetirementWorkflowProcessTest {

    /** The context. */
    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    /** The session. */
    private final Session session = context.resourceResolver().adaptTo(Session.class);

    @InjectMocks
    TerminateRetirementWorkflowProcess terminateRetWfProcess;

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

    /** The workflow array. */
    private WorkItem[] wiArray;

    /** The workflow session. */
    @Mock
    private WorkflowModel workflowModel;

    /** The meta data. */
    private final MetaDataMap metaData = new SimpleMetaDataMap();

    /**
     * Setup.
     *
     * @throws LoginException the login exception
     */
    @BeforeEach
    void setup() throws LoginException {
        lenient().when(workflow.getMetaDataMap()).thenReturn(metaData);
        lenient().when(workflowData.getPayloadType()).thenReturn("JCR_PATH");
        lenient().when(workItem.getWorkflow()).thenReturn(workflow);
        lenient().when(workflow.getWorkflowData()).thenReturn(workflowData);
        lenient().when(workflow.getWorkflowModel()).thenReturn(workflowModel);
        lenient().when(workflow.getState()).thenReturn("RUNNING");
        wiArray = new WorkItem[]{workItem};
        lenient().when(workItem.getWorkflowData()).thenReturn(workflowData);
        lenient().when(workflowData.getPayload()).thenReturn("/content/terminate-retirement-process");

        context.registerAdapter(WorkflowSession.class, Session.class, session);
        context.load().json(
                "/com/workday/community/aem/core/models/impl/TerminateRetirementWorkflowProcessTestData.json",
                "/content");
    }

    /**
     * Terminate Retirement Workflows.
     *
     * @throws Exception the exception
     */
    @Test
    public void testExecuteMethod() throws Exception {
        lenient().when(workflowData.getPayload()).thenReturn("/content/terminate-retirement-process");
        lenient().when(workflowSession.getActiveWorkItems()).thenReturn(wiArray);
        lenient().when(workflowModel.getId()).thenReturn("/var/workflow/models/workday-community/retirement_workflow_30_days");
        terminateRetWfProcess.execute(workItem, workflowSession, metaData);

        verify(workflowSession,times(1)).terminateWorkflow(wiArray[0].getWorkflow());
    }
}
