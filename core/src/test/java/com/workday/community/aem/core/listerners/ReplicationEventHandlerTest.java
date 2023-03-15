package com.workday.community.aem.core.listerner;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.Mockito;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.event.Event;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.workday.community.aem.core.listeners.ReplicationEventHandler;

@ExtendWith(MockitoExtension.class)
public class ReplicationEventHandlerTest {

    @Test
    void testHandleEvents() throws Exception {
        Event event = Mockito.mock(Event.class);
        ReplicationAction action = Mockito.mock(ReplicationAction.class);
        //lenient().when(ReplicationAction.fromEvent(event)).thenReturn(action);
        lenient().when(action.getType()).thenReturn(ReplicationActionType.ACTIVATE);
        JobManager jobManager = Mockito.mock(JobManager.class);
        ReplicationEventHandler eventHandler = new ReplicationEventHandler();
        eventHandler.handleEvent(event);
        verify(jobManager, times(1)).addJob(anyString(), anyMap());
    }

    @Test
    void testHandleEventsFailed() throws Exception {
        ReplicationEventHandler eventHandler = new ReplicationEventHandler();
        Event event = mock(Event.class);
        ReplicationAction action = mock(ReplicationAction.class);
        lenient().when(action.getType()).thenReturn(ReplicationActionType.DELETE);
        JobManager jobManager = mock(JobManager.class);
        eventHandler.handleEvent(event);
        verify(jobManager, times(0)).addJob(eq("workday-community/replication/job"), anyMap());
    }
}
