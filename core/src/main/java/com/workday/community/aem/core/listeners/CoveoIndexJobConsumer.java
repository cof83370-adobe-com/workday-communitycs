package com.workday.community.aem.core.listeners;

import java.util.ArrayList;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CoveoPushApiService;

/**
 * The Class CoveoIndexJobConsumer.
 */
@Component(
    service = JobConsumer.class,
    immediate = true,
    property = {
        JobConsumer.PROPERTY_TOPICS + "=" + GlobalConstants.COMMUNITY_COVEO_JOB
    }
)
public class CoveoIndexJobConsumer implements JobConsumer {

    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The query service. */
    @Reference 
    private CoveoPushApiService coveoPushApiService;

    /** The externalizer service. */
    @Reference
    private Externalizer externalizer;

    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Override
    public JobResult process(Job job) {
        try {
            ArrayList<String> paths = (ArrayList<String>) job.getProperty("paths");
            // @todo Once we have the fields mapping and coveo service, we can extract page 
            // properties and pass those info to coveo. 
            String op = (String) job.getProperty("op");
            startCoveoIndex(paths, op);
            return JobResult.OK;
        } 
        catch (Exception e) {
            logger.error("\n Error occured in coveo index job consumer : {}  ", e.getMessage());
            return JobResult.FAILED;
        }
    }

    /**
	 * Start coveo indexing or deleteing.
	 */
    public void startCoveoIndex(ArrayList<String> paths, String op) {
        for (String path : paths) {
            if (op == "delete") {
                String documentId = externalizer.publishLink(resolverFactory.getThreadResourceResolver(), path) + ".html";
                coveoPushApiService.callDeleteSingleItemUri(documentId);
            }
            else {

            }
        }
    }
    
}
