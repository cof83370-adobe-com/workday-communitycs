package com.workday.community.aem.core.listeners;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import com.workday.community.aem.core.services.RunModeConfigService;

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

    /** The extract page properties service. */
    @Reference
    private ExtractPagePropertiesService extractPagePropertiesService;

    /** The run mode config service. */
    @Reference
    private RunModeConfigService runModeConfigService;
    
    @Override
    public JobResult process(Job job) {
        ArrayList<String> paths = (ArrayList<String>) job.getProperty("paths");
        String op = (String) job.getProperty("op");
        if (paths != null) {
            if (op.equals("delete")) {
                return startCoveoDelete(paths);
            }

            if(op.equals("index")) {
                return startCoveoIndex(paths);
            }
        }

        logger.error("Error occur in Coveo index job consumer, job does not have required properties: path and op.");
        return JobResult.FAILED;
    }

    /**
	 * Start coveo deleteing.
     * 
     * @param paths Page paths
	 * @return Job result
	 */
    public JobResult startCoveoDelete(ArrayList<String> paths) {  
        boolean hasError = false;
        for (String path : paths) {
            String documentId = runModeConfigService.getPublishInstanceDomain().concat(path).concat(".html");
            Integer status = coveoPushApiService.callDeleteSingleItemUri(documentId); 
            if (status != HttpStatus.SC_ACCEPTED) {
                hasError = true;
                logger.error("Error occurred in coveo job consumer when deleting path: {}", path);
            }
        }
        return hasError ? JobResult.FAILED : JobResult.OK;
    }

    /**
	 * Start coveo indexing.
     * 
     * @param paths Page paths
	 * @return Job result
	 */
    public JobResult startCoveoIndex(ArrayList<String> paths) {  
        List<Object> payload = new ArrayList<>();
        for (String path : paths) {
            payload.add(extractPagePropertiesService.extractPageProperties(path));
        }
        Integer status = coveoPushApiService.indexItems(payload);
        if (status != HttpStatus.SC_ACCEPTED) {
            logger.error("Error occurred in coveo job consumer when indexing paths: {}", paths.toArray());
            return JobResult.FAILED;
        }
        return JobResult.OK;
    }
    
}
