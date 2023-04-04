package com.workday.community.aem.core.listerners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer.JobResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.commons.Externalizer;
import com.workday.community.aem.core.listeners.CoveoIndexJobConsumer;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;

/**
 * The Class CoveoIndexJobConsumerTest.
 */
@ExtendWith(MockitoExtension.class)
public class CoveoIndexJobConsumerTest {

    /** The service ExtractPagePropertiesServiceImpl. */
    @Spy 
    private CoveoIndexJobConsumer consumer;

    /**
     * Test start coveo delete successed.
     */
    @Test
    void testStartCoveoDeleteSuccess() {
        ArrayList<String> paths = new ArrayList<String>();
        String path = "/sample/path";
        paths.add(path);
        CoveoPushApiService coveoPushService = Mockito.mock(CoveoPushApiService.class);
        Externalizer externalizer = Mockito.mock(Externalizer.class);
        ResourceResolverFactory resolverFactory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver resourceResolver = Mockito.mock(ResourceResolver.class);
        doReturn(resourceResolver).when(resolverFactory).getThreadResourceResolver();
        String documentId = "https://www.test.link.html";
        doReturn(documentId).when(externalizer).publishLink(resourceResolver, path);
        doReturn(HttpStatus.SC_ACCEPTED).when(coveoPushService).callDeleteSingleItemUri(any()); 
        JobResult result = consumer.startCoveoDelete(paths, resolverFactory, externalizer, coveoPushService);
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
        CoveoPushApiService coveoPushService = Mockito.mock(CoveoPushApiService.class);
        Externalizer externalizer = Mockito.mock(Externalizer.class);
        ResourceResolverFactory resolverFactory = Mockito.mock(ResourceResolverFactory.class);
        ResourceResolver resourceResolver = Mockito.mock(ResourceResolver.class);
        doReturn(resourceResolver).when(resolverFactory).getThreadResourceResolver();
        String documentId = "https://www.test.link.html";
        doReturn(documentId).when(externalizer).publishLink(resourceResolver, path);
        doReturn(HttpStatus.SC_REQUEST_TOO_LONG).when(coveoPushService).callDeleteSingleItemUri(any()); 
        JobResult result = consumer.startCoveoDelete(paths, resolverFactory, externalizer, coveoPushService);
        assertEquals(JobResult.FAILED, result);
    }


    /**
     * Test start coveo index successed.
     */
    @Test
    void testStartCoveoIndexSuccess() {
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/sample/path");
        CoveoPushApiService coveoPushService = Mockito.mock(CoveoPushApiService.class);
        ExtractPagePropertiesService extractPagePropertiesService = Mockito.mock(ExtractPagePropertiesService.class);
        ArrayList<Object> payload = new ArrayList<Object>();
        HashMap<String, Object> property = new HashMap<String, Object>();
        property.put("pageTitle", "Sample page");
        payload.add(property);
        doReturn(property).when(extractPagePropertiesService).extractPageProperties(any());
        doReturn(HttpStatus.SC_ACCEPTED).when(coveoPushService).indexItems(payload);
        JobResult result = consumer.startCoveoIndex(paths, extractPagePropertiesService, coveoPushService);
        assertEquals(JobResult.OK, result);
    }

    /**
     * Test start coveo index failed.
     */
    @Test
    void testStartCoveoIndexFail() {
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/sample/path");
        CoveoPushApiService coveoPushService = Mockito.mock(CoveoPushApiService.class);
        ExtractPagePropertiesService extractPagePropertiesService = Mockito.mock(ExtractPagePropertiesService.class);
        ArrayList<Object> payload = new ArrayList<Object>();
        HashMap<String, Object> property = new HashMap<String, Object>();
        property.put("pageTitle", "Sample page");
        payload.add(property);
        doReturn(property).when(extractPagePropertiesService).extractPageProperties(any());
        doReturn(HttpStatus.SC_REQUEST_TOO_LONG).when(coveoPushService).indexItems(payload);
        JobResult result = consumer.startCoveoIndex(paths, extractPagePropertiesService, coveoPushService);
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
