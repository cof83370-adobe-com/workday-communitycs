package com.workday.community.aem.core.jobs;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.EmailService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.WorkflowConfigService;
import com.workday.community.aem.core.utils.WorkflowUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class RetirementManagerJobConsumer.
 */
@Slf4j
@Component(service = JobConsumer.class, immediate = true, 
    property = { JobConsumer.PROPERTY_TOPICS + "=" + "community/retirement/manager/job" })
public class RetirementManagerJobConsumer implements JobConsumer {

  /** The cache manager. */
  @Reference
  private CacheManagerService cacheManager;

  /** The query service. */
  @Reference
  private QueryService queryService;

  /** The run mode config service. */
  @Reference
  private RunModeConfigService runModeConfigService;

  /** The email service of workday. */
  @Reference
  private EmailService emailService;

  /** The resolver factory. */
  @Reference
  private ResourceResolverFactory resolverFactory;
  
  /** The workflow config service. */
  @Reference
  private WorkflowConfigService workflowConfigService;
  
  @Reference
  private Replicator replicator;

  /**
   * The email template review reminder body path.
   */
  private final String emailTemplateRevReminderBodyPath = 
        "/workflows/review-reminder/jcr:content/root/container/container/text";

  /**
   * The email template retirement notification body path.
   */
  private final String emailTemplateRetNotifyBodyPath = 
        "/workflows/retirement-notification/jcr:content/root/container/container/text";

  /**
   * The Property reviewReminderDate.
   */
  private final String propReviewReminderDate = "jcr:content/reviewReminderDate";

  /**
   * The Property retirementNotificationDate.
   */
  private final String propRetirementNotificationDate = "jcr:content/retirementNotificationDate";

  /**
   * The email template review reminder subject path.
   */
  private final String emailTemplateRevReminderSubjectPath = 
      "/workflows/review-reminder/jcr:content/root/container/container/title";

  /**
   * The email template retirement notification subject path.
   */
  private final String emailTemplateRetNotifySubjectPath = 
      "/workflows/retirement-notification/jcr:content/root/container/container/title";
  
  /**
   * The Property archivalDate.
   */
  private final String propArchivalDate = "jcr:content/archivalDate";

  @Override
  public JobResult process(Job job) {
    log.debug("RetirementManagerJobConsumer process >>>>>>>>>>>");
    // Check for any custom parameters in the job
    String timestamp = job.getProperty("jobTimestamp").toString();
    log.debug("Timestamp of Job: {}", timestamp);

    //logic for retirement scheduler
    return runJob();
  }
  
  /**
   * Runjob for RetirementManager notification.
   */
  public JobResult runJob() {
    log.debug("RetirementManagerJobConsumer runJob >>>>>>>>>>>");
    try (ResourceResolver resResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      if (workflowConfigService.enableWorkflowNotificationReview()) {
        sendReviewNotification(resResolver);
      }

      if (workflowConfigService.enableWorkflowNotificationRetirement()) {
        startRetirementAndNotify(resResolver);
      }
      
      archiveContent(resResolver);

    } catch (Exception e) {
      log.error("Exception in run >>>>>>> {}", e.getMessage());
      return JobResult.FAILED;
    }
    return JobResult.OK;
  }

  /**
   * Review RetirementManager notification.
   *
   * @param resResolver the resResolver
   */
  public void sendReviewNotification(ResourceResolver resResolver) {
    log.debug("RetirementManagerJobConsumer::Start querying Review Notification pages");
    List<String> reviewReminderPagePaths = queryService.getPagesDueTodayByDateProp(propReviewReminderDate);

    log.debug("RetirementManagerJobConsumer::End querying Review Notification pages.. Sending Notification");
    sendNotification(resResolver, reviewReminderPagePaths, emailTemplateRevReminderBodyPath,
        emailTemplateRevReminderSubjectPath, false);
  }

  /**
   * Trigger Workflow and Send RetirementManager notification.
   *
   * @param resResolver the resResolver
   */
  public void startRetirementAndNotify(ResourceResolver resResolver) {
    log.debug("RetirementManagerJobConsumer::Start querying Retirement and Notify pages");
    List<String> retirementNotificationPagePaths = queryService
        .getPagesDueTodayByDateProp(propRetirementNotificationDate);

    log.debug("RetirementManagerJobConsumer::End querying Retirement and Notify pages.. Sending Notification");
    sendNotification(resResolver, retirementNotificationPagePaths, emailTemplateRetNotifyBodyPath,
        emailTemplateRetNotifySubjectPath, true);

  }

  /**
   * It is use to check whether AEM is running in Author mode or not.
   *
   * @return Returns true is AEM is in author mode, false otherwise
   */
  public boolean isAuthorInstance() {
    return (runModeConfigService.getInstance() != null
        && runModeConfigService.getInstance().equals(GlobalConstants.PROP_AUTHOR));
  }

  /**
   * Send notification to author.
   *
   * @param resolver                 the resolver
   * @param paths                    the paths
   * @param emailTemplateBodyPath    the emailTemplateBodyPath
   * @param emailTemplateSubjectPath the emailTemplateSubjectPath
   * @param triggerRetirement        the triggerRetirement
   */
  public void sendNotification(ResourceResolver resolver, List<String> paths, String emailTemplateBodyPath,
      String emailTemplateSubjectPath, Boolean triggerRetirement) {
    log.debug("sendNotification >>>>>>>   ");

    paths.stream().filter(item -> resolver.getResource(item) != null).forEach(path -> {
      try {
        if (triggerRetirement) {
          startWorkflow(resolver, WorkflowConstants.RETIREMENT_WORKFLOW, path);
        }

        Node node = Objects.requireNonNull(resolver.getResource(path + GlobalConstants.JCR_CONTENT_PATH))
            .adaptTo(Node.class);

        if (node != null) {
          if (node.hasProperty(GlobalConstants.PROP_AUTHOR)) {
            // logic to send mail to author
            String author = node.getProperty(GlobalConstants.PROP_AUTHOR).getString();

            WorkflowUtils.sendNotification(author, resolver, emailTemplateBodyPath, emailTemplateSubjectPath, path,
                node, workflowConfigService, emailService);
          }
        }

      } catch (Exception e) {
        log.error("Exception occured in sendNotification: {}", e.getMessage());
      }
    });
  }

  /**
   * Trigger Workflow for a payload.
   *
   * @param resolver           the resolver
   * @param model              the model
   * @param payloadContentPath the payloadContentPath
   * @throws WorkflowException the workflow exception
   */
  public void startWorkflow(ResourceResolver resolver, String model, String payloadContentPath)
      throws WorkflowException {
    log.debug("startWorkflow for >>>>>>> {}  ", payloadContentPath);

    WorkflowSession workflowSession = resolver.adaptTo(WorkflowSession.class);

    // Create workflow model using model path
    WorkflowModel workflowModel = workflowSession.getModel(model);

    WorkflowData workflowData = workflowSession.newWorkflowData("JCR_PATH", payloadContentPath);

    // Pass value to workflow
    final Map<String, Object> workflowMetadata = new HashMap<>();
    workflowMetadata.put("pathInfo", payloadContentPath);

    // Trigger workflow
    workflowSession.startWorkflow(workflowModel, workflowData, workflowMetadata);
    log.debug("startWorkflow completed >>>>>>>   ");
  }
  
  /**
   * Archive Content.
   *
   * @param resolver the resolver
   */
  public void archiveContent(ResourceResolver resolver) {
    log.debug("in archiveContent >>>>>>>");

    if (replicator == null) {
      return;
    }

    List<String> retiredPagePaths = queryService.getRetiredPagesByArchivalDate(propArchivalDate);
    retiredPagePaths.stream().filter(item -> resolver.getResource(item) != null).forEach(path -> {
      Session session = resolver.adaptTo(Session.class);
      PageManager pageManager = resolver.adaptTo(PageManager.class);
      Page page = null;
      try {
        if (session != null && pageManager != null) {
          page = pageManager.getPage(path);
          if (page != null) {
            log.debug("Before archiving path >>>>>>> {}", path);

            // Deactivate the page before archiving
            replicator.replicate(session, ReplicationActionType.DEACTIVATE, path);

            // Create version for page before deleting
            pageManager.createRevision(page, null, WorkflowConstants.ARCHIVAL_VERSION_COMMENT);

            // Delete page
            pageManager.delete(page, false);

            log.debug("After archiving path >>>>>>> {}", path);
          }
        }
      } catch (ReplicationException e) {
        log.error("ReplicationException occured while archiving path: '{}' : {} ", path, e.getMessage());
      } catch (WCMException exec) {
        log.error("WCMException occured while archiving path: '{}' : {} ", path, exec.getMessage());
      }
    });
  }
}