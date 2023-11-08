package com.workday.community.aem.core.schedulers;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.workday.community.aem.core.config.RetirementManagerSchedulerConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.EmailService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.RunModeConfigService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The Class RetirementManagerScheduler.
 */
@Slf4j
@Component(
    service = RetirementManagerScheduler.class,
    configurationPid = "com.workday.community.aem.core.config.RetirementManagerSchedulerConfig",
    immediate = true
)
@Designate(ocd = RetirementManagerSchedulerConfig.class)
public class RetirementManagerScheduler implements Runnable {

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

  /** The scheduler. */
  @Reference
  private Scheduler scheduler;

  /** The resolver factory. */
  @Reference
  private ResourceResolverFactory resolverFactory;

  private RetirementManagerSchedulerConfig retireConfig;

  private final String emailTemplateRevReminderText = 
        "/workflows/review-reminder/jcr:content/root/container/container/text";

  private final String emailTemplateRetNotifyText = 
        "/workflows/retirement-notification/jcr:content/root/container/container/text";

  private final String propReviewReminderDate = "jcr:content/reviewReminderDate";

  private final String propRetirementNotificationDate = "jcr:content/retirementNotificationDate";
  
  private final String emailTemplateRevReminderSubject = 
      "/workflows/review-reminder/jcr:content/root/container/container/title";

  private final String emailTemplateRetNotifySubject = 
      "/workflows/retirement-notification/jcr:content/root/container/container/title";

  /**
   * Activate/Modified RetirementManagerScheduler scheduler.
   *
   * @param config the config
   */
  @Activate
  @Modified
  protected void activate(final RetirementManagerSchedulerConfig config) {
    this.retireConfig = config;

    if (isAuthorInstance()) {
      log.debug(
          "RetirementManagerScheduler activate method called - "
              + "authorInstUrl: {}, cron: {}, review notification: {}, retirement notification: {}",
          config.authorDomain(), config.workflowNotificationCron(), config.enableWorkflowNotificationReview(),
          config.enableWorkflowNotificationRetirement());

      // un-schedule the existing scheduler for modified event
      if (scheduler != null) {
        scheduler.unschedule(this.getClass().getSimpleName());
      }

      // Execute this method to add scheduler.
      addScheduler(config);
    }
  }

  /**
   * Add all configurations to Schedule a scheduler depending on name and
   * expression.
   *
   * @param config the config
   */
  public void addScheduler(RetirementManagerSchedulerConfig config) {
    log.debug("Scheduler added successfully >>>>>>>   ");

    if (config.enableWorkflowNotificationReview() || config.enableWorkflowNotificationRetirement()) {
      ScheduleOptions options = scheduler.EXPR(config.workflowNotificationCron());
      options.name(this.getClass().getSimpleName());

      // Add scheduler to call depending on option passed.
      scheduler.schedule(this, options);
      log.debug("Scheduler added successfully name='{}'", this.getClass().getSimpleName());
    } else {
      log.debug("RetirementManagerScheduler disabled");
    }
  }

  /**
   * Deactivate. On deactivate component it will unschedule scheduler
   *
   * @param config the config
   */
  @Deactivate
  protected void deactivate(RetirementManagerSchedulerConfig config) {
    if (isAuthorInstance() && scheduler != null) {
      scheduler.unschedule(this.getClass().getSimpleName());
      scheduler = null;
    }
  }

  /**
   * Run. run() method will get call every day based on CRON
   */
  @Override
  public void run() {
    log.debug("RetirementManagerScheduler run >>>>>>>>>>>");
    try (ResourceResolver resResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      if (this.retireConfig.enableWorkflowNotificationReview()) {
        sendReviewNotification(resResolver);
      }

      if (this.retireConfig.enableWorkflowNotificationRetirement()) {
        startRetirementAndNotify(resResolver);
      }

    } catch (Exception e) {
      log.error("Exception in run >>>>>>> {}", e.getMessage());
    }
  }

  /**
   * Review RetirementManager notification.
   *
   * @param resResolver the resResolver
   */
  public void sendReviewNotification(ResourceResolver resResolver) {
    log.debug("RetirementManagerScheduler::Start querying Review Notification pages");
    List<String> reviewReminderPagePaths = queryService.getPagesDueTodayByDateProp(propReviewReminderDate);

    log.debug("RetirementManagerScheduler::End querying Review Notification pages.. Sending Notification");
    sendNotification(resResolver, reviewReminderPagePaths, emailTemplateRevReminderText,
        emailTemplateRevReminderSubject, false);
  }

  /**
   * Trigger Workflow and Send RetirementManager notification.
   *
   * @param resResolver the resResolver
   */
  public void startRetirementAndNotify(ResourceResolver resResolver) {
    log.debug("RetirementManagerScheduler::Start querying Retirement and Notify pages");
    List<String> retirementNotificationPagePaths = queryService
        .getPagesDueTodayByDateProp(propRetirementNotificationDate);

    log.debug("RetirementManagerScheduler::End querying Retirement and Notify pages.. Sending Notification");
    sendNotification(resResolver, retirementNotificationPagePaths, emailTemplateRetNotifyText,
        emailTemplateRetNotifySubject, true);

  }

  /**
   * Read text component value of email template content.
   *
   * @param textNode the text node
   * @param node     the node
   * @param path     the path
   * @return the string
   */
  public String processTextComponentFromEmailTemplate(Node textNode, Node node, String path) {
    log.debug("processTextComponentFromEmailTemplate >>>>>>>   ");
    String text = "";

    try {
      if (textNode.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
        String resourceType = textNode.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getValue().getString();
        if (resourceType.equals(GlobalConstants.TEXT_COMPONENT)) {
          text = textNode.getProperty("text").getValue().getString();

          String pageUrl = this.retireConfig.authorDomain().concat("/editor.html").concat(path).concat(".html");
          String pageTitle = node.getProperty(JCR_TITLE).getString();
          String pageTitleLink = "<a href='".concat(pageUrl).concat("' target='_blank'>").concat(pageTitle)
              .concat("</a>");
          text = text.trim().replace("{pageTitle}", pageTitleLink);
        }
      }
    } catch (RepositoryException e) {
      log.error("Iterator page jcr:content failed: {}", e.getMessage());
    }

    return text;
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
   * @param resolver                   the resolver
   * @param paths                      the paths
   * @param emailTemplateContainerText the emailTemplateContainerText
   * @param emailSubject               the emailSubject
   * @param triggerRetirement          the triggerRetirement
   */
  public void sendNotification(ResourceResolver resolver, List<String> paths, String emailTemplateContainerText,
      String emailSubject, Boolean triggerRetirement) {
    log.debug("sendNotification >>>>>>>   ");

    paths.stream().filter(item -> resolver.getResource(item) != null).forEach(path -> {
      try {
        if (triggerRetirement) {
          startWorkflow(resolver, WorkflowConstants.RETIREMENT_WORKFLOW, path);
        }

        Node node = Objects.requireNonNull(resolver.getResource(path + GlobalConstants.JCR_CONTENT_PATH))
            .adaptTo(Node.class);

        if (node != null) {
          if (node.hasProperty(GlobalConstants.PROP_JCR_CREATED_BY)) {
            // logic to send mail to author
            String author = node.getProperty(GlobalConstants.PROP_JCR_CREATED_BY).getString();
            log.debug("author is: {}", author);

            // Regular Expression
            String regex = "^(.+)@(.+)$";
            // Compile regular expression to get the pattern
            Pattern pattern = Pattern.compile(regex);

            // Create instance of matcher
            Matcher matcher = pattern.matcher(author);

            if (author != null && matcher.matches()) {
              Node emailTemplateParentNode = Objects
                  .requireNonNull(resolver.getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH
                      + emailTemplateContainerText.replace("/text", "")))
                  .adaptTo(Node.class);

              Node emailTemplateTextNode = Objects
                  .requireNonNull(resolver.getResource(
                      GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateContainerText))
                  .adaptTo(Node.class);

              String msg = "";
              String subject = node.getProperty(JCR_TITLE).getString() + emailSubject;
              log.debug("Email Subject: {}", subject);

              if (emailTemplateTextNode != null) {
                msg = processTextComponentFromEmailTemplate(emailTemplateTextNode, node, path);

                // send email once Day CQ Mail Configuration is ready
                log.debug("Sending email to author: {}", author);
                emailService.sendEmail(author, subject, msg);
              } else if (emailTemplateParentNode != null) {
                NodeIterator nodeItr = emailTemplateParentNode.getNodes();

                while (nodeItr.hasNext()) {
                  Node childNode = nodeItr.nextNode();
                  msg = processTextComponentFromEmailTemplate(childNode, node, path);
                }

                // send email once Day CQ Mail Configuration is ready
                log.debug("Sending email to author: {}", author);
                emailService.sendEmail(author, subject, msg);
              }
              log.debug("Mail content is: {}", msg);
            }
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
}
