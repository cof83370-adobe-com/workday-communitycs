package com.workday.community.aem.core.listeners;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.GlobalConstants.EVENTS_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.FAQ_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.KITS_AND_TOOLS_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.REFERENCE_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.REPLICATION_ACTION_TYPE_ACTIVATE;
import static com.workday.community.aem.core.constants.GlobalConstants.REPLICATION_ACTION_TYPE_DEACTIVATE;
import static com.workday.community.aem.core.constants.GlobalConstants.TROUBLESHOOTING_TEMPLATE_PATH;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_PROP;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_VAL;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.dto.AemContentDto;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for content Activate/DeActivate/deletes and pushes those changes to Drupal.
 */
@Slf4j
@Component(
    service = JobConsumer.class,
    immediate = true,
    property = {
        JobConsumer.PROPERTY_TOPICS + "=" + GlobalConstants.COMMUNITY_PAGE_UPDATE_JOB
    }
)
public class PageUpdateJobConsumer implements JobConsumer {


  Map<String, String> bundleMap;

  /**
   * The cache manager.
   */
  @Reference
  private CacheManagerService cacheManager;


  /**
   * The run mode config service.
   */
  @Reference
  private RunModeConfigService runModeConfigService;

  /**
   * The drupal service.
   */
  @Reference
  private DrupalService drupalService;

  /**
   * Activates the bundleMap.
   */
  @Activate
  @Modified
  public void activate() {

    bundleMap = new HashMap<>();
    bundleMap.put(EVENTS_TEMPLATE_PATH, "event");
    bundleMap.put(FAQ_TEMPLATE_PATH, "faq");
    bundleMap.put(KITS_AND_TOOLS_TEMPLATE_PATH, "kits_and_tools");
    bundleMap.put(REFERENCE_TEMPLATE_PATH, "reference");
    bundleMap.put(TROUBLESHOOTING_TEMPLATE_PATH, "troubleshooting");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobResult process(Job job) {
    List<String> paths = job.getProperty("paths", List.class);
    String op = (String) job.getProperty("op");
    if (StringUtils.isNotBlank(op)) {
      switch (op) {
        case REPLICATION_ACTION_TYPE_ACTIVATE:
          return notifyWhenPageGotActivatedOrDeActivated(paths, REPLICATION_ACTION_TYPE_ACTIVATE);
        case REPLICATION_ACTION_TYPE_DEACTIVATE:
          return notifyWhenPageGotActivatedOrDeActivated(paths, REPLICATION_ACTION_TYPE_DEACTIVATE);
        case GlobalConstants.REPLICATION_ACTION_TYPE_DELETE:
          return notifyWhenPageGotDeleted(paths);
        default:
          log.error("Unknown operation: {}", op);
          return JobResult.FAILED;
      }
    }

    log.error("Missing required properties in the job: path {} and op{}.", paths.toString(), op);
    return JobResult.FAILED;
  }

  /**
   * Notify When Page Got Activate or Deactivated.
   *
   * @param pathsList pages list.
   * @param actionType Action Type.
   *
   * @return JobResult Failed or Pass based on result.
   */
  private JobResult notifyWhenPageGotActivatedOrDeActivated(List<String> pathsList, String actionType) {
    String path = pathsList.get(0);
    if (StringUtils.isNotBlank(path)) {
      return processPageActivationOrDeactivation(path, actionType);
    }
    return JobResult.FAILED;
  }

  /**
   * Notify When Page Got Deleted.
   *
   * @param pathsList pages list.
   * @return JobResult Failed or Pass based on result.
   */
  private JobResult notifyWhenPageGotDeleted(List<String> pathsList) {
    String path = pathsList.get(0);
    if (StringUtils.isNotBlank(path)) {
      return processPageDeletion(path);
    }
    return JobResult.FAILED;
  }

  /**
   * process When Page got Activated or Deactivated.
   *
   * @param path page path.
   * @param actionType Action Type (Activate or Deactivate)
   *
   * @return JobResult Failed or Pass based on result.
   */
  private JobResult processPageActivationOrDeactivation(String path, String actionType) {
    try (ResourceResolver resourceResolver = getResourceResolver()) {
      Resource pageResource = resourceResolver.getResource(path);
      if (pageResource != null) {
        Page currentPage = pageResource.adaptTo(Page.class);
        if (currentPage != null) {
          // Extract and prepare data...
          AemContentDto aemContentDto = prepareAemContentDto(currentPage, actionType);
          ApiResponse apiResponse = drupalService.createOrUpdateEntity(aemContentDto);
          if (null != apiResponse && apiResponse.getResponseCode() == HttpStatus.SC_OK
              || apiResponse.getResponseCode() == HttpStatus.SC_CREATED) {
            return JobResult.OK;
          }
        }
      }
    } catch (CacheException | DrupalException e) {
      log.error("Error occurred: {}", e.getMessage());
    }
    return JobResult.FAILED;
  }

  /**
   * process When Page got Activated or Deactivated.
   *
   * @param path page path.
   * @return JobResult Failed or Pass based on result.
   */
  private JobResult processPageDeletion(String path) {
    try {
      path = runModeConfigService.getPublishInstanceDomain()
          .concat(path).concat(".html");
      ApiResponse deleteEntityResponse = drupalService.deleteEntity(path);
      if (null != deleteEntityResponse && deleteEntityResponse.getResponseCode() == HttpStatus.SC_NO_CONTENT) {
        return JobResult.OK;
      }
    } catch (DrupalException e) {
      log.error("Error occurred: {}", e.getMessage());
    }

    return JobResult.FAILED;
  }

  /**
   * Get Resource resolver Object.
   *
   * @return resourceResolver Object.
   *
   * @throws CacheException throws Cache Exception.
   */
  private ResourceResolver getResourceResolver() throws CacheException {
    return cacheManager.getServiceResolver(ADMIN_SERVICE_USER);
  }

  /**
   * Prepare AemContentDto bean.
   *
   * @param currentPage current page object.
   * @param actionType Action Type.
   * @return aemContentDto bean
   */
  private AemContentDto prepareAemContentDto(Page currentPage, String actionType) {
    ValueMap currentPageProperties = currentPage.getProperties();
    String fieldAemStatus = determineAemStatus(currentPageProperties);
    if (StringUtils.isBlank(fieldAemStatus) && actionType.equalsIgnoreCase(REPLICATION_ACTION_TYPE_ACTIVATE)) {
      fieldAemStatus = "published";
    } else if (StringUtils.isBlank(fieldAemStatus) && actionType.equalsIgnoreCase(REPLICATION_ACTION_TYPE_DEACTIVATE)) {
      fieldAemStatus = "unpublished";
    }
    AemContentDto aemContentDto = new AemContentDto();
    // Set AemContentDto properties...
    List<String> terms = Arrays.asList(Optional.ofNullable(currentPage.getProperties()
            .get(GlobalConstants.CQ_TAGS, String[].class))
        .orElse(new String[0]));

    aemContentDto.setFieldAemIdentifier(currentPageProperties.get(GlobalConstants.JCR_UUID, String.class));
    aemContentDto.setTerms(terms);
    aemContentDto.setFieldAemStatus(fieldAemStatus);
    aemContentDto.setLabel(currentPage.getName());
    List<String> aclTags = Arrays.asList(Optional.ofNullable(currentPage.getProperties()
            .get(GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, String[].class))
        .orElse(new String[0]));
    aemContentDto.setAccess(aclTags);
    aemContentDto.setFieldAemLink(runModeConfigService.getPublishInstanceDomain()
        .concat(currentPage.getPath()).concat(".html"));
    aemContentDto.setOwner(currentPageProperties.get(GlobalConstants.PROP_AUTHOR, String.class));
    aemContentDto.setBundle(bundleMap.get(currentPage.getTemplate().getPath()));

    return aemContentDto;
  }

  /**
   * Determine AEM Status of the page.
   *
   * @param currentPageProperties ValueMap object.
   * @return archived or empty based on retirement status.
   */
  private String determineAemStatus(ValueMap currentPageProperties) {
    String retirementStatus = currentPageProperties.get(RETIREMENT_STATUS_PROP, "");
    return retirementStatus.equalsIgnoreCase(RETIREMENT_STATUS_VAL) ? "archived" : StringUtils.EMPTY;
  }

}
