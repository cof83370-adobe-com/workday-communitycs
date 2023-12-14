package com.workday.community.aem.core.utils;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.EmailService;
import com.workday.community.aem.core.services.WorkflowConfigService;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Utility class for common workflow operations.
 */
@Slf4j
public class WorkflowUtils {

  /**
   * Send notification.
   *
   * @param author                   the author
   * @param resolver                 the resolver
   * @param emailTemplateBodyPath    the emailTemplateBodyPath
   * @param emailTemplateSubjectPath the emailTemplateSubjectPath
   * @param path                     the path
   */
  public static void sendNotification(String author, ResourceResolver resolver, String emailTemplateBodyPath,
      String emailTemplateSubjectPath, String path, Node node, WorkflowConfigService workflowConfigService,
      EmailService emailService) {
    log.debug("sendNotification >>>>>>>   ");

    // Regular Expression
    String regex = "^(.+)@(.+)$";
    // Compile regular expression to get the pattern
    Pattern pattern = Pattern.compile(regex);

    // Create instance of matcher
    Matcher matcher = pattern.matcher(author);

    if (author == null || !matcher.matches()) {
      log.debug("Invalid email id: {}", author);
      return;
    }

    try {
      String subject = getEmailSubject(resolver, emailTemplateSubjectPath, path, 
          node, workflowConfigService, emailService);

      sendEmailNotification(resolver, emailTemplateBodyPath, path, node, subject, author, workflowConfigService,
          emailService);
    } catch (Exception e) {
      log.error("Exception occured in sendNotification: {}", e.getMessage());
    }
  }

  /**
   * Read title component value of email template.
   *
   * @param titleNode the title node
   * @param node      the node
   * @param path      the path
   * @return the string
   */
  public static String processTitleComponentFromEmailTemplate(Node titleNode, Node node, String path,
      WorkflowConfigService workflowConfigService, EmailService emailService) {
    log.debug("processTitleComponentFromEmailTemplate >>>>>>>  {} ", path);
    String title = "";

    try {
      if (titleNode != null && titleNode.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
        String resourceType = titleNode.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getValue().getString();
        if (resourceType.equals(GlobalConstants.TITLE_COMPONENT)) {
          Property titleProperty = titleNode.getProperty(JCR_TITLE);
          if (titleProperty != null) {
            title = titleProperty.getValue().getString();

            if (node != null && node.getProperty(JCR_TITLE) != null) {
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
   * Read text component value of email template content.
   *
   * @param textNode the text node
   * @param node     the node
   * @param path     the path
   * @return the string
   */
  public static String processTextComponentFromEmailTemplate(Node textNode, Node node, String path,
      WorkflowConfigService workflowConfigService, EmailService emailService) {
    log.debug("processTextComponentFromEmailTemplate >>>>>>>  {} ", path);
    String text = "";

    try {
      if (textNode != null && textNode.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
        String resourceType = textNode.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getValue().getString();
        if (resourceType.equals(GlobalConstants.TEXT_COMPONENT)) {
          Property textProperty = textNode.getProperty("text");
          if (textProperty != null) {
            text = textProperty.getValue().getString();

            String pageUrl = workflowConfigService.getAuthorDomain().concat("/editor.html").concat(path)
                .concat(".html");
            if (node != null && node.getProperty(JCR_TITLE) != null) {
              String pageTitle = node.getProperty(JCR_TITLE).getString();
              String pageTitleLink = "<a href='".concat(pageUrl).concat("' target='_blank'>").concat(pageTitle)
                  .concat("</a>");
              text = text.trim().replace("{pageTitle}", pageTitleLink);
            }

            if (text.trim().contains("{dateTime}")) {
              Timestamp timestamp = new Timestamp(System.currentTimeMillis());
              text = text.trim().replace("{dateTime}", timestamp.toString());
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
   * Get email subject.
   *
   * @param resolver                    the resolver
   * @param emailTemplateSubjectPath    the emailTemplateSubjectPath
   * @param path                        the path
   * @param node                        the node
   * @return the string
   */
  public static String getEmailSubject(ResourceResolver resolver, String emailTemplateSubjectPath, String path,
      Node node, WorkflowConfigService workflowConfigService, EmailService emailService) {
    log.debug("getMailSubject >>>>>>>  {} ", path);

    String subject = "";
    try {
      Node emailTemplateTitleParentNode = Objects
          .requireNonNull(resolver.getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH
              + emailTemplateSubjectPath.replace("/title", "")))
          .adaptTo(Node.class);

      Node emailTemplateTitleNode = Objects
          .requireNonNull(resolver
              .getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateSubjectPath))
          .adaptTo(Node.class);

      if (emailTemplateTitleNode != null) {
        subject = processTitleComponentFromEmailTemplate(emailTemplateTitleNode, node, path, workflowConfigService,
            emailService);
      } else if (emailTemplateTitleParentNode != null) {
        NodeIterator nodeItr = emailTemplateTitleParentNode.getNodes();

        while (nodeItr.hasNext()) {
          Node childNode = nodeItr.nextNode();
          String emailSubjectTitle = processTitleComponentFromEmailTemplate(childNode, node, path,
              workflowConfigService, emailService);
          if (!(emailSubjectTitle.isBlank())) {
            subject = emailSubjectTitle;
            break;
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception occured in getMailSubject: {}", e.getMessage());
    }

    return subject;
  }

  /**
   * Send email notification to author.
   *
   * @param resolver                   the resolver
   * @param emailTemplateBodyPath      the emailTemplateBodyPath
   * @param path                       the path
   * @param node                       the node
   * @param subject                    the subject
   * @param author                     the author
   */
  public static void sendEmailNotification(ResourceResolver resolver, String emailTemplateBodyPath, String path,
      Node node, String subject, String author, WorkflowConfigService workflowConfigService,
      EmailService emailService) {
    log.debug("sendEmailNotification >>>>>>>  {} ", path);

    String msg = "";

    try {
      Node emailTemplateTextParentNode = Objects.requireNonNull(resolver.getResource(
          GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateBodyPath.replace("/text", "")))
          .adaptTo(Node.class);

      Node emailTemplateTextNode = Objects
          .requireNonNull(resolver
              .getResource(GlobalConstants.COMMUNITY_CONTENT_NOTIFICATIONS_ROOT_PATH + emailTemplateBodyPath))
          .adaptTo(Node.class);

      if (emailTemplateTextNode != null) {
        msg = processTextComponentFromEmailTemplate(emailTemplateTextNode, node, path, workflowConfigService,
            emailService);
        if (!subject.isBlank() && !msg.isBlank()) {
          emailService.sendEmail(author, subject, msg);
        } else {
          log.debug("subject and message are blank");
        }
      } else if (emailTemplateTextParentNode != null) {
        NodeIterator nodeItr = emailTemplateTextParentNode.getNodes();

        while (nodeItr.hasNext()) {
          Node childNode = nodeItr.nextNode();
          String emailBodyText = processTextComponentFromEmailTemplate(childNode, node, path, workflowConfigService,
              emailService);
          if (!(emailBodyText.isBlank())) {
            msg = emailBodyText;
            break;
          }
        }
        if (!subject.isBlank() && !msg.isBlank()) {
          emailService.sendEmail(author, subject, msg);
        } else {
          log.debug("subject and message are blank");
        }
      }
    } catch (Exception e) {
      log.error("Exception occured in sendEmailNotification: {}", e.getMessage());
    }
  }
}
