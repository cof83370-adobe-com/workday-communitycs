package com.workday.community.aem.core.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.workday.community.aem.core.config.CoveoIndexApiConfig;
import com.workday.community.aem.core.constants.GlobalConstants;

/**
 * The Class ReplicationEventHandler.
 */
@Component(
    service = EventHandler.class,
    immediate = true,
    property = {
        EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC,
    })
@Designate(ocd = CoveoIndexApiConfig.class)
public class ReplicationEventHandler implements EventHandler {
    
    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The jobManager service. */
    @Reference
    JobManager jobManager;

    /** The coveoApiConfig. */
    private CoveoIndexApiConfig coveoIndexApiConfig;

    /**
	 * Activate the config.
	 */
    @Activate
    @Modified
    protected void activate(CoveoIndexApiConfig coveoIndexApiConfig) {
        this.coveoIndexApiConfig = coveoIndexApiConfig;
    }

    /**
	 * Get coveo indexing is enabled or not.
	 *
	 * @return Coveo indexing is enabled or not.
	 */
    public boolean isCoveoEnabled() {
        return coveoIndexApiConfig.isCoveoIndexingEnabled();
    }
    
    @Override
    public void handleEvent(Event event) {
        if (isCoveoEnabled()) {
            try {
                ReplicationAction action = getAction(event);
                if (action.getType().equals(ReplicationActionType.ACTIVATE) ||
                    action.getType().equals(ReplicationActionType.DEACTIVATE) ||
                    action.getType().equals(ReplicationActionType.DELETE)
                ) {
                    startCoveoJob(action);
                }
            } catch (Exception e) {
                logger.error("\n Error occured while Publishing/Unpublishing page - {} " , e.getMessage());
            }
        }    
    }

    /**
	 * Get the ReplicationAction.
	 *
	 * @return The ReplicationAction.
	 */
    public ReplicationAction getAction(Event event) {
        ReplicationAction action = ReplicationAction.fromEvent(event);
        return action;
    }

    /**
	 * Start coveo job for indexing or deleteing.
	 */
    public void startCoveoJob(ReplicationAction action) {
        Map<String, Object> jobProperties = new HashMap<>();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add(action.getPath());
        String op = action.getType().equals(ReplicationActionType.ACTIVATE) ? "index" : "delete";
        jobProperties.put("op", op);
        jobProperties.put("paths", paths);
                
        // Add this job to the job manager.
        jobManager.addJob(GlobalConstants.COMMUNITY_COVEO_JOB, jobProperties);
    }
    
}
