package com.workday.community.aem.core.listerners;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.listeners.ReplicationEventHandler;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import org.apache.sling.event.jobs.JobManager;
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
   * The ReplicationEventHandler.
   */
  @InjectMocks
  private ReplicationEventHandler eventHandler;

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
   * A mocked event object.
   */
  @Mock
  private Event event;

  /**
   * Test handler events passed.
   */
  @ParameterizedTest
  @EnumSource(value = ReplicationActionType.class, names = {"ACTIVATE", "DEACTIVATE", "DELETE"})
  void testHandleEventsPass(ReplicationActionType actionType) {
    ReplicationAction action = new ReplicationAction(actionType,
        GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH);
    when(service.isCoveoIndexEnabled()).thenReturn(true);
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
    try (MockedStatic<ReplicationAction> mock = mockStatic(ReplicationAction.class)) {
      when(ReplicationAction.fromEvent(event)).thenReturn(action);
      eventHandler.handleEvent(event);
    }
    verify(jobManager, times(0)).addJob(anyString(), anyMap());
  }
}
