package com.workday.community.aem.core.listerners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.workday.community.aem.core.listeners.CoveoIndexJobConsumer;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class CoveoIndexJobConsumerTest.
 */
@ExtendWith(AemContextExtension.class)
public class CoveoIndexJobConsumerTest {
    
    /**
     * Test process job passed.
     *
     * @throws Exception the exception
     */
    @Test
    void testProcessJobPass() throws Exception {
        Job job = mock(Job.class);
        ArrayList<String> paths = new ArrayList<>();
        paths.add("sample/path");
        when(job.getProperty("paths")).thenReturn(paths);
        CoveoIndexJobConsumer consumer = new CoveoIndexJobConsumer();
        JobResult result = consumer.process(job);
        assertEquals(JobResult.OK, result);
    }

    /**
     * Test process job failed.
     *
     * @throws Exception the exception
     */
    @Test
    void testProcessJobFail() throws Exception {
        Job job = mock(Job.class);
        when(job.getProperty("value")).thenReturn("some value");
        CoveoIndexJobConsumer consumer = new CoveoIndexJobConsumer();
        JobResult result = consumer.process(job);
        assertEquals(JobResult.OK, result);
    }
}
