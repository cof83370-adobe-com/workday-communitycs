package com.workday.community.aem.core.jobs;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

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
import com.workday.community.aem.core.services.RetirementManagerJobConfigService;
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
  
  /** The retirement manager job config service. */
  @Reference
  private RetirementManagerJobConfigService retirementManagerJobConfigService;
  
  @Reference
  private Replicator replicator;

  /**
   * The email template review reminder text.
   */
  private final String emailTemplateRevReminderText = 
        "/workflows/review-reminder/jcr:content/root/container/container/text";

  /**
   * The email template retirement notification text.
   */
  private final String emailTemplateRetNotifyText = 
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
   * The email template review reminder subject.
   */
  private final String emailTemplateRevReminderSubject = 
      "/workflows/review-reminder/jcr:content/root/container/container/title";

  /**
   * The email template retirement notification subject.
   */
  private final String emailTemplateRetNotifySubject = 
      "/workflows/retirement-notification/jcr:content/root/container/container/title";
  
  /**
   * The Property archivalDate.
   */
  private final String propArchivalDate = "jcr:content/archivalDate";

  @Override
  public JobResult process(Job job) {
    log.debug("RetirementManagerJobConsumer process >>>>>>>>>>>");
    // Check for any custom parameters in the job
    String customParam = (String) job.getProperty("customJob");
    log.debug("Custom Job: {}", customParam);

    //logic for retirement scheduler
    runJob();

    // Return JobResult.OK if processing is successful
    return JobResult.OK;
  }
  
  /**
   * Runjob for RetirementManager notification.
   */
  public void runJob() {
    log.debug("RetirementManagerJobConsumer runJob >>>>>>>>>>>");
    try (ResourceResolver resResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      if (retirementManagerJobConfigService.getEnableWorkflowNotificationReview()) {
        sendReviewNotification(resResolver);
      }

      if (retirementManagerJobConfigService.getEnableWorkflowNotificationRetirement()) {
        startRetirementAndNotify(resResolver);
      }
      
      archiveContent(resResolver);

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
    log.debug("RetirementManagerJobConsumer::Start querying Review Notification pages");
    List<String> reviewReminderPagePaths = queryService.getPagesDueTodayByDateProp(propReviewReminderDate);

    log.debug("RetirementManagerJobConsumer::End querying Review Notification pages.. Sending Notification");
    sendNotification(resResolver, reviewReminderPagePaths, emailTemplateRevReminderText,
        emailTemplateRevReminderSubject, false);
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
          if (textNode.getProperty("text") != null) {
            text = textNode.getProperty("text").getValue().getString();

            String pageUrl = retirementManagerJobConfigService.getAuthorDomain()
                .concat("/editor.html").concat(path).concat(".html");
            if (node.getProperty(JCR_TITLE) != null) {
              String pageTitle = node.getProperty(JCR_TITLE).getString();
              String pageTitleLink = "<a href='".concat(pageUrl).concat("' target='_blank'>").concat(pageTitle)
                  .concat("</a>");
              text = text.trim().replace("{pageTitle}", pageTitleLink);
            }
          }
        }
      }
    } catch (RepositoryException e) {
      log.error("Exception in processTextComponentFromEmailTemplate: {}", e.getMessage());
    }

    return text;
  }

  /**
   * Read title component value of email template.
   *
   * @param titleNode the title node
   * @param node      the node
   * @param path      the path
   * @return the string
   */
  public String processTitleComponentFromEmailTemplate(Node titleNode, Node node, String path) {
    log.debug("processTitleComponentFromEmailTemplate >>>>>>>   ");
    String title = "";

    try {
      if (titleNode.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
        String resourceType = titleNode.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getValue().getString();
        if (resourceType.equals(GlobalConstants.TITLE_COMPONENT)) {
          if (titleNode.getProperty(JCR_TITLE) != null) {
            title = titleNode.getProperty(JCR_TITLE).getValue().getString();

            if (node.getProperty(JCR_TITLE) != null) {
              title = title.trim().replace("{pageTitle}", node.getProperty(JCR_TITLE).getString());
            }
          }
        }
      }
    } catch (RepositoryException e) {
      log.error("Exception in processTitleComponentFromEmailTemplate: {}", e.getMessage());
    }

    return title;
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
   * @param resolver                    the resolver
   * @param paths                       the paths
   * @param emailTemplateContainerText  the emailTemplateContainerText
   * @param emailTemplateContainerTitle the emailTemplateContainerTitle
   * @param triggerRetirement           the triggerRetirement
   */
  public void sendNotification(ResourceResolver resolver, List<String> paths, String emailTemplateContainerText,
      String emailTemplateContainerTitle, Boolean triggerRetirement) {
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

            // Regular Expression
            String regex = "^(.+)@(.+)$";
            // Compile regular expression to get the pattern
            Pattern pattern = Pattern.compile(regex);

            // Create instance of matcher
            Matcher matcher = pattern.matcher(author);

            if (author != null && matcher.matches()) {
              log.debug("author email is present");
              Node emailTemplateTextParentNode = Objects
                  .requireNonNull(resolver.getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH
                      + emailTemplateContainerText.replace("/text", "")))
                  .adaptTo(Node.class);

              Node emailTemplateTextNode = Objects
                  .requireNonNull(resolver.getResource(
                      GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateContainerText))
                  .adaptTo(Node.class);

              Node emailTemplateTitleParentNode = Objects
                  .requireNonNull(resolver.getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH
                      + emailTemplateContainerTitle.replace("/title", "")))
                  .adaptTo(Node.class);

              Node emailTemplateTitleNode = Objects
                  .requireNonNull(resolver.getResource(
                      GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateContainerTitle))
                  .adaptTo(Node.class);

              String msg = "";
              String subject = "";

              if (emailTemplateTitleNode != null) {
                subject = processTitleComponentFromEmailTemplate(emailTemplateTitleNode, node, path);
              } else if (emailTemplateTitleParentNode != null) {
                NodeIterator nodeItr = emailTemplateTitleParentNode.getNodes();

                while (nodeItr.hasNext()) {
                  Node childNode = nodeItr.nextNode();
                  String emailSubjectTitle = processTitleComponentFromEmailTemplate(childNode, node, path);
                  if (!(emailSubjectTitle.isBlank())) {
                    subject = emailSubjectTitle;
                    break;
                  }
                }
              }

              if (emailTemplateTextNode != null) {
                msg = processTextComponentFromEmailTemplate(emailTemplateTextNode, node, path);
                emailService.sendEmail(author, subject, msg);
              } else if (emailTemplateTextParentNode != null) {
                NodeIterator nodeItr = emailTemplateTextParentNode.getNodes();

                while (nodeItr.hasNext()) {
                  Node childNode = nodeItr.nextNode();
                  String emailBodyText = processTextComponentFromEmailTemplate(childNode, node, path);
                  if (!(emailBodyText.isBlank())) {
                    msg = emailBodyText;
                    break;
                  }
                }
                emailService.sendEmail(author, subject, msg);
              }
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
        if (pageManager != null) {
          page = pageManager.getPage(path);
          if (page != null) {
            log.debug("before archive >>>>>>>");

            // Deactivate the page before archiving
            replicator.replicate(session, ReplicationActionType.DEACTIVATE, path);

            // Create version for page before deleting
            pageManager.createRevision(page);

            // Delete page
            pageManager.delete(page, false);

            log.debug("after archive >>>>>>>");
          }
        }
      } catch (ReplicationException e) {
        log.error("ReplicationException occured in archiveContent method: {}", e.getMessage());
      } catch (WCMException exec) {
        log.error("WCMException occured in archiveContent method: {} ", exec.getMessage());
      }
    });
  }
}