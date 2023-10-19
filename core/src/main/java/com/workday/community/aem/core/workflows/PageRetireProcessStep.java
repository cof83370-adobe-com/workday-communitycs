package com.workday.community.aem.core.workflows;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.GlobalConstants.ISO_8601_FORMAT;
import static com.workday.community.aem.core.constants.WorkflowConstants.ACTUAL_RETIREMENT_DATE;
import static com.workday.community.aem.core.constants.WorkflowConstants.IMMEDIATE_RETIREMENT;
import static com.workday.community.aem.core.constants.WorkflowConstants.JCR_PATH;
import static com.workday.community.aem.core.constants.WorkflowConstants.LAST_RETIREMENT_ACTION;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_PROP;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_VAL;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_WORKFLOW_30_DAYS_MODEL_NAME;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_WORKFLOW_IMMEDIATE_MODEL_NAME;
import static com.workday.community.aem.core.constants.WorkflowConstants.SCHEDULED_RETIREMENT;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class PageRetireProcessStep.
 *
 * <p>This step inclueds three things.
 * 1. Remove the payload page from book, if already part of any book.
 * 2. Add retirement badge.
 * 3. Replicate the page to publisher.</p>
 */
@Slf4j
@Component(property = {
    Constants.SERVICE_DESCRIPTION + "=Process to retire the given page",
    Constants.SERVICE_VENDOR + "=Workday Community",
    "process.label" + "=Retire the page"
})
public class PageRetireProcessStep implements WorkflowProcess {

  /**
   * The cache manager.
   */
  @Reference
  private CacheManagerService cacheManager;

  /**
   * The query service.
   */
  @Reference
  private QueryService queryService;

  /**
   * The replicator.
   */
  @Reference
  private Replicator replicator;

  /**
   * {@inheritDoc}
   *
   * @param workItem        the work item
   * @param workflowSession the workflow session
   * @param metaDataMap     the meta data map
   * @throws WorkflowException the workflow exception
   */
  @Override
  public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap)
      throws WorkflowException {
    final String payloadType = workItem.getWorkflowData().getPayloadType();
    log.info("Payload type: {}", payloadType);
    if (StringUtils.equals(payloadType, JCR_PATH)) {
      // Get the JCR path from the payload
      String path = workItem.getWorkflowData().getPayload().toString();
      try {
        Session jcrSession = workflowSession.adaptTo(Session.class);
        if (null != jcrSession) {
          removeBookNodes(path, jcrSession);
          addRetirementBadge(path, workItem);
          replicatePage(jcrSession, path);
        }
      } catch (Exception e) {
        log.error("payload type - {} is not valid", payloadType);
      }
    }
  }

  /**
   * Removes the book nodes.
   *
   * @param pagePath   the page path
   * @param jcrSession the jcr session
   */
  public void removeBookNodes(String pagePath, Session jcrSession) {
    if (pagePath.contains(GlobalConstants.JCR_CONTENT_PATH)) {
      return;
    }

    try (ResourceResolver rresolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      List<String> paths = queryService.getBookNodesByPath(pagePath, null);
      paths.stream().filter(item -> rresolver.getResource(item) != null).forEach(path -> {
        try {
          Node root = Objects.requireNonNull(rresolver.getResource(path)).adaptTo(Node.class);
          final String pathToReplicate = root.getParent().getPath();
          root.remove();
          rresolver.commit();
          replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, pathToReplicate);
        } catch (Exception e) {
          log.error("Exception occured while removing the node: {}", path);
        }
      });
      log.debug("Removed node for page {}", pagePath);
    } catch (Exception exec) {
      log.error(
          "Exception occured while removing the: {} page from book node. Exception was: {} :",
          pagePath,
          exec.getMessage());
    }
  }

  /**
   * Adds the retirement badge.
   *
   * @param pagePath the page path
   * @param workItem the work item
   */
  public void addRetirementBadge(String pagePath, WorkItem workItem) {
    try (ResourceResolver rresolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      Resource resource = Objects
          .requireNonNull(rresolver.getResource(pagePath + GlobalConstants.JCR_CONTENT_PATH));
      String modelPath = workItem.getWorkflow().getWorkflowModel().getId();
      int lastIndex = modelPath.lastIndexOf('/');
      String workflowModelName = "";
      if (lastIndex != -1) {
        workflowModelName = modelPath.substring(lastIndex + 1);
      }
      if (StringUtils.isNotBlank(workflowModelName)) {
        addWorkflowModelSpecificProps(resource, workflowModelName);
      }
      rresolver.commit();
    } catch (Exception exec) {
      log.error("Exception occured while addRetirementBadge: {}", exec.getMessage());
    }
  }

  /**
   * Adds the workflow model specific props.
   *
   * @param resource  the resource
   * @param modelName the model name
   */
  private void addWorkflowModelSpecificProps(Resource resource, final String modelName) {
    // Add retirement badge.
    ModifiableValueMap map = resource.adaptTo(ModifiableValueMap.class);
    map.put(RETIREMENT_STATUS_PROP, RETIREMENT_STATUS_VAL);
    map.put(ACTUAL_RETIREMENT_DATE, Objects.requireNonNull(getCurrentTimeInIso8601Format()));
    switch (modelName) {
      case RETIREMENT_WORKFLOW_IMMEDIATE_MODEL_NAME:
        map.put(LAST_RETIREMENT_ACTION, IMMEDIATE_RETIREMENT);
        break;
      case RETIREMENT_WORKFLOW_30_DAYS_MODEL_NAME:
        map.put(LAST_RETIREMENT_ACTION, SCHEDULED_RETIREMENT);
        break;
      default:
        log.debug("Workflow model name is: {}  which was not as expected:", modelName);
        break;
    }
  }

  /**
   * Replicate page.
   *
   * @param jcrSession the jcr session
   * @param pagePath   the page path
   */
  public void replicatePage(Session jcrSession, String pagePath) {
    if (replicator == null) {
      return;
    }

    try {
      replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, pagePath);
    } catch (Exception exec) {
      log.error("Exception occured while replicatePage method: {}", exec.getMessage());
    }
  }

  /**
   * Gets the current time in iso 8601 format.
   *
   * @return the current time in iso 8601 format
   */
  private Calendar getCurrentTimeInIso8601Format() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_FORMAT);
    String iso8601String = sdf.format(calendar.getTime());

    try {
      calendar.setTime(sdf.parse(iso8601String));
      return calendar;
    } catch (ParseException exec) {
      log.error("Exception occured in getCurrentTimeInIso8601Format method: {}",
          exec.getMessage());
    }

    return null;
  }
}
