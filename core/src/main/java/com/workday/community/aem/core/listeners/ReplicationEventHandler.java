package com.workday.community.aem.core.listeners;

import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_PUBLIC_PAGE_PATH;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * The Class ReplicationEventHandler.
 */
@Slf4j
@Component(
    service = EventHandler.class,
    immediate = true,
    property = {
        EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC,
    })
public class ReplicationEventHandler implements EventHandler {

  /**
   * The CoveoIndexApiConfigService.
   */
  @Reference
  private CoveoIndexApiConfigService coveoIndexApiConfigService;

  /**
   * The jobManager service.
   */
  @Reference
  private JobManager jobManager;

  /**
   * Get coveo indexing is enabled or not.
   *
   * @return Coveo indexing is enabled or not.
   */
  public boolean isCoveoEnabled() {
    return coveoIndexApiConfigService.isCoveoIndexEnabled();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleEvent(Event event) {
    if (isCoveoEnabled()) {
      ReplicationAction action = getAction(event);
      if ((action.getPath().contains(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH)
          || action.getPath().contains(WORKDAY_PUBLIC_PAGE_PATH))
          && (action.getType().equals(ReplicationActionType.ACTIVATE)
          || action.getType().equals(ReplicationActionType.DEACTIVATE)
          || action.getType().equals(ReplicationActionType.DELETE))
      ) {
        if (startCoveoJob(action) == null) {
          log.error("\n Error occurred while Creating Coveo push job for page");
        }
      }
    }
  }

  /**
   * Get the ReplicationAction.
   *
   * @return The ReplicationAction.
   */
  private ReplicationAction getAction(Event event) {
    return ReplicationAction.fromEvent(event);
  }

  /**
   * Start coveo job for indexing or deleting.
   *
   * @param action The ReplicationAction object.
   * @return The new Job, or null if there was an error.
   */
  private Job startCoveoJob(ReplicationAction action) {
    Map<String, Object> jobProperties = new HashMap<>();
    List<String> paths = new ArrayList<>();
    paths.add(action.getPath());
    String op = action.getType().equals(ReplicationActionType.ACTIVATE) ? "index" : "delete";
    jobProperties.put("op", op);
    jobProperties.put("paths", paths);

    return jobManager.addJob(GlobalConstants.COMMUNITY_COVEO_JOB, jobProperties);
  }

}
