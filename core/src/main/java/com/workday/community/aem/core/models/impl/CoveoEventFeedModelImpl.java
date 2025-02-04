package com.workday.community.aem.core.models.impl;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static java.util.Objects.requireNonNull;

import com.adobe.xfa.ut.StringUtils;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoEventFeedModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.DamUtils;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * The CoveoEventFeedModel implementation Class.
 */
@Slf4j
@Model(adaptables = {
    Resource.class,
    SlingHttpServletRequest.class
}, adapters = { CoveoEventFeedModel.class }, resourceType = {
    CoveoEventFeedModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CoveoEventFeedModelImpl implements CoveoEventFeedModel {

  protected static final String RESOURCE_TYPE =
      "/content/workday-community/components/common/coveoeventfeed";

  private static final String MODEL_CONFIG_FILE =
      "/content/dam/workday-community/resources/event-feed-criteria.json";

  private static final String EVENT_PATH_ROOT = "/jcr:content/root/container/";

  private JsonObject modelConfig;

  private JsonObject searchConfig;

  @Self
  private SlingHttpServletRequest request;

  @ValueMapValue
  private String featuredEvent;

  /**
   * SearchConfig service object.
   */
  @OSGiService
  private SearchApiConfigService searchConfigService;

  @OSGiService
  private UserService userService;

  /**
   * The drupal service object.
   */
  @OSGiService
  private DrupalService drupalService;

  /**
   * Coveo event feed mode init.
   *
   * @param request The request object.
   */
  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      this.request = request;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonObject getSearchConfig() {
    if (searchConfig == null) {
      searchConfig = CoveoUtils.getSearchConfig(
          this.searchConfigService,
          this.request,
          this.drupalService,
          this.userService);
    }
    return searchConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String> getFeatureEvent() {
    if (StringUtils.isEmpty(this.featuredEvent)) {
      return new HashMap<>();
    }

    ResourceResolver resourceResolver = this.request.getResourceResolver();
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    Page pageObject = requireNonNull(pageManager).getPage(this.featuredEvent);
    if (pageObject == null) {
      log.error("Feature Event Page is not found");
      return new HashMap<>();
    }

    GregorianCalendar startTime = (GregorianCalendar) pageObject
        .getProperties()
        .get("eventStartDate");
    GregorianCalendar endTime = (GregorianCalendar) pageObject
        .getProperties()
        .get("eventEndDate");
    SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy");
    fmt.setCalendar(startTime);
    fmt.setCalendar(endTime);

    String imagePath = "";
    String registerPath = "";
    String registerTitle = "";
    if (!StringUtils.isEmpty(featuredEvent)) {
      String featureImage = featuredEvent + EVENT_PATH_ROOT + "eventdetailscontainer/image";
      Resource image = resourceResolver.getResource(featureImage);
      if (image != null) {
        try {
          imagePath =
              requireNonNull(image.adaptTo(Node.class)).getProperty("fileReference").getValue()
                  .getString();
        } catch (RepositoryException e) {
          log.error("There is no image for the selected feature event");
          imagePath = "";
        }
      }

      String registerButtonPath = featuredEvent + EVENT_PATH_ROOT + "eventregistration/button";
      Resource registerButton = resourceResolver.getResource(registerButtonPath);
      if (registerButton != null) {
        try {
          registerTitle =
              requireNonNull(registerButton.adaptTo(Node.class)).getProperty(JCR_TITLE).getString();
          Property link = requireNonNull(registerButton.adaptTo(Node.class)).getProperty("linkURL");
          registerPath = link == null ? "" : link.getString();
        } catch (RepositoryException ex) {
          log.error("Exception happens when try to access feature event information {}",
              ex.getMessage());
        }
      }
    }

    if (!StringUtils.isEmpty(registerPath) && !registerPath.endsWith(".html")) {
      registerPath += ".html";
    }

    String featurePage = this.featuredEvent;
    if (!StringUtils.isEmpty(featurePage) && !featurePage.endsWith(".html")) {
      featurePage += ".html";
    }

    String eventLocation = (String) pageObject.getProperties().get("eventLocation");
    String eventHost = (String) pageObject.getProperties().get("eventHost");

    Map<String, String> ret = new HashMap<>();
    ret.put("title", pageObject.getTitle());
    ret.put("link", featurePage);
    ret.put("startDate", fmt.format(startTime.getTime()));
    ret.put("endDate", fmt.format(endTime.getTime()));
    ret.put("image", imagePath);
    ret.put("location", eventLocation);
    ret.put("host", eventHost);
    ret.put("registerTitle", registerTitle);
    ret.put("registerLink", registerPath);

    return ret;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSortCriteria() {
    return this.getModelConfig().get("sortCriteria").getAsString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getEventCriteria() {
    LocalDate localDate = LocalDate.now();
    ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.of("Z"));
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd@HH:mm");
    String todayStr = startOfDay.format(fmt);
    String criteria = this.getModelConfig().get("eventCriteria").getAsString();
    String retCriteria = String.format(criteria, todayStr);
    if (!retCriteria.endsWith(")")) {
      retCriteria = retCriteria + ")";
    }

    if (!retCriteria.startsWith("(")) {
      retCriteria = "(" + retCriteria;
    }

    return retCriteria;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAllEventsUrl() {
    return this.getModelConfig().get("allEventsUrl").getAsString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getExtraCriteria() {
    return this.getModelConfig().get("extraCriteria").getAsString();
  }

  private JsonObject getModelConfig() {
    if (this.modelConfig == null) {
      try {
        this.modelConfig =
            DamUtils.readJsonFromDam(this.request.getResourceResolver(), MODEL_CONFIG_FILE);
      } catch (DamException e) {
        log.error("getModelConfig() call fails: {}", e.getMessage());
        return new JsonObject();
      }
    }
    return this.modelConfig;
  }
}
