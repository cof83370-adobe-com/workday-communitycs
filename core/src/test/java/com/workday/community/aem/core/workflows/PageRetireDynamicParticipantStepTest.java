package com.workday.community.aem.core.workflows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.services.RunModeConfigService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class PageRetireDynamicParticipantStepTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class PageRetireDynamicParticipantStepTest {

  /**
   * The context.
   */
  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  /**
   * The process arg.
   */
  private final String PROCESS_ARG = "CMTY CC Admin {#ENV#}";

  /**
   * The workflow title.
   */
  private final String workflowTitle = "Retirement Workflow (30 Days)";

  /**
   * The default dynamic participants group.
   */
  private final String defaultDynamicParticipantsGroup = "administrators";

  /**
   * The mock dynamic participant step.
   */
  @InjectMocks
  PageRetireDynamicParticipantStep mockDynamicParticipantStep;

  /**
   * The workflow session.
   */
  @Mock
  private WorkflowSession workflowSession;

  /**
   * The workflow.
   */
  @Mock
  private Workflow workflow;

  /**
   * The workflow data.
   */
  @Mock
  private WorkflowData workflowData;

  /**
   * The run mode config service.
   */
  @Mock
  private RunModeConfigService runModeConfigService;

  /**
   * The work item.
   */
  @Mock
  private WorkItem workItem;

  /**
   * The meta data.
   */
  @Mock
  private MetaDataMap metaData;

  /**
   * Setup.
   *
   * @throws LoginException the login exception
   */
  @BeforeEach
  void setup() throws LoginException {
    lenient().when(workflow.getMetaDataMap()).thenReturn(metaData);
    lenient().when(workItem.getWorkflow()).thenReturn(workflow);
    lenient().when(workItem.getWorkflowData()).thenReturn(workflowData);
    context.load().json(
        "/com/workday/community/aem/core/models/impl/PageRetireProcessStepTestData.json",
        "/content");
    context.registerService(RunModeConfigService.class, runModeConfigService);
    Page currentPage =
        context.currentResource("/content/page-no-retired-badge").adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
  }

  /**
   * Test get participant method.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetParticipantMethod() throws Exception {
    lenient().when(metaData.get("PROCESS_ARGS", String.class)).thenReturn(PROCESS_ARG);
    WorkflowModel model = mock(WorkflowModel.class);
    lenient().when(workflow.getWorkflowModel()).thenReturn(model);
    lenient().when(model.getTitle()).thenReturn(workflowTitle);
    lenient().when(runModeConfigService.getEnv()).thenReturn("Dev");
    String dynamicParticipant =
        mockDynamicParticipantStep.getParticipant(workItem, workflowSession, metaData);
    assertEquals("CMTY CC Admin {Dev}", dynamicParticipant);
  }

  /**
   * Test get participant method with no env.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetParticipantMethodWithNoEnv() throws Exception {
    lenient().when(metaData.get("PROCESS_ARGS", String.class)).thenReturn(PROCESS_ARG);
    WorkflowModel model = mock(WorkflowModel.class);
    lenient().when(workflow.getWorkflowModel()).thenReturn(model);
    lenient().when(model.getTitle()).thenReturn(workflowTitle);
    lenient().when(runModeConfigService.getEnv()).thenReturn("");
    String dynamicParticipant =
        mockDynamicParticipantStep.getParticipant(workItem, workflowSession, metaData);
    assertEquals(dynamicParticipant, defaultDynamicParticipantsGroup);
  }

  /**
   * Test get participant method with no process args.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetParticipantMethodWithNoProcessArgs() throws Exception {
    lenient().when(metaData.get("PROCESS_ARGS", String.class)).thenReturn("");
    WorkflowModel model = mock(WorkflowModel.class);
    lenient().when(workflow.getWorkflowModel()).thenReturn(model);
    lenient().when(model.getTitle()).thenReturn(workflowTitle);
    lenient().when(runModeConfigService.getEnv()).thenReturn("");
    String dynamicParticipant =
        mockDynamicParticipantStep.getParticipant(workItem, workflowSession, metaData);
    assertEquals(dynamicParticipant, defaultDynamicParticipantsGroup);
  }
}
