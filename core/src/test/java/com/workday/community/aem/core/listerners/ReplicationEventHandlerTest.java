package com.workday.community.aem.core.listerners;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.apache.sling.event.jobs.JobManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.event.Event;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.workday.community.aem.core.listeners.ReplicationEventHandler;

/**
 * The Class ReplicationEventHandlerTest.
 */
@ExtendWith(MockitoExtension.class)
public class ReplicationEventHandlerTest {

    /** The ReplicationEventHandler. */
    @Spy
    ReplicationEventHandler eventHandler;

    /** The ReplicationAction. */
    @Mock 
    ReplicationAction action;

    /** The JobManager. */
    @Mock
    JobManager jobManager;

    /**
     * Test handler events.
     */
    @Test
    void testHandleEventsFailed() {
        Event event = Mockito.mock(Event.class);
        doReturn(action).when(eventHandler).getAction(event);
        doReturn(ReplicationActionType.DELETE).when(action).getType();
        doReturn(true).when(eventHandler).isCoveoEnabled();
        eventHandler.handleEvent(event);
        verify(jobManager, times(0)).addJob(anyString(), anyMap());
    }
}
