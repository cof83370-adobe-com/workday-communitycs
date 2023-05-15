package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoEventFeedModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.LRUCacheWithTimeout;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { CoveoEventFeedModel.class },
    resourceType = { CoveoEventFeedModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoEventFeedModelImpl implements CoveoEventFeedModel {
  private static final Logger logger = LoggerFactory.getLogger(CoveoEventFeedModelImpl.class);
  protected static final String RESOURCE_TYPE = "/content/workday-community/components/common/coveoeventfeed";
  @Self
  private SlingHttpServletRequest request;

  @Inject
  String eventtype;

  @Inject
  private String featureEventPage = "/content/workday-community/en-us/admin-tools/demo-event";

  @OSGiService
  private SearchApiConfigService searchConfigService;

  private final LRUCacheWithTimeout<String, String> cache = new LRUCacheWithTimeout(100, 60 * 1000);

  @PostConstruct
  private void init() {
    logger.debug("initializing Event feed model");
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
  public Map<String, String> getFeatureEvent() {
    PageManager pageManager = this.request.getResourceResolver().adaptTo(PageManager.class);
    Page pageObject = pageManager.getPage(this.featureEventPage);

    GregorianCalendar startTime = (GregorianCalendar)pageObject.getProperties().get("startDate");
    GregorianCalendar endTime = (GregorianCalendar)pageObject.getProperties().get("endDate");
    SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy");
    fmt.setCalendar(startTime);
    fmt.setCalendar(endTime);
    String eventLocation = (String)pageObject.getProperties().get("eventLocation");
    String eventHost = (String)pageObject.getProperties().get("eventHost");

    Map<String, String> ret = new HashMap<>();
    ret.put("title", pageObject.getTitle());
    ret.put("link", this.featureEventPage);
    ret.put("startDate", fmt.format(startTime.getTime()));
    ret.put("endDate", fmt.format(endTime.getTime()));
    ret.put("image", "");
    ret.put("location", eventLocation);
    ret.put("host", eventHost);
    ret.put("registerTitle", "");
    ret.put("registerLink", "");

    return ret;
  }

  @Override
  public String getSortCriteria() {
    return "@com_content_event_start_date Ascending";
  }

  @Override
  public String getEventCriteria() {
    LocalDate localDate = LocalDate.now();
    ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.of("Z"));
    long today = startOfDay.toInstant().toEpochMilli();
    return "(@commcontenttype='Calendar Event' OR @drucontenttype=calendar_event)";

    //TODO change to the correct one once there are data.
    // return String.format("(@commcontenttype='Calendar Event' OR @drucontenttype=calendar_event)(@commoneventstartdate>=%s)", today);
  }

  @Override
  public String getExtraCriteria() {
    return "(NOT @druwdcworkflowworkflowstate==retired)";
  };
}
