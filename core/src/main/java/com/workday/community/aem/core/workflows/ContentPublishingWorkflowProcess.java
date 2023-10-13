package com.workday.community.aem.core.workflows;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.WorkflowConstants.JCR_PATH;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.AssetReferenceSearch;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.QueryService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class ContentPublishingWorkflowProcess.
 */
@Slf4j
@Component(service = WorkflowProcess.class, property = {
    "process.label = Process to Activate Page, Referenced Assets and Book"})
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

      try (
          ResourceResolver resourceResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
        Session jcrSession = workflowSession.adaptTo(Session.class);

        if (null != jcrSession) {
          updatePageProperties(path, jcrSession, resourceResolver);
          replicatePage(jcrSession, path, resourceResolver);
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
  public void updatePageProperties(String pagePath, Session jcrSession,
                                   ResourceResolver resResolver) {
    LocalDate date = LocalDate.now();
    log.debug("Current Date: {}", date);

    LocalDate reviewReminderDate = date.plusMonths(10);
    LocalDate retirementNotificationDate = date.plusMonths(11);
    LocalDate scheduledRetirementDate = date.plusMonths(12);

    Calendar reviewReminderCalendar = Calendar.getInstance();
    Calendar retirementNotificationCalendar = Calendar.getInstance();
    Calendar scheduledRetirementCalendar = Calendar.getInstance();

    reviewReminderCalendar.set(reviewReminderDate.getYear(),
        reviewReminderDate.getMonthValue() - 1,
        reviewReminderDate.getDayOfMonth());
    retirementNotificationCalendar.set(retirementNotificationDate.getYear(),
        retirementNotificationDate.getMonthValue() - 1,
        retirementNotificationDate.getDayOfMonth());
    scheduledRetirementCalendar.set(scheduledRetirementDate.getYear(),
        scheduledRetirementDate.getMonthValue() - 1, scheduledRetirementDate.getDayOfMonth());

    Date revReminderDate = reviewReminderCalendar.getTime();
    Date retNotificationDate = retirementNotificationCalendar.getTime();
    Date scheduledRetDate = scheduledRetirementCalendar.getTime();

    DateFormat df = new SimpleDateFormat(GlobalConstants.ISO_8601_FORMAT);

    PageManager pageManager = resResolver.adaptTo(PageManager.class);
    Page currentPage = pageManager.getPage(pagePath);

    try {
      Node node = (Node) jcrSession.getItem(pagePath + GlobalConstants.JCR_CONTENT_PATH);
      if (currentPage == null || node == null
          || pagePath.contains(WccConstants.WORKDAY_PUBLIC_PAGE_PATH)) {
        return;
      }

      Template template = currentPage.getTemplate();
      if (template.getPath().equalsIgnoreCase(WorkflowConstants.EVENT_TEMPLATE_PATH)) {
        if (!node.hasProperty(WorkflowConstants.REVIEW_REMINDER_DATE)) {
          node.setProperty(WorkflowConstants.REVIEW_REMINDER_DATE, df.format(revReminderDate));
        }
        if (!node.hasProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE)) {
          node.setProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE,
              df.format(retNotificationDate));
        }
        if (!node.hasProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE)) {
          node.setProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE,
              df.format(scheduledRetDate));
        }
      } else {
        node.setProperty(WorkflowConstants.REVIEW_REMINDER_DATE, df.format(revReminderDate));
        node.setProperty(WorkflowConstants.RETIREMENT_NOTIFICATION_DATE,
            df.format(retNotificationDate));
        node.setProperty(WorkflowConstants.SCHEDULED_RETIREMENT_DATE,
            df.format(scheduledRetDate));
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
  public void replicatePage(Session jcrSession, String pagePath, ResourceResolver resResolver) {
    if (replicator == null) {
      return;
    }

    replicateReferencedAssets(jcrSession, pagePath, resResolver);

    log.debug("PAGE ACTIVATION STARTED");
    try {
      replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, pagePath);
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
  public void replicateReferencedAssets(Session jcrSession, String pagePath,
                                        ResourceResolver resResolver) {
    if (replicator == null) {
      return;
    }

    Node node = Objects.requireNonNull(resResolver.getResource(pagePath + "/jcr:content"))
        .adaptTo(Node.class);
    AssetReferenceSearch ref =
        new AssetReferenceSearch(node, DamConstants.MOUNTPOINT_ASSETS, resResolver);
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
  public void replicateBookNodes(String pagePath, Session jcrSession,
                                 ResourceResolver resResolver) {
    if (replicator == null || pagePath.contains(GlobalConstants.JCR_CONTENT_PATH)) {
      return;
    }

    try {
      List<String> paths = queryService.getBookNodesByPath(pagePath, null);
      paths.stream().filter(item -> resResolver.getResource(item) != null)
          .forEach(path -> {
            try {
              Node root =
                  Objects.requireNonNull(resResolver.getResource(path)).adaptTo(Node.class);
              final String pathToReplicate = root.getParent().getPath();
              replicator.replicate(jcrSession, ReplicationActionType.ACTIVATE, pathToReplicate);
            } catch (Exception exec) {
              log.error("Exception occured while replicating the book node and cause was: {}",
                  exec.getMessage());
            }
          });
      log.debug("Replicate node for page {}", pagePath);
    } catch (Exception exec) {
      log.error(
          "Exception occured while replicating the: {} page from book node. Exception was: {} :",
          pagePath,
          exec.getMessage());
    }
  }
}
