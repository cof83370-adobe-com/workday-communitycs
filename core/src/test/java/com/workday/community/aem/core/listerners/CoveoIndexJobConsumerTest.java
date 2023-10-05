package com.workday.community.aem.core.listerners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.workday.community.aem.core.listeners.CoveoIndexJobConsumer;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import com.workday.community.aem.core.services.RunModeConfigService;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.HttpStatus;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class CoveoIndexJobConsumerTest.
 */
@ExtendWith(MockitoExtension.class)
public class CoveoIndexJobConsumerTest {

    /** The CoveoIndexJobConsumer. */
    @InjectMocks
    private CoveoIndexJobConsumer consumer;

    /** The CoveoPushApiService. */
    @Mock
    private CoveoPushApiService coveoPushService;

    /** The RunModeConfigService. */
    @Mock
    private RunModeConfigService runModeConfigService;

    /** The ExtractPagePropertiesService. */
    @Mock
    private ExtractPagePropertiesService extractPagePropertiesService;

    /**
     * Test start coveo delete successed.
     */
    @Test
    void testStartCoveoDeleteSuccess() {
        ArrayList<String> paths = new ArrayList<String>();
        String path = "/sample/path";
        paths.add(path);
        String documentId = "https://www.test.link";
        doReturn(documentId).when(runModeConfigService).getPublishInstanceDomain();
        doReturn(HttpStatus.SC_ACCEPTED).when(coveoPushService).callDeleteSingleItemUri(any());
        JobResult result = consumer.startCoveoDelete(paths);
        assertEquals(JobResult.OK, result);
    }

    /**
     * Test start coveo delete failed.
     */
    @Test
    void testStartCoveoDeleteFail() {
        ArrayList<String> paths = new ArrayList<String>();
        String path = "/sample/path";
        paths.add(path);
        String documentId = "https://www.test.link";
        doReturn(documentId).when(runModeConfigService).getPublishInstanceDomain();
        doReturn(HttpStatus.SC_REQUEST_TOO_LONG).when(coveoPushService).callDeleteSingleItemUri(any());
        JobResult result = consumer.startCoveoDelete(paths);
        assertEquals(JobResult.FAILED, result);
    }


    /**
     * Test start coveo index successed.
     */
    @Test
    void testStartCoveoIndexSuccess() {
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/sample/path");
        ArrayList<Object> payload = new ArrayList<Object>();
        HashMap<String, Object> property = new HashMap<String, Object>();
        property.put("pageTitle", "Sample page");
        payload.add(property);
        doReturn(property).when(extractPagePropertiesService).extractPageProperties(any());
        doReturn(HttpStatus.SC_ACCEPTED).when(coveoPushService).indexItems(payload);
        JobResult result = consumer.startCoveoIndex(paths);
        assertEquals(JobResult.OK, result);
    }

    /**
     * Test start coveo index failed.
     */
    @Test
    void testStartCoveoIndexFail() {
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/sample/path");
        ArrayList<Object> payload = new ArrayList<Object>();
        HashMap<String, Object> property = new HashMap<String, Object>();
        property.put("pageTitle", "Sample page");
        payload.add(property);
        doReturn(property).when(extractPagePropertiesService).extractPageProperties(any());
        doReturn(HttpStatus.SC_REQUEST_TOO_LONG).when(coveoPushService).indexItems(payload);
        JobResult result = consumer.startCoveoIndex(paths);
        assertEquals(JobResult.FAILED, result);
    }

    /**
     * Test process job failed.
     */
    @Test
    void testProcessJobFail() throws Exception {
        Job job = mock(Job.class);
        doReturn(null).when(job).getProperty("op");
        doReturn(null).when(job).getProperty("paths");
        JobResult result = consumer.process(job);
        assertEquals(JobResult.FAILED, result);
    }
}
