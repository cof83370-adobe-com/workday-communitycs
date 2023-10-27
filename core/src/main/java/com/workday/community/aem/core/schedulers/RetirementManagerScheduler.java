package com.workday.community.aem.core.schedulers;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.workday.community.aem.core.config.RetirementManagerSchedulerConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.services.CacheManagerService;
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
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
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
@Component(service = RetirementManagerScheduler.class, immediate = true)
@Designate(ocd = RetirementManagerSchedulerConfig.class)
public class RetirementManagerScheduler implements Runnable {

  /** The cache manager. */
  @Reference
  private CacheManagerService cacheManager;

  /** The query service. */
  @Reference
  private QueryService queryService;

  /** The author domain. */
  private String authorDomain = "";

  /** The message gateway service. */
  @Reference
  private MessageGatewayService messageGatewayService;

  /** The run mode config service. */
  @Reference
  private RunModeConfigService runModeConfigService;

  /** The scheduler. */
  @Reference
  private Scheduler scheduler;

  /** The resolver factory. */
  @Reference
  private ResourceResolverFactory resolverFactory;

  private final String emailTemplateRevReminderText = 
      "/workflows/review-reminder-10-months/jcr:content/root/container/container/text";

  private final String emailTemplateRetNotifyText = 
      "/workflows/retirement-notification-11-months/jcr:content/root/container/container/text";

  private final String propReviewReminderDate = "jcr:content/reviewReminderDate";

  private final String propRetirementNotificationDate = "jcr:content/retirementNotificationDate";

  private final String reviewReminderEmailSubject = " to Retire in 60 Days";

  private final String retirementNotifyEmailSubject = " to Retire in 30 Days";

  private boolean wfNotifyReview10Months = false;

  private boolean wfNotifyRetirement11Months = false;

  /**
   * Activate RetirementManagerScheduler scheduler.
   *
   * @param config the config
   */
  @Activate
  protected void activate(final RetirementManagerSchedulerConfig config) {
    if (isAuthorInstance()) {
      authorDomain = config.authorDomain();
      log.debug(" RetirementManagerScheduler activate method called and authorInstUrl : {}", authorDomain);

      if (config.workflowNotificationReview10Months()) {
        wfNotifyReview10Months = true;
      }

      if (config.workflowNotificationRetirement11Months()) {
        wfNotifyRetirement11Months = true;
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

    if (wfNotifyReview10Months || wfNotifyRetirement11Months) {
      ScheduleOptions options = scheduler.EXPR(config.workflowNotificationCron());
      options.name(config.schedulerName());

      // Add scheduler to call depending on option passed.
      scheduler.schedule(this, options);
      log.debug("Scheduler added successfully name='{}'", config.schedulerName());
    } else {
      log.debug("RetirementManagerScheduler disabled");
    }
  }

  /**
   * Removes the scheduler. Custom method to deactivate or unschedule scheduler.
   *
   *
   * @param config the config
   */
  public void removeScheduler(RetirementManagerSchedulerConfig config) {
    scheduler.unschedule(config.schedulerName());
  }

  /**
   * Deactivate. On deactivate component it will unschedule scheduler
   *
   * @param config the config
   */
  @Deactivate
  protected void deactivate(RetirementManagerSchedulerConfig config) {
    if (isAuthorInstance()) {
      removeScheduler(config);
    }
  }

  /**
   * Modified. On component modification change status will remove and add
   * scheduler
   *
   * @param config the config
   */
  @Modified
  protected void modified(RetirementManagerSchedulerConfig config) {
    if (isAuthorInstance()) {
      removeScheduler(config);
      addScheduler(config);
    }
  }

  /**
   * Run. run() method will get call every day based on CRON
   */
  @Override
  public void run() {
    log.debug("RetirementManagerScheduler run >>>>>>>>>>>");
    try (ResourceResolver resResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      if (wfNotifyReview10Months) {
        sendReviewNotification(resResolver);
      }

      if (wfNotifyRetirement11Months) {
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
    List<String> reviewReminderPagePaths = queryService.getPagesDueTodayByDateProp(propReviewReminderDate);

    sendNotification(resResolver, reviewReminderPagePaths, emailTemplateRevReminderText, reviewReminderEmailSubject,
        false);
  }

  /**
   * Trigger Workflow and Send RetirementManager notification.
   *
   * @param resResolver the resResolver
   */
  public void startRetirementAndNotify(ResourceResolver resResolver) {
    List<String> retirementNotificationPagePaths = queryService
        .getPagesDueTodayByDateProp(propRetirementNotificationDate);

    sendNotification(resResolver, retirementNotificationPagePaths, emailTemplateRetNotifyText,
        retirementNotifyEmailSubject, true);

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

          String pageUrl = authorDomain.concat("/editor.html").concat(path).concat(".html");
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
   * Send mail to author.
   *
   * @param authorMail the author mail
   * @param message    the message
   * @param node       the node
   * @throws RepositoryException the repository exception
   * @throws EmailException      the email exception
   */
  public void sendEmail(String authorMail, String message, Node node, String subject)
      throws RepositoryException, EmailException {

    Email email = new SimpleEmail();
    email.addTo(authorMail);
    email.setSubject(subject);
    email.setMsg(message);
    MessageGateway<Email> messageGateway;

    // Inject a Messagegateway Service and send the message
    messageGateway = messageGatewayService.getGateway(Email.class);

    // check the logs to see that messageGateway is not null
    messageGateway.send(email);
  }

  /**
   * Send notification to author.
   *
   * @param resolver                   the resolver
   * @param paths                      the paths
   * @param emailTemplateContainerText the emailTemplateContainerText
   * @param emailSubject               the emailSubject
   * @param triggerRetirment           the triggerRetirment
   */
  public void sendNotification(ResourceResolver resolver, List<String> paths, String emailTemplateContainerText,
      String emailSubject, Boolean triggerRetirment) {
    log.debug("sendNotification >>>>>>>   ");

    paths.stream().filter(item -> resolver.getResource(item) != null).forEach(path -> {
      try {
        if (triggerRetirment) {
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
                // sendEmail(author, msg, node, subject);
              } else if (emailTemplateParentNode != null) {
                NodeIterator nodeItr = emailTemplateParentNode.getNodes();

                while (nodeItr.hasNext()) {
                  Node childNode = nodeItr.nextNode();
                  msg = processTextComponentFromEmailTemplate(childNode, node, path);
                }

                // send email once Day CQ Mail Configuration is ready
                log.debug("Sending email to author: {}", author);
                // sendEmail(author, msg, node, subject);
              }
              log.debug("Mail content is: {}", msg);
            }
          }
        }

      } catch (Exception e) {
        log.error("Exception occured while sending the mail: {}", e.getMessage());
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
    log.debug("startWorkflow >>>>>>>   ");

    WorkflowSession workflowSession = resolver.adaptTo(WorkflowSession.class);

    // Create workflow model using model path
    WorkflowModel workflowModel = workflowSession.getModel(model);

    WorkflowData workflowData = workflowSession.newWorkflowData("JCR_PATH", payloadContentPath);

    // Pass value to workflow
    final Map<String, Object> workflowMetadata = new HashMap<>();
    workflowMetadata.put("pathInfo", payloadContentPath);

    // Trigger workflow
    workflowSession.startWorkflow(workflowModel, workflowData, workflowMetadata);
  }
}
