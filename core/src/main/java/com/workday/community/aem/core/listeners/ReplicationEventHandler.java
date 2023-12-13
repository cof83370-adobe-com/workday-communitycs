package com.workday.community.aem.core.listeners;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.GlobalConstants.EVENTS_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.FAQ_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.KITS_AND_TOOLS_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.REFERENCE_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.TROUBLESHOOTING_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_PUBLIC_PAGE_PATH;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.DrupalService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
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
   * The subscriptionTemplatesList.
   */
  private List<String> subscriptionTemplatesList;

  /**
   * The CoveoIndexApiConfigService.
   */
  @Reference
  private CoveoIndexApiConfigService coveoIndexApiConfigService;

  /**
   * The DrupalService.
   */
  @Reference
  private DrupalService drupalService;

  /**
   * The cache manager.
   */
  @Reference
  private CacheManagerService cacheManager;

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
   * Get AEM - Drupal Conte Sync is enabled or not.
   *
   * @return AEM - Drupal Conte Sync is enabled or not.
   */
  public boolean isContentSyncEnabled() {
    return drupalService.isContentSyncEnabled();
  }

  /**
   * Activates the Replication Event Handler.
   */
  @Activate
  @Modified
  public void activate() {

    subscriptionTemplatesList = new ArrayList<>();
    subscriptionTemplatesList.add(EVENTS_TEMPLATE_PATH);
    subscriptionTemplatesList.add(FAQ_TEMPLATE_PATH);
    subscriptionTemplatesList.add(KITS_AND_TOOLS_TEMPLATE_PATH);
    subscriptionTemplatesList.add(REFERENCE_TEMPLATE_PATH);
    subscriptionTemplatesList.add(TROUBLESHOOTING_TEMPLATE_PATH);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handleEvent(Event event) {
    ReplicationAction action = getAction(event);
    String actionPath = action.getPath();
    log.error("\n Replication Event occurred at the path {}", actionPath);
    if (isCoveoEnabled()) {
      if ((actionPath.contains(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH)
          || actionPath.contains(WORKDAY_PUBLIC_PAGE_PATH))
          && (action.getType().equals(ReplicationActionType.ACTIVATE)
          || action.getType().equals(ReplicationActionType.DEACTIVATE)
          || action.getType().equals(ReplicationActionType.DELETE))
      ) {
        if (startCoveoJob(action) == null) {
          log.error("\n Error occurred while Creating Coveo push job for page");
        }
      }
    }
    if (isContentSyncEnabled()) {
      String actionType = determineActionType(action.getType());
      if ((action.getType().equals(ReplicationActionType.ACTIVATE)
          || action.getType().equals(ReplicationActionType.DEACTIVATE))
          && isCurrentPageIsInSubscriptionPageTypes(actionPath)) {

        if (startPageUpdateJob(actionType, actionPath) == null) {
          log.error("\n Error occurred while Page Create/update push job for page {}", actionPath);
        }
      } else if (action.getType().equals(ReplicationActionType.DELETE)) {
        if (startPageUpdateJob(actionType, actionPath) == null) {
          log.error("\n Error occurred while Page Delete push job for page {}", actionPath);
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
    String op = action.getType().equals(ReplicationActionType.ACTIVATE) ? "index" : "delete";
    Map<String, Object> jobProperties = createJobProperties(op, action.getPath());

    return jobManager.addJob(GlobalConstants.COMMUNITY_COVEO_JOB, jobProperties);
  }

  /**
   * Start Page Update job for sync up with Drupal content.
   *
   * @param actionType The ReplicationAction Type.
   * @param path       The Path of the page.
   * @return The new Job, or null if there was an error.
   */
  private Job startPageUpdateJob(String actionType, String path) {
    if (StringUtils.isBlank(actionType)) {
      return null;
    }
    Map<String, Object> jobProperties = createJobProperties(actionType, path);

    return jobManager.addJob(GlobalConstants.COMMUNITY_PAGE_UPDATE_JOB, jobProperties);
  }

  /**
   * Determine Action Type on the page Based on ReplicationActionType.
   *
   * @param type The Replication Action Type.
   * @return actionType, or empty based on ReplicationActionType.
   */
  private String determineActionType(ReplicationActionType type) {
    switch (type) {
      case ACTIVATE:
        return GlobalConstants.REPLICATION_ACTION_TYPE_ACTIVATE;
      case DEACTIVATE:
        return GlobalConstants.REPLICATION_ACTION_TYPE_DEACTIVATE;
      case DELETE:
        return GlobalConstants.REPLICATION_ACTION_TYPE_DELETE;
      default:
        return StringUtils.EMPTY;
    }
  }

  /**
   * Create Job properties Object based on actionType and path.
   *
   * @param actionType The Action Type on the page.
   * @param path       path of the page.
   * @return jobProperties Map Object for Job Properties
   */
  private Map<String, Object> createJobProperties(String actionType, String path) {
    Map<String, Object> jobProperties = new HashMap<>();
    List<String> paths = new ArrayList<>();
    paths.add(path);

    jobProperties.put("op", actionType);
    jobProperties.put("paths", paths);

    return jobProperties;
  }

  /**
   * Create Job properties Object based on actionType and path.
   *
   * @param path path of the page.
   * @return isCurrentPageInSubscriptionPagesList true if current page in subscription Page Types.
   */
  private boolean isCurrentPageIsInSubscriptionPageTypes(String path) {
    boolean isCurrentPageIsInSubscriptionPageTypes = false;

    try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      Resource pageResource = resourceResolver.getResource(path);
      if (pageResource != null) {
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        Page currentPage = pageManager.getContainingPage(pageResource);
        if (currentPage != null) {
          String templatePath = currentPage.getTemplate().getPath();
          isCurrentPageIsInSubscriptionPageTypes = checkIfItExistsInGivenTemplatesList(templatePath);
        }
      }
    } catch (CacheException e) {
      log.error("Exception occurred when adding author property to page {} ", e.getMessage());
    }

    return isCurrentPageIsInSubscriptionPageTypes;
  }

  /**
   * Create Job properties Object based on actionType and path.
   *
   * @param templatePath path of the template.
   * @return isCurrentPageInSubscriptionPagesList true if current page in subscription Page Types.
   */
  private boolean checkIfItExistsInGivenTemplatesList(String templatePath) {

    return subscriptionTemplatesList.contains(templatePath);
  }
}
