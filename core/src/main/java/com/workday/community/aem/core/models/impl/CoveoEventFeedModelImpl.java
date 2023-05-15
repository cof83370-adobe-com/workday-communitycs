package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoEventFeedModel;
import com.workday.community.aem.core.models.CoveoTabListModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
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
import javax.jcr.Session;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { CoveoTabListModel.class },
    resourceType = { CoveoEventFeedModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoEventFeedModelImpl implements CoveoEventFeedModel {
  private static final Logger logger = LoggerFactory.getLogger(CoveoEventFeedModelImpl.class);
  protected static final String RESOURCE_TYPE = "workday-community/components/common/coveoeventfeed";
  @Self
  private SlingHttpServletRequest request;

  @Inject
  private String[] eventType;

  @Inject
  private String featureEventPage = "/admin-tools/demo-event";

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
  public JsonObject getFeatureEvent() {
    PageManager pageManager = this.request.getResourceResolver().adaptTo(PageManager.class);
    Page pageObject = pageManager.getPage(this.featureEventPage);
    JsonObject ret = new JsonObject();
    ret.addProperty("title", pageObject.getTitle());
    ret.addProperty("link", this.featureEventPage);
    ret.addProperty("eventStartDate", "");
    ret.addProperty("eventEndDate", "");
    ret.addProperty("image", pageObject.getProperties("cq:featuredimage").get("fileName").toString());
    ret.addProperty("location", pageObject.getPath());
    ret.addProperty("registerTitle", "");
    ret.addProperty("registerLink", "");

    return null;
  }

  @Override
  public String getSortCriteria() {
    return "@com_content_event_start_date Ascending";
  }

  @Override
  public String getEventCriteria() {
    LocalDate localDate = LocalDate.now();
    ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.of("PST"));
    long today = startOfDay.toInstant().toEpochMilli();
    return String.format("(@commcontenttype='Calendar Event' OR @drucontenttype=calendar_event)(@commoneventstartdate>=%s", today);
  }



  @Override
  public String getExtraCriteria() {
    return "(NOT @druwdcworkflowworkflowstate==retired)";
  };
}
