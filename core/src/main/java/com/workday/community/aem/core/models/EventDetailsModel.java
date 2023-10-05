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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
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
@Model(adaptables = {Resource.class,
    SlingHttpServletRequest.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EventDetailsModel {

  /**
   * The logger.
   */
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  @Reference
  UserService userService;

  /**
   * The event format.
   */
  private List<String> eventFormat = new ArrayList<>();

  /**
   * The location.
   */
  private String eventLocation;

  /**
   * The host.
   */
  private String eventHost;

  /**
   * The length.
   */
  private long eventLengthDays;

  /**
   * The event length hours.
   */
  private long eventLengthHours;

  /**
   * The event length minutes.
   */
  private long eventLengthMinutes;

  /**
   * The days label.
   */
  private String daysLabel;

  /**
   * The hours label.
   */
  private String hoursLabel;

  /**
   * The minutes label.
   */
  private String minutesLabel;

  /**
   * The date format.
   */
  private String dateFormat;

  /**
   * The time format.
   */
  private String timeFormat;

  /**
   * The User TimeZone
   */
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
   * The Snap Service
   */
  @Inject
  private SnapService snapService;

  /**
   * The Sling Http Servlet Request
   */
  @Self
  private SlingHttpServletRequest request;

  /**
   * Inits the model.
   */
  @PostConstruct
  protected void init() {
    if (null != currentPage) {
      try {
        final ValueMap map = currentPage.getProperties();
        String startDateStr = map.get("eventStartDate", String.class);
        String endDateStr = map.get("eventEndDate", String.class);
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
   * Populates User TimeZone
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
    LocalDateTime endDateAndTime =
        formattedEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    long lengthInMinutes = ChronoUnit.MINUTES.between(startDateAndTime, endDateAndTime);
    if (lengthInMinutes > 0 && lengthInMinutes <= EventDetailsConstants.MINUTES_IN_8_HOURS) {
      eventLengthHours = lengthInMinutes / EventDetailsConstants.MINUTES_IN_1_HOUR;
      eventLengthMinutes = lengthInMinutes % EventDetailsConstants.MINUTES_IN_1_HOUR;
      hoursLabel = (eventLengthHours <= 1) ? EventDetailsConstants.HOUR_LABEL
          : EventDetailsConstants.HOURS_LABEL;

      minutesLabel = (eventLengthMinutes <= 1) ? EventDetailsConstants.MINUTE_LABEL
          : EventDetailsConstants.MINUTES_LABEL;
    } else {
      eventLengthDays = lengthInMinutes / EventDetailsConstants.MINUTES_IN_24_HOURS;
      if (lengthInMinutes % EventDetailsConstants.MINUTES_IN_24_HOURS > 0) {
        eventLengthDays++;
      }
      daysLabel = (eventLengthDays <= 1) ? EventDetailsConstants.DAY_LABEL
          : EventDetailsConstants.DAYS_LABEL;
    }
  }

  /**
   * Gets the days label.
   *
   * @return the days label
   */
  public String getDaysLabel() {
    return daysLabel;
  }

  /**
   * Gets the minutes label.
   *
   * @return the minutesLabel
   */
  public String getMinutesLabel() {
    return minutesLabel;
  }

  /**
   * Gets the hours label.
   *
   * @return the hoursLabel
   */
  public String getHoursLabel() {
    return hoursLabel;
  }

  /**
   * Gets the event length days.
   *
   * @return the eventLengthInDays
   */
  public long getEventLengthDays() {
    return eventLengthDays;
  }

  /**
   * Gets the event length hours.
   *
   * @return the eventLengthInHours
   */
  public long getEventLengthHours() {
    return eventLengthHours;
  }

  /**
   * Gets the event length minutes.
   *
   * @return the eventLengthInMinutes
   */
  public long getEventLengthMinutes() {
    return eventLengthMinutes;
  }

  /**
   * Checks if is configured.
   *
   * @return true, if is configured
   */
  public boolean isConfigured() {
    return eventFormat != null && eventLocation != null && eventHost != null;
  }

  /**
   * Gets the date format.
   *
   * @return the date format
   */
  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * Gets the time format.
   *
   * @return the time format
   */
  public String getTimeFormat() {
    return timeFormat;
  }

  /**
   * Gets the event format.
   *
   * @return the event format
   */
  public List<String> getEventFormat() {
    return Collections.unmodifiableList(eventFormat);
  }

  /**
   * Gets the event location.
   *
   * @return the event location
   */
  public String getEventLocation() {
    return eventLocation;
  }

  /**
   * Gets the event host.
   *
   * @return the event host
   */
  public String getEventHost() {
    return eventHost;
  }

  /**
   * @return the userTimeZone
   */
  public String getUserTimeZone() {
    return userTimeZone;
  }
}
