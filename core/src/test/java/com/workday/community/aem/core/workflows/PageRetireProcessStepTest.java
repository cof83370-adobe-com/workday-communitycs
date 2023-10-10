package com.workday.community.aem.core.workflows;

import static com.workday.community.aem.core.constants.WorkflowConstants.IMMEDIATE_RETIREMENT;
import static com.workday.community.aem.core.constants.WorkflowConstants.LAST_RETIREMENT_ACTION;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_PROP;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_VAL;
import static com.workday.community.aem.core.constants.WorkflowConstants.SCHEDULED_RETIREMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.Workflow;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.metadata.SimpleMetaDataMap;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
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

/**
 * The Class PageRetireProcessStepTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class PageRetireProcessStepTest {

  /**
   * The context.
   */
  private final AemContext context = new AemContext(ResourceResolverType.JCR_MOCK);

  /**
   * The session.
   */
  private final Session session = context.resourceResolver().adaptTo(Session.class);

  /**
   * The meta data.
   */
  private final MetaDataMap metaData = new SimpleMetaDataMap();

  /**
   * The retire process step.
   */
  @InjectMocks
  private PageRetireProcessStep retireProcessStep;

  /**
   * The cache manager.
   */
  @Mock
  private CacheManagerService cacheManager;

  /**
   * The replicator.
   */
  @Mock
  private Replicator replicator;

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
   * The query service.
   */
  @Mock
  private QueryService queryService;

  /**
   * The resolver.
   */
  @Mock
  private ResourceResolver resolver;

  /**
   * The work item.
   */
  @Mock
  private WorkItem workItem;

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
    context.registerService(Replicator.class, replicator);
    Page currentPage =
        context.currentResource("/content/page-no-retired-badge").adaptTo(Page.class);
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
    lenient().when(workflowData.getPayload()).thenReturn("/content/page-with-retired-badge");
    Node node = session.getNode("/content/page-with-retired-badge/jcr:content");
    assertNotNull(node);
    boolean actualResult = node.hasProperty(RETIREMENT_STATUS_PROP);
    String propVal = node.getProperty(RETIREMENT_STATUS_PROP).getString();
    assertTrue(actualResult);
    assertEquals(RETIREMENT_STATUS_VAL, propVal);
    retireProcessStep.execute(workItem, workflowSession, metaData);
    assertNotNull(session);
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
    lenient().when(queryService.getBookNodesByPath("/content/page-with-retired-badge", null))
        .thenReturn(pathList);
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
    Resource resource = mock(Resource.class);
    lenient().when(resolver.getResource(anyString())).thenReturn(resource);
    Node node = mock(Node.class);
    lenient().when(resource.adaptTo(Node.class)).thenReturn(node);
    Node parentNode = mock(Node.class);
    lenient().when(node.getParent()).thenReturn(parentNode);
    lenient().when(parentNode.getPath()).thenReturn("/content/sample/node/path");
    retireProcessStep.removeBookNodes("/content/page-with-retired-badge", session);
    verify(resolver).close();
  }

  /**
   * Test add retirement badge true.
   *
   * @throws Exception the exception
   */
  @Test
  void testAddRetirementBadgeFor30DaysWorkflowModel() throws Exception {
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
    Resource mockedResource = mock(Resource.class);
    lenient().when(resolver.getResource(anyString())).thenReturn(mockedResource);

    ModifiableValueMap modiMap = mock(ModifiableValueMap.class);
    lenient().when(mockedResource.adaptTo(ModifiableValueMap.class)).thenReturn(modiMap);

    Workflow workflowMock = mock(Workflow.class);
    lenient().when(workItem.getWorkflow()).thenReturn(workflowMock);

    WorkflowModel workflowModelMock = mock(WorkflowModel.class);
    lenient().when(workflowMock.getWorkflowModel()).thenReturn(workflowModelMock);

    lenient().when(workflowModelMock.getId())
        .thenReturn("/var/workflow/models/workday-community/retirement_workflow_30_days");

    retireProcessStep.addRetirementBadge("/content/page-no-retired-badge", workItem);
    verify(modiMap, times(1)).put(LAST_RETIREMENT_ACTION, SCHEDULED_RETIREMENT);
  }

  /**
   * Test add retirement badge for immediate workflow model.
   *
   * @throws Exception the exception
   */
  @Test
  void testAddRetirementBadgeForImmediateWorkflowModel() throws Exception {
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
    Resource mockedResource = mock(Resource.class);
    lenient().when(resolver.getResource(anyString())).thenReturn(mockedResource);

    ModifiableValueMap modiMap = mock(ModifiableValueMap.class);
    lenient().when(mockedResource.adaptTo(ModifiableValueMap.class)).thenReturn(modiMap);

    Workflow workflowMock = mock(Workflow.class);
    lenient().when(workItem.getWorkflow()).thenReturn(workflowMock);

    WorkflowModel workflowModelMock = mock(WorkflowModel.class);
    lenient().when(workflowMock.getWorkflowModel()).thenReturn(workflowModelMock);

    lenient().when(workflowModelMock.getId())
        .thenReturn("/var/workflow/models/workday-community/retirement_workflow_immediate");

    retireProcessStep.addRetirementBadge("/content/page-no-retired-badge", workItem);
    verify(modiMap, times(1)).put(LAST_RETIREMENT_ACTION, IMMEDIATE_RETIREMENT);
  }

  /**
   * Test replicate page.
   *
   * @throws Exception the exception
   */
  @Test
  void testReplicatePage() throws Exception {
    retireProcessStep.replicatePage(session, "/content/page-with-retired-badge");
    assertNotNull(session);
  }
}
