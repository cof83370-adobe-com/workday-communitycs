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

    /** The ReplicationEventHandler. */
    @InjectMocks
    ReplicationEventHandler eventHandler;

    /** The JobManager. */
    @Mock
    JobManager jobManager;

    /** The CoveoIndexApiConfigService. */
    @Mock
    CoveoIndexApiConfigService service;

    /**
     * Test handler events passed.
     */
    @Test
    void testHandleEventsPass() {
        Event event = mock(Event.class);
        ReplicationAction action = mock(ReplicationAction.class);
        when(service.isCoveoIndexEnabled()).thenReturn(true);
        try (MockedStatic<ReplicationAction>  mock = mockStatic(ReplicationAction.class)) {
            when(ReplicationAction.fromEvent(event)).thenReturn(action);
            when(action.getType()).thenReturn(ReplicationActionType.DELETE);
            when(action.getPath()).thenReturn(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH);
            eventHandler.handleEvent(event);
        }
        verify(jobManager).addJob(anyString(), anyMap());
    }

    /**
     * Test handler events failed.
     */
    @Test
    void testHandleEventsFailed() {
        Event event = mock(Event.class);
        ReplicationAction action = mock(ReplicationAction.class);
        when(service.isCoveoIndexEnabled()).thenReturn(true);
        try (MockedStatic<ReplicationAction>  mock = mockStatic(ReplicationAction.class)) {
            when(ReplicationAction.fromEvent(event)).thenReturn(action);
            when(action.getPath()).thenReturn("other");
            eventHandler.handleEvent(event);
        }
        verify(jobManager, times(0)).addJob(anyString(), anyMap());
    }

    /**
     * Test coveo is not enabled.
     */
    @Test
    void testHandleEventsNotRun() {
        Event event = mock(Event.class);
        ReplicationAction action = mock(ReplicationAction.class);
        when(service.isCoveoIndexEnabled()).thenReturn(false);
        try (MockedStatic<ReplicationAction>  mock = mockStatic(ReplicationAction.class)) {
            when(ReplicationAction.fromEvent(event)).thenReturn(action);
            eventHandler.handleEvent(event);
          }
        verify(jobManager, times(0)).addJob(anyString(), anyMap());
    }
}
