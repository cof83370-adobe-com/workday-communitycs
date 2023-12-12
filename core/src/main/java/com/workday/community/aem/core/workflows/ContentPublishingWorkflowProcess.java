package com.workday.community.aem.core.workflows;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.WorkflowConstants.JCR_PATH;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

import com.adobe.granite.taskmanagement.Task;
import com.adobe.granite.taskmanagement.TaskManager;
import com.adobe.granite.taskmanagement.TaskManagerException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.AssetReferenceSearch;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.EmailService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.WorkflowConfigService;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class ContentPublishingWorkflowProcess.
 */
@Slf4j
@Component(service = WorkflowProcess.class, property = {
    "process.label = Process to Activate Page, Referenced Assets and Book" })
public class ContentPublishingWorkflowProcess implements WorkflowProcess {

  @Reference
  private Replicator replicator;

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
  
  /** The workflow config service. */
  @Reference
  private WorkflowConfigService workflowConfigService;
  
  /** The email service. */
  @Reference
  private EmailService emailService;
  
  /**
   * The email template publish text.
   */
  private final String emailTemplatePagePublishText = 
        "/workflows/publish-notification/jcr:content/root/container/container/text";
  
  /**
   * The email template publish subject.
   */
  private final String emailTemplatePagePublishSubject = 
      "/workflows/publish-notification/jcr:content/root/container/container/title";

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
    String path;

    log.debug("Payload type: {}", payloadType);
    if (StringUtils.equals(payloadType, JCR_PATH)) {
      path = workItem.getWorkflowData().getPayload().toString();
      log.debug("Payload path: {}", path);

      String wfInitiator = workItem.getWorkflow().getInitiator();

      try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page currentPage = pageManager.getPage(path);

        Session jcrSession = workflowSession.adaptTo(Session.class);

        if (null != jcrSession) {
          updatePageProperties(path, jcrSession, resourceResolver, currentPage);
          replicatePage(jcrSession, path, resourceResolver, wfInitiator, currentPage);
          replicateBookNodes(path, jcrSession, resourceResolver);
        }
      } catch (Exception e) {
        log.error("payload type - {} is not valid", payloadType);
      }
    }
  }

  /**
   * Replicate the book nodes.
   *
   * @param pagePath    the page path
   * @param jcrSession  the jcr session
   * @param resResolver the ResourceResolver
   */
  public void updatePageProperties(String pagePath, Session jcrSession, ResourceResolver resResolver,
      Page currentPage) {
    LocalDate date = LocalDate.now();
    log.debug("Current Date: {}", date);

    LocalDate reviewReminderDate = date.plusMonths(10);
    LocalDate retirementNotificationDate = date.plusMonths(11);
    LocalDate scheduledRetirementDate = date.plusMonths(12);

    Calendar reviewReminderCalendar = Calendar.getInstance();
    Calendar retirementNotificationCalendar = Calendar.getInstance();
    Calendar scheduledRetirementCalendar = Calendar.getInstance();

    Calendar calendar = Calendar.getInstance();

    reviewReminderCalendar.set(reviewReminderDate.getYear(), reviewReminderDate.getMonthValue() - 1,
        reviewReminderDate.getDayOfMonth());
    retirementNotificationCalendar.set(retirementNotificationDate.getYear(),
        retirementNotificationDate.getMonthValue() - 1, retirementNotificationDate.getDayOfMonth());
    scheduledRetirementCalendar.set(scheduledRetirementDate.getYear(), scheduledRetirementDate.getMonthValue() - 1,
        scheduledRetirementDate.getDayOfMonth());

    try {
      Node node = (Node) jcrSession.getItem(pagePath + GlobalConstants.JCR_CONTENT_PATH);
      if (currentPage == null || node == null || pagePath.contains(WccConstants.WORKDAY_PUBLIC_PAGE_PATH)) {
        return;
      }

      Template template = currentPage.getTemplate();
      if (template.getPath().equalsIgnoreCase(WorkflowConstants.EVENT_TEMPLATE_PATH)) {
        if (!node.hasProperty(WorkflowConstants.REVIEW_REMINDER_DATE)) {
          node.setProperty(WorkflowConstants.REVIEW_REMINDER_DATE, reviewReminderCalendar);
        }
        if (!node.hasProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE)) {
          node.setProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE, retirementNotificationCalendar);
        }
        if (!node.hasProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE)) {
          node.setProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE, scheduledRetirementCalendar);
        }
      } else {
        node.setProperty(WorkflowConstants.REVIEW_REMINDER_DATE, reviewReminderCalendar);
        node.setProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE, retirementNotificationCalendar);
        node.setProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE, scheduledRetirementCalendar);
      }

      if (node.hasProperty(WorkflowConstants.RETIREMENT_STATUS_PROP)) {
        if (node.getProperty(WorkflowConstants.RETIREMENT_STATUS_PROP) != null
            && node.getProperty(WorkflowConstants.RETIREMENT_STATUS_PROP).getString()
                .equalsIgnoreCase(WorkflowConstants.RETIREMENT_STATUS_VAL)) {
          node.setProperty(WorkflowConstants.RETIREMENT_STATUS_PROP, WorkflowConstants.UNRETIREMENT_STATUS_VAL);
        }

        if (node.hasProperty(WorkflowConstants.ACTUAL_RETIREMENT_DATE)) {
          node.getProperty(WorkflowConstants.ACTUAL_RETIREMENT_DATE).remove();
        }

        node.setProperty(WorkflowConstants.UNRETIREMENT_DATE, calendar);
      }

      if (node.hasProperty(GlobalConstants.PROP_SUPPRESS_UPDATED_DATE)) {
        node.getProperty(GlobalConstants.PROP_SUPPRESS_UPDATED_DATE).remove();
      } else {
        node.setProperty(GlobalConstants.PROP_UPDATED_DATE, calendar);
      }

      if (!node.hasProperty(GlobalConstants.PROP_POSTED_DATE)
          || node.getProperty(GlobalConstants.PROP_POSTED_DATE).getDate() == null) {
        node.setProperty(GlobalConstants.PROP_POSTED_DATE, calendar);
      }

      jcrSession.save();
    } catch (RepositoryException e) {
      log.error("RepositoryException occurred in updatePageProperties {}:", e.getMessage());
    }
  }

  /**
   * Replicate page.
   *
   * @param jcrSession  the jcr session
   * @param pagePath    the page path
   * @param resResolver the ResourceResolver
   */
  public void replicatePage(Session jcrSession, String pagePath, ResourceResolver resResolver, String initiator,
      Page currentPage) {
    if (replicator == null) {
      return;
    }

    replicateReferencedAssets(jcrSession, pagePath, resResolver);

    log.debug("PAGE ACTIVATION STARTED");
    try {
      replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, pagePath);

      ReplicationStatus repStatus = replicator.getReplicationStatus(jcrSession, pagePath);
      if (repStatus.isActivated()) {
        sendInboxNotification(resResolver, initiator, currentPage);
      }
    } catch (ReplicationException e) {
      log.error("Exception occured while replicatePage method: {}", e.getMessage());
    }
  }

  /**
   * Replicate Assets references of a page.
   *
   * @param jcrSession  the jcr session
   * @param pagePath    the page path
   * @param resResolver the ResourceResolver
   */
  public void replicateReferencedAssets(Session jcrSession, String pagePath, ResourceResolver resResolver) {
    if (replicator == null) {
      return;
    }

    Node node = Objects.requireNonNull(resResolver.getResource(pagePath + GlobalConstants.JCR_CONTENT_PATH))
        .adaptTo(Node.class);
    AssetReferenceSearch ref = new AssetReferenceSearch(node, DamConstants.MOUNTPOINT_ASSETS, resResolver);
    Map<String, Asset> allref = new HashMap<>(ref.search());

    try {
      for (Map.Entry<String, Asset> entry : allref.entrySet()) {
        String assetPath = entry.getKey();
        log.debug("Asset activation started for {}", assetPath);
        replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, assetPath);
      }
    } catch (ReplicationException e) {
      log.error("Exception occured while replicateAssets method: {}", e.getMessage());
    }
  }

  /**
   * Replicate the book nodes.
   *
   * @param pagePath    the page path
   * @param jcrSession  the jcr session
   * @param resResolver the ResourceResolver
   */
  public void replicateBookNodes(String pagePath, Session jcrSession, ResourceResolver resResolver) {
    if (replicator == null || pagePath.contains(GlobalConstants.JCR_CONTENT_PATH)) {
      return;
    }

    try {
      List<String> paths = queryService.getBookNodesByPath(pagePath, null);
      paths.stream().filter(item -> resResolver.getResource(item) != null).forEach(path -> {
        try {
          Node root = Objects.requireNonNull(resResolver.getResource(path)).adaptTo(Node.class);
          final String pathToReplicate = root.getParent().getPath();
          replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, pathToReplicate);
        } catch (Exception exec) {
          log.error("Exception occured while replicating the book node and cause was: {}", exec.getMessage());
        }
      });
      log.debug("Replicate node for page {}", pagePath);
    } catch (Exception exec) {
      log.error("Exception occured while replicating the: {} page from book node. Exception was: {} :", pagePath,
          exec.getMessage());
    }
  }

  /**
   * Send inbox notification to the initiator.
   *
   * @param resResolver the ResourceResolver
   * @param assignee   the initiator
   */
  private void sendInboxNotification(ResourceResolver resResolver, String initiator, Page payloadPage) {
    log.debug("sendInboxNotification start");
    String authorId = null;
    try {
      TaskManager taskManager = resResolver.adaptTo(TaskManager.class);
      Task newTask = taskManager.getTaskManagerFactory().newTask(WorkflowConstants.TASK_TYPE_NOTIFICATION);
      if (newTask != null) {
        newTask.setName(WorkflowConstants.NOTIFICATION_NAME_CONTENT_PUBLISHED);
        if (payloadPage != null) {
          newTask.setContentPath(payloadPage.getPath());
          newTask.setDescription(payloadPage.getTitle());
          newTask.setCurrentAssignee(initiator); // workflow initiator inbox notification
          taskManager.createTask(newTask);

          UserManager userManager = resResolver.adaptTo(UserManager.class);
          String author = (String) payloadPage.getProperties().get(GlobalConstants.PROP_AUTHOR);
          Iterator<Authorizable> iter = userManager.findAuthorizables("email", author, UserManager.SEARCH_TYPE_USER);
          while (iter != null && iter.hasNext()) {
            Authorizable auth = iter.next();
            authorId = auth.getID();
          }

          if (null != authorId) {
            log.debug("author has AEM Account");
            if (!authorId.equalsIgnoreCase(initiator)) {
              log.debug("author and initiator are not same");
              newTask.setCurrentAssignee(authorId); // author inbox notification
              taskManager.createTask(newTask);
            } else {
              log.debug("author: {} and initiator: {} are same", authorId, initiator);
            }
          } else {
            log.debug("author has no AEM Account");
            // send email notification to author
            sendMailNotification(author, resResolver, emailTemplatePagePublishText, emailTemplatePagePublishSubject,
                payloadPage.getPath());
          }
        }
      }
    } catch (TaskManagerException e) {
      log.error("Exception occured while sending inbox notification: {}", e.getMessage());
    } catch (RepositoryException e) {
      log.error("RepositoryException in MetadataImpl::getFullNameByUserID: {}", e.getMessage());
    }

    log.debug("sendInboxNotification end");
  }
  
  /**
   * Send mail notification to author.
   *
   * @param author                      the author
   * @param resolver                    the resolver
   * @param emailTemplateContainerText  the emailTemplateContainerText
   * @param emailTemplateContainerTitle the emailTemplateContainerTitle
   * @param path                        the path
   */
  public void sendMailNotification(String author, ResourceResolver resolver, String emailTemplateContainerText,
      String emailTemplateContainerTitle, String path) {
    log.debug("sendMailNotification >>>>>>>   ");

    try {
      Node node = Objects.requireNonNull(resolver.getResource(path + GlobalConstants.JCR_CONTENT_PATH))
          .adaptTo(Node.class);

      if (node != null) {
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
              .requireNonNull(resolver
                  .getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateContainerText))
              .adaptTo(Node.class);

          Node emailTemplateTitleParentNode = Objects
              .requireNonNull(resolver.getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH
                  + emailTemplateContainerTitle.replace("/title", "")))
              .adaptTo(Node.class);

          Node emailTemplateTitleNode = Objects
              .requireNonNull(resolver
                  .getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateContainerTitle))
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
        } else {
          log.debug("email id is not valid: {}", author);
        }
      }
    } catch (Exception e) {
      log.error("Exception occured in sendMailNotification: {}", e.getMessage());
    }
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

            String pageUrl = workflowConfigService.getAuthorDomain()
                .concat("/editor.html").concat(path).concat(".html");
            if (node.getProperty(JCR_TITLE) != null) {
              String pageTitle = node.getProperty(JCR_TITLE).getString();
              String pageTitleLink = "<a href='".concat(pageUrl).concat("' target='_blank'>").concat(pageTitle)
                  .concat("</a>");
              text = text.trim().replace("{pageTitle}", pageTitleLink);
            }
            
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            text = text.trim().replace("{dateTime}", timestamp.toString());
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
}
