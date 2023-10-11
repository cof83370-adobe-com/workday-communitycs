package com.workday.community.aem.core.workflows;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.workday.community.aem.core.constants.GlobalConstants;
import java.util.Calendar;
import java.util.TimeZone;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class UpdateLastUpdatedOnPageActivation.
 */
@Component(service = WorkflowProcess.class, property = {
    "process.label = Update Last Updated Page Activation"})
public class UpdateLastUpdatedOnPageActivation implements WorkflowProcess {

  /**
   * The Constant TAG.
   */
  private static final String TAG = UpdateLastUpdatedOnPageActivation.class.getSimpleName();

  /**
   * The Constant log.
   */
  private static final Logger log =
      LoggerFactory.getLogger(UpdateLastUpdatedOnPageActivation.class);

  /**
   * The Constant OVERRIDE_DATE.
   */
  private static final String OVERRIDE_DATE = "overrideDate";

  /**
   * The Constant UPDATED_DATE.
   */
  private static final String UPDATED_DATE = "updatedDate";

  /**
   * {@inheritDoc}
   *
   * @param workItem        the work item
   * @param workflowSession the workflow session
   * @param metaDataMap     the meta data map
   */
  @Override
  public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) {
    String payloadType = workItem.getWorkflowData().getPayloadType();
    log.info("{}: Payload type: {}", TAG, payloadType);
    if (StringUtils.equals(payloadType, "JCR_PATH")) {
      // Get the JCR path from the payload
      String path = workItem.getWorkflowData().getPayload().toString();
      try {
        Session jcrSession = workflowSession.adaptTo(Session.class);
        Node node = (Node) jcrSession.getItem(path + GlobalConstants.JCR_CONTENT_PATH);
        if (node != null) {
          if (node.hasProperty(OVERRIDE_DATE)) {
            node.getProperty(OVERRIDE_DATE).remove();
          } else {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            node.setProperty(UPDATED_DATE, calendar);
          }
          jcrSession.save();
        }
      } catch (RepositoryException e) {
        log.error("{}: payload type - {} is not valid", TAG, payloadType);
      }
    }
  }
}
