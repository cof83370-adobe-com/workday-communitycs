package com.workday.community.aem.core.schedulers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.JobBuilder.ScheduleBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.workday.community.aem.core.services.RetirementManagerJobConfigService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class RetirementManagerJobSchedulerTest.
 */
@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
public class RetirementManagerJobSchedulerTest {
    
    /** The job topic. */
	private static final String TOPIC = "content/retirement/manager/job";
    
    @Mock
    JobManager jobManager;
    
    @InjectMocks
    RetirementManagerJobScheduler revNotifScheduler;
    
    @Mock
    RetirementManagerJobConfigService retirementManagerJobConfigService;
    
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
		revNotifScheduler.activate();
    }
    
    @Test
    public final void testStopRetirementManagerJob() {
    	revNotifScheduler.stopRetirementManagerJob();
    }
    
    @Test
    public final void testStartRetirementManagerJob() {
    	lenient().when(jobManager.createJob(TOPIC)).thenReturn(jobBuilder);
    	assertNotNull(jobBuilder);
    	lenient().when(jobBuilder.schedule()).thenReturn(scheduleBuilder);
    	assertNotNull(scheduleBuilder);
    	revNotifScheduler.startRetirementManagerJob();
    }
}
