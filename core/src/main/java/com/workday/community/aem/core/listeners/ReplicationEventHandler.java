package com.workday.community.aem.core.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;

/**
 * The Class ReplicationEventHandler.
 */
@Component(
    service = EventHandler.class,
    immediate = true,
    property = {
        EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC,
    })
public class ReplicationEventHandler implements EventHandler {
    
    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The jobManager service. */
    @Reference
    JobManager jobManager;

    /** The CoveoIndexApiConfigService. */
    @Reference 
    private CoveoIndexApiConfigService coveoIndexApiConfigService;

    /**
	 * Get coveo indexing is enabled or not.
	 *
	 * @return Coveo indexing is enabled or not.
	 */
    public boolean isCoveoEnabled() {
        return coveoIndexApiConfigService.isCoveoIndexEnabled();
    }
    
    @Override
    public void handleEvent(Event event) {
        if (isCoveoEnabled()) {
            ReplicationAction action = getAction(event);
            if (action.getPath().contains(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH) &&
                (action.getType().equals(ReplicationActionType.ACTIVATE) ||
                action.getType().equals(ReplicationActionType.DEACTIVATE) ||
                action.getType().equals(ReplicationActionType.DELETE))
            ) {
                if (startCoveoJob(action) == null) {
                    logger.error("\n Error occurred while Creating Coveo push job for page");
                }
            }
        }    
    }

    /**
	 * Get the ReplicationAction.
	 *
	 * @return The ReplicationAction.
	 */
    public ReplicationAction getAction(Event event) {
        return ReplicationAction.fromEvent(event);
    }

    /**
	 * Start coveo job for indexing or deleteing.
	 */
    public Job startCoveoJob(ReplicationAction action) {
        Map<String, Object> jobProperties = new HashMap<>();
        ArrayList<String> paths = new ArrayList<>();
        paths.add(action.getPath());
        String op = action.getType().equals(ReplicationActionType.ACTIVATE) ? "index" : "delete";
        jobProperties.put("op", op);
        jobProperties.put("paths", paths);
                
        // Add this job to the job manager.
        return jobManager.addJob(GlobalConstants.COMMUNITY_COVEO_JOB, jobProperties);
    }
    
}
