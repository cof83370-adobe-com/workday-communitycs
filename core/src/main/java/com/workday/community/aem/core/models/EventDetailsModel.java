package com.workday.community.aem.core.models;

import com.day.cq.wcm.api.Page;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.EventDetailsConstants;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EventDetailsModel.
 *
 * @author pepalla
 */
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class EventDetailsModel {

  /**
   * The logger.
   */
  private static final Logger logger = LoggerFactory.getLogger(EventDetailsModel.class);

  @Reference
  private UserService userService;

  /**
   * The event format.
   */
  @Getter
  private List<String> eventFormat = new ArrayList<>();

  /**
   * The location.
   */
  @Getter
  private String eventLocation;

  /**
   * The host.
   */
  @Getter
  private String eventHost;

  /**
   * The length.
   */
  @Getter
  private long eventLengthDays;

  /**
   * The event length hours.
   */
  @Getter
  private long eventLengthHours;

  /**
   * The event length minutes.
   */
  @Getter
  private long eventLengthMinutes;

  /**
   * The days label.
   */
  @Getter
  private String daysLabel;

  /**
   * The hours label.
   */
  @Getter
  private String hoursLabel;

  /**
   * The minutes label.
   */
  @Getter
  private String minutesLabel;

  /**
   * The date format.
   */
  @Getter
  private String dateFormat;

  /**
   * The time format.
   */
  @Getter
  private String timeFormat;

  /**
   * The User TimeZone.
   */
  @Getter
  private String userTimeZone;

  /**
   * The current page.
   */
  @Inject
  private Page currentPage;

  /**
   * The resolver.
   */
  @Inject
  private ResourceResolver resolver;

  /**
   * The Snap Service.
   */
  @Inject
  private SnapService snapService;

  /**
   * The Sling Http Servlet Request.
   */
  @Self
  private SlingHttpServletRequest request;

  /**
   * Checks if is configured.
   *
   * @return true, if is configured
   */
  public boolean isConfigured() {
    return eventFormat != null && eventLocation != null && eventHost != null;
  }

  /**
   * Inits the model.
   */
  @PostConstruct
  protected void init() {
    if (null != currentPage) {
      final ValueMap map = currentPage.getProperties();
      String startDateStr = map.get("eventStartDate", String.class);
      String endDateStr = map.get("eventEndDate", String.class);
      try {
        if (StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)) {
          calculateRequired(startDateStr, endDateStr);
        }
        eventLocation = map.get("eventLocation", String.class);
        eventHost = map.get("eventHost", String.class);
        eventFormat = CommunityUtils.getPageTagsList(map, "eventFormat", resolver);
        userTimeZone = populateUserTimeZone();
      } catch (ParseException exec) {
        logger.error("Exception occurred at init method of EventDetailsModel:{} ",
            exec.getMessage());
      }
    }
  }

  /**
   * Populates User TimeZone.
   *
   * @return user time zone string
   */
  private String populateUserTimeZone() {
    String sfId = OurmUtils.getSalesForceId(request, userService);
    String timeZoneStr = "";
    Gson gson = new Gson();
    if (StringUtils.isNotBlank(sfId) && null != snapService) {
      String profileData = snapService.getUserProfile(sfId);
      if (StringUtils.isNotBlank(profileData)) {
        JsonObject profileObject = gson.fromJson(profileData, JsonObject.class);
        JsonElement timeZoneElement = profileObject.get("timeZone");
        timeZoneStr = (timeZoneElement == null || timeZoneElement.isJsonNull()) ? "" :
            timeZoneElement.getAsString();
      }
    }
    return timeZoneStr;
  }

  /**
   * Calculate required.
   *
   * @param eventStartDate the event start date
   * @param eventEndDate   the event end date
   * @throws ParseException the parse exception
   */
  private void calculateRequired(final String eventStartDate, final String eventEndDate)
      throws ParseException {
    DateFormat formatter = new SimpleDateFormat(EventDetailsConstants.DATE_TIME_FORMAT);
    Date formattedStartDate = formatter.parse(eventStartDate);
    LocalDateTime startDateAndTime = formattedStartDate.toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime();

    dateFormat = DateTimeFormatter.ofPattern(EventDetailsConstants.REQ_DATE_FORMAT)
        .format(startDateAndTime);
    timeFormat = DateTimeFormatter.ofPattern(EventDetailsConstants.REQ_TIME_FORMAT)
        .format(startDateAndTime);

    Date formattedEndDate = formatter.parse(eventEndDate);
    LocalDateTime endDateAndTime = formattedEndDate
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    long lengthInMinutes = ChronoUnit.MINUTES.between(startDateAndTime, endDateAndTime);

    if (lengthInMinutes > 0 && lengthInMinutes <= EventDetailsConstants.MINUTES_IN_8_HOURS) {
      eventLengthHours = lengthInMinutes / EventDetailsConstants.MINUTES_IN_1_HOUR;
      hoursLabel = (eventLengthHours <= 1)
          ? EventDetailsConstants.HOUR_LABEL : EventDetailsConstants.HOURS_LABEL;

      eventLengthMinutes = lengthInMinutes % EventDetailsConstants.MINUTES_IN_1_HOUR;
      minutesLabel = (eventLengthMinutes <= 1) ? EventDetailsConstants.MINUTE_LABEL
          : EventDetailsConstants.MINUTES_LABEL;
    } else {
      eventLengthDays = lengthInMinutes / EventDetailsConstants.MINUTES_IN_24_HOURS;
      if (lengthInMinutes % EventDetailsConstants.MINUTES_IN_24_HOURS > 0) {
        eventLengthDays++;
      }
      daysLabel = (eventLengthDays <= 1)
          ? EventDetailsConstants.DAY_LABEL : EventDetailsConstants.DAYS_LABEL;
    }
  }

}
