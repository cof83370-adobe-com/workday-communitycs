package com.workday.community.aem.core.listeners;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.constants.GlobalConstants;


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
    
    @Override
    public JobResult process(Job job) {
        try {
            String paths = (String) job.getProperty("paths");
            // @todo Once we have the coveo mapping and service, we can extract page properties 
            // and pass those info to coveo. 
            return JobResult.OK;
        } 
        catch (Exception e) {
            logger.error("\n Error occured in coveo index job consumer : {}  ", e.getMessage());
            return JobResult.FAILED;
        }
    }
    
}
