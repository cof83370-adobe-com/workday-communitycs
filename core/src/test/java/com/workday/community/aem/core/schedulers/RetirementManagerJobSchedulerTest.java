package com.workday.community.aem.core.schedulers;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.JobBuilder.ScheduleBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.workday.community.aem.core.services.WorkflowConfigService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class RetirementManagerJobSchedulerTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class RetirementManagerJobSchedulerTest {
    
    /** The job topic. */
	private static final String TOPIC = "community/retirement/manager/job";
    
    @Mock
    JobManager jobManager;
    
    @InjectMocks
    RetirementManagerJobScheduler revNotifScheduler;
    
    @Mock
    WorkflowConfigService workflowConfigService;
    
    @Mock
    JobBuilder jobBuilder;
    
    @Mock
    ScheduleBuilder scheduleBuilder;
    
    /**
     * Setup.
     *
     */
    @BeforeEach
    void setup() {
    	lenient().when(jobManager.createJob(TOPIC)).thenReturn(jobBuilder);
    	lenient().when(jobBuilder.schedule()).thenReturn(scheduleBuilder);
    }
    
    @Test
    public final void testActivate() {
    	revNotifScheduler.activate();
		verify(jobManager, times(1)).createJob(TOPIC);
		verify(jobBuilder, times(1)).schedule();
    }
}
