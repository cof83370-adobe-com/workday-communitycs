package com.workday.community.aem.core.listeners;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpStatus;
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
import com.workday.community.aem.core.services.ExtractPagePropertiesService;

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

    @Reference
    private ExtractPagePropertiesService extractPagePropertiesService;

    /** The externalizer service. */
    @Reference
    private Externalizer externalizer;

    @Reference
    private ResourceResolverFactory resolverFactory;
    
    @Override
    public JobResult process(Job job) {
        ArrayList<String> paths = (ArrayList<String>) job.getProperty("paths");
        String op = (String) job.getProperty("op");
        if (op != null && paths != null) {
            if (op.equals("delete")) {
                return startCoveoDelete(paths, resolverFactory, externalizer, coveoPushApiService);
            }
            else {
                return startCoveoIndex(paths, extractPagePropertiesService, coveoPushApiService);
            }
        }
        else {
            logger.error("Error occured in coveo index job consumer, job does not have required properties: path and op.");
            return JobResult.FAILED; 
        }
    }

    /**
	 * Start coveo deleteing.
     * 
     * @param paths Page paths
     * @param resolverFactory The resolverFactory service
     * @param externalizer Externalizer service
     * @param coveoPushApiService The coveoPushApiService
	 * @return Job result
	 */
    public JobResult startCoveoDelete(ArrayList<String> paths, ResourceResolverFactory resolverFactory, Externalizer externalizer, CoveoPushApiService coveoPushApiService) {
        Boolean hasError = false;
        for (String path : paths) {
            String documentId = externalizer.publishLink(resolverFactory.getThreadResourceResolver(), path) + ".html";
            Integer status = coveoPushApiService.callDeleteSingleItemUri(documentId); 
            if (status != HttpStatus.SC_ACCEPTED) {
                hasError = true;
                logger.error("Error occured in coveo job consumer when deleteing path: {}", path);
            }
        }
        return hasError ? JobResult.FAILED : JobResult.OK;
    }

    /**
	 * Start coveo indexing.
     * 
     * @param paths Page paths
     * @param extractPagePropertiesService The extractPagePropertiesService
     * @param coveoPushApiService The coveoPushApiService
	 * @return Job result
	 */
    public JobResult startCoveoIndex(ArrayList<String> paths, ExtractPagePropertiesService extractPagePropertiesService, CoveoPushApiService coveoPushApiService) {
        ArrayList<Object> payload = new ArrayList<Object>();
        for (String path : paths) {
            payload.add(extractPagePropertiesService.extractPageProperties(path));
        }
        Integer status = coveoPushApiService.indexItems(payload);
        if (status != HttpStatus.SC_ACCEPTED) {
            logger.error("Error occured in coveo job consumer when indexing paths: {}", Arrays.toString(paths.toArray()));
            return JobResult.FAILED;
        }
        return JobResult.OK;
    }
    
}
