package com.workday.community.aem.core.listeners;

import java.util.HashMap;
import java.util.Map;

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
    
    @Override
    public void handleEvent(Event event) {
        try {
            ReplicationAction action = getAction(event);
            //ReplicationAction action = ReplicationAction.fromEvent(event);
            if (action.getType().equals(ReplicationActionType.ACTIVATE) ||
                action.getType().equals(ReplicationActionType.DEACTIVATE) ||
                action.getType().equals(ReplicationActionType.DELETE)
            ) {
                Map<String, Object> jobProperties = new HashMap<>();
                String op = action.getType().equals(ReplicationActionType.ACTIVATE) ? "index" : "delete";
                jobProperties.put("op", op);
                jobProperties.put("path", action.getPath());
                
                // Add this job to the job manager.
                jobManager.addJob("workday-community/replication/job", jobProperties);
            }
        } catch (Exception e){
            logger.error("\n Error occured while Publishing/Unpublishing page - {} " , e.getMessage());
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
    
}
