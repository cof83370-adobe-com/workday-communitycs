package com.workday.community.aem.core.listerners;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.listeners.ReplicationEventHandler;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.DrupalService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.event.Event;

/**
 * The Class ReplicationEventHandlerTest.
 */
@ExtendWith(MockitoExtension.class)
public class ReplicationEventHandlerTest {

  /**
   * The path to the Community content page.
   */
  static final String COMMUNITY_EVENT_PAGE_PATH = "/content/workday-community/en-us/event1/event2";

  /**
   * The ReplicationEventHandler.
   */
  @InjectMocks
  private ReplicationEventHandler eventHandler;

  /**
   * AemContext
   */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  /**
   * The JobManager.
   */
  @Mock
  private JobManager jobManager;

  /**
   * The CoveoIndexApiConfigService.
   */
  @Mock
  private CoveoIndexApiConfigService service;

  /**
   * The DrupalService.
   */
  @Mock
  private DrupalService drupalService;

  /**
   * A mocked event object.
   */
  @Mock
  private Event event;

  @Mock
  CacheManagerService cacheManager;

  @Mock
  ResourceResolver resourceResolver;

  @Mock
  private Resource resourceMock;

  @Mock
  private PageManager pageManager;

  @Mock
  private Page eventsPage;

  @Mock
  private Template eventsTemplate;

  @BeforeEach
  public void setUp() throws CacheException {
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resourceResolver);
    lenient().when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);

  }
  /**
   * Test handler events passed.
   */
  @ParameterizedTest
  @EnumSource(value = ReplicationActionType.class, names = {"ACTIVATE", "DEACTIVATE", "DELETE"})
  void testHandleEventsPass(ReplicationActionType actionType) {
    ReplicationAction action = new ReplicationAction(actionType,
        GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH);
    when(service.isCoveoIndexEnabled()).thenReturn(true);
    when(drupalService.isContentSyncEnabled()).thenReturn(false);
    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager).addJob(anyString(), anyMap());
  }

  /**
   * Test handler events failed.
   */
  @Test
  void testHandleEventsFailed() {
    ReplicationAction action = new ReplicationAction(ReplicationActionType.ACTIVATE, "other");
    when(service.isCoveoIndexEnabled()).thenReturn(true);
    when(drupalService.isContentSyncEnabled()).thenReturn(false);
    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager, times(0)).addJob(anyString(), anyMap());
  }

  /**
   * Test coveo is not enabled.
   */
  @Test
  void testHandleEventsNotRun() {
    ReplicationAction action = mock(ReplicationAction.class);
    when(service.isCoveoIndexEnabled()).thenReturn(false);
    when(drupalService.isContentSyncEnabled()).thenReturn(false);
    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager, times(0)).addJob(anyString(), anyMap());
  }

  /**
   * Test handler events passed for events page when content is activated or deactivated.
   */
  @ParameterizedTest
  @EnumSource(value = ReplicationActionType.class, names = {"ACTIVATE", "DEACTIVATE"})
  void testHandleEventsPassedWhenContentIsActivateOrDeactivated(ReplicationActionType actionType) {
    ReplicationAction action = new ReplicationAction(actionType, COMMUNITY_EVENT_PAGE_PATH);
    when(service.isCoveoIndexEnabled()).thenReturn(false);
    when(drupalService.isContentSyncEnabled()).thenReturn(true);
    when(resourceResolver.getResource(anyString())).thenReturn(resourceMock);
    when(pageManager.getContainingPage(resourceMock)).thenReturn(eventsPage);
    when(eventsPage.getTemplate()).thenReturn(eventsTemplate);
    when(eventsTemplate.getPath()).thenReturn(GlobalConstants.EVENTS_TEMPLATE_PATH);

    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager).addJob(anyString(), anyMap());
  }

  /**
   * Test handler events passed for events page when content is deleted.
   */
  @ParameterizedTest
  @EnumSource(value = ReplicationActionType.class, names = {"DELETE"})
  void testHandleEventsPassedWhenContentIsDeleted(ReplicationActionType actionType) {
    ReplicationAction action = new ReplicationAction(actionType, COMMUNITY_EVENT_PAGE_PATH);
    when(service.isCoveoIndexEnabled()).thenReturn(false);
    when(drupalService.isContentSyncEnabled()).thenReturn(true);
    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager).addJob(anyString(), anyMap());
  }




  /**
   * Test handler events failed for events page when content sync is disabled.
   */
  @ParameterizedTest
  @EnumSource(value = ReplicationActionType.class, names = {"ACTIVATE"})
  void testHandleEventsFailedForWhenContentSyncDisabled(ReplicationActionType actionType) {
    ReplicationAction action = new ReplicationAction(actionType, COMMUNITY_EVENT_PAGE_PATH);
    when(service.isCoveoIndexEnabled()).thenReturn(false);
    when(drupalService.isContentSyncEnabled()).thenReturn(false);
    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager, times(0)).addJob(anyString(), anyMap());
  }

  /**
   * Test handler events failed when event is other than predefined list.
   */
  @Test
  void testHandleEventsFailedForOtherEvents() {
    ReplicationAction action = new ReplicationAction(ReplicationActionType.ACTIVATE, "other");
    when(service.isCoveoIndexEnabled()).thenReturn(false);
    when(drupalService.isContentSyncEnabled()).thenReturn(true);
    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager, times(0)).addJob(anyString(), anyMap());
  }

  @AfterEach
  public void after() {
    resourceResolver.close();
  }
}
