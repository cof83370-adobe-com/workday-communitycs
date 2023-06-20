package com.workday.community.aem.core.models.impl;

import com.adobe.xfa.ut.StringUtils;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoEventFeedModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static java.util.Objects.*;

/**
 * The CoveoEventFeedModel implementation Class.
 */
@Model(
    adaptables = {
        Resource.class,
        SlingHttpServletRequest.class
    },
    adapters = { CoveoEventFeedModel.class },
    resourceType = { CoveoEventFeedModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoEventFeedModelImpl implements CoveoEventFeedModel {
  private static final Logger LOGGER = LoggerFactory.getLogger(CoveoEventFeedModelImpl.class);
  protected static final String RESOURCE_TYPE = "/content/workday-community/components/common/coveoeventfeed";
  private static final String MODEL_CONFIG_FILE = "/content/dam/workday-community/resources/event-feed-criteria.json";
  private static final String EVENT_PATH_ROOT = "/jcr:content/root/container/";

  private JsonObject modelConfig;

  @Self
  private SlingHttpServletRequest request;

  @ValueMapValue
  private String featuredEvent;

  @OSGiService
  private SearchApiConfigService searchConfigService;

  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      this.request = request;
    }
  }

  @Override
  public JsonObject getSearchConfig() {
    JsonObject config = new JsonObject();
    config.addProperty("orgId", searchConfigService.getOrgId());
    config.addProperty("searchHub", searchConfigService.getSearchHub());
    config.addProperty("analytics", true);

    return config;
  }

  @Override
  public Map<String, String> getFeatureEvent() throws RepositoryException {
    if (StringUtils.isEmpty(this.featuredEvent)) {
      return new HashMap<>();
    }

    ResourceResolver resourceResolver = this.request.getResourceResolver();
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    Page pageObject = requireNonNull(pageManager).getPage(this.featuredEvent);
    if (pageObject == null) {
      LOGGER.error("Feature Event Page is not found");
      return new HashMap<>();
    }

    GregorianCalendar startTime = (GregorianCalendar)pageObject.getProperties().get("eventStartDate");
    GregorianCalendar endTime = (GregorianCalendar)pageObject.getProperties().get("eventEndDate");
    SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy");
    fmt.setCalendar(startTime);
    fmt.setCalendar(endTime);
    String eventLocation = (String)pageObject.getProperties().get("eventLocation");
    String eventHost = (String)pageObject.getProperties().get("eventHost");

    String imagePath = "";
    String registerPath = "";
    String registerTitle = "";
    if (!StringUtils.isEmpty(featuredEvent)) {

      // Feature image
      String featureImage = featuredEvent + EVENT_PATH_ROOT + "eventdetailscontainer/image";
      Resource image = resourceResolver.getResource(featureImage);
      if (image != null) {
        imagePath = requireNonNull(image.adaptTo(Node.class)).getProperty("fileReference").getValue().getString();
      }

      // Register button
      String registerButtonPath = featuredEvent + EVENT_PATH_ROOT + "eventregistration/button";
      Resource registerButton = resourceResolver.getResource(registerButtonPath);
      if (registerButton != null) {
        try {
          registerTitle = requireNonNull(registerButton.adaptTo(Node.class)).getProperty(JCR_TITLE).getString();
          Property link = requireNonNull(registerButton.adaptTo(Node.class)).getProperty("linkURL");
          registerPath = link == null ? "" : link.getString();
        } catch (RepositoryException ex) {
          LOGGER.error("Exception happens when try to access feature event information {}", ex.getMessage());
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

  @Override
  public String getSortCriteria() throws DamException{
    return this.getModelConfig().get("sortCriteria").getAsString();
  }

  @Override
  public String getEventCriteria() throws DamException{
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

  @Override
  public String getAllEventsUrl() throws DamException {
    return this.getModelConfig().get("allEventsUrl").getAsString();
  }

  @Override
  public String getExtraCriteria() throws DamException {
    return this.getModelConfig().get("extraCriteria").getAsString();
  }

  private JsonObject getModelConfig() throws DamException {
    if (this.modelConfig == null) {
      this.modelConfig = DamUtils.readJsonFromDam(this.request.getResourceResolver(), MODEL_CONFIG_FILE);
    }
    return this.modelConfig;
  }
}
