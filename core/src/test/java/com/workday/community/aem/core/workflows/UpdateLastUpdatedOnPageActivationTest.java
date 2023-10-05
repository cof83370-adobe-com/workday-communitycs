package com.workday.community.aem.core.workflows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.metadata.SimpleMetaDataMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class UpdateLastUpdatedOnPageActivationTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class UpdateLastUpdatedOnPageActivationTest {

    /** The context. */
    private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

    /** The session. */
    private final Session session = context.resourceResolver().adaptTo(Session.class);

    /** The workflow session. */
    @Mock
    private WorkflowSession workflowSession;

    /** The workflow. */
    @Mock
    private Workflow workflow;

    /** The workflow data. */
    @Mock
    private WorkflowData workflowData;

    /**
     * The resolver.
     */
    @Mock
    private ResourceResolver resolver;

    /** The work item. */
    @Mock
    private WorkItem workItem;

    /** The my workflow. */
    private UpdateLastUpdatedOnPageActivation myWorkflow = new UpdateLastUpdatedOnPageActivation();

    /** The meta data. */
    private final MetaDataMap metaData = new SimpleMetaDataMap();

    /** The arg. */
    private final String ARG = "test";

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
                "/com/workday/community/aem/core/models/impl/UpdateLastUpdatedOnPageActivationTestData.json",
                "/content");
        context.registerService(ResourceResolver.class, resolver);
        lenient().when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resolver);
    }

    /**
     * My workflow with args.
     *
     * @throws Exception the exception
     */
    @Test
    public void myWorkflowWithArgs() throws Exception {
        metaData.put("PROCESS_ARGS", ARG);
        lenient().when(workflowData.getPayload()).thenReturn("/content/page-override-no-flag");
        myWorkflow.execute(workItem, workflowSession, metaData);

        String arg = metaData.get("PROCESS_ARGS", String.class);
        assertNotNull(arg);
        assertEquals(ARG, arg);

        assertNotNull(session);
        Node node = session.getNode("/content/page-override-no-flag/jcr:content");
        assertNotNull(node);

        Property property = node.getProperty("updatedDate");
        assertNotNull(property);
    }

    /**
     * My workflow with override date.
     *
     * @throws Exception the exception
     */
    @Test
    public void myWorkflowWithOverrideDate() throws Exception {
        lenient().when(workflowData.getPayload()).thenReturn("/content/page-override-flag");
        myWorkflow.execute(workItem, workflowSession, metaData);

        assertNotNull(session);
        Node node = session.getNode("/content/page-override-flag/jcr:content");
        assertNotNull(node);

        Boolean actualResult = node.hasProperty("overrideDate");
        assertFalse(actualResult);
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

}
