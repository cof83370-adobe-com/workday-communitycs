package com.workday.community.aem.core.listeners;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.WorkflowConstants;
import com.workday.community.aem.core.constants.enums.EventPeriodEnum;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

/**
 * The listener interface for receiving recurringEventsCreator events.
 * The class that is interested in processing a recurringEventsCreator
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addRecurringEventsCreatorListener</code> method. When
 * the recurringEventsCreator event occurs, that object's appropriate
 * method is invoked.
 */
@Slf4j
@Component(service = ResourceChangeListener.class, immediate = true, property = {
    ResourceChangeListener.PATHS + "=" + GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH,
    ResourceChangeListener.CHANGES + "=" + "ADDED",
})
@ServiceDescription("RecurringEventsCreatorListener")
public class RecurringEventsCreatorListener implements ResourceChangeListener {

  /**
   * The Constant EVENT_FREQUENCY_MONTHLY.
   */
  private static final String EVENT_FREQUENCY_MONTHLY = "monthly";

  /**
   * The Constant PROP_RECURRING_EVENTS.
   */
  private static final String PROP_RECURRING_EVENTS = "recurringEvents";

  /**
   * The Constant PROP_EVENT_FREQUENCY.
   */
  private static final String PROP_EVENT_FREQUENCY = "eventFrequency";

  /**
   * The Constant REGION_COUNTRY_TAGS.
   */
  private static final String REGION_COUNTRY_TAGS = "regionCountryTags";

  /**
   * The Constant USER_TAGS.
   */
  private static final String USER_TAGS = "userTags";

  /**
   * The Constant PROGRAMS_TOOLS_TAGS.
   */
  private static final String PROGRAMS_TOOLS_TAGS = "programsToolsTags";

  /**
   * The Constant INDUSTRY_TAGS.
   */
  private static final String INDUSTRY_TAGS = "industryTags";

  /**
   * The Constant USING_WORKDAY_TAGS.
   */
  private static final String USING_WORKDAY_TAGS = "usingWorkdayTags";

  /**
   * The Constant PRODUCT_TAGS.
   */
  private static final String PRODUCT_TAGS = "productTags";

  /**
   * The Constant RELEASE_TAGS.
   */
  private static final String RELEASE_TAGS = "releaseTags";

  /**
   * The Constant EVENT_AUDIENCE.
   */
  private static final String EVENT_AUDIENCE = "eventAudience";

  /**
   * The Constant EVENT_FORMAT.
   */
  private static final String EVENT_FORMAT = "eventFormat";

  /**
   * The Constant PROP_EVENT_LOCATION.
   */
  private static final String PROP_EVENT_LOCATION = "eventLocation";

  /**
   * The Constant PROP_EVENT_HOST.
   */
  private static final String PROP_EVENT_HOST = "eventHost";

  /**
   * The Constant PROP_ALTERNATE_TIMEZONE.
   */
  private static final String PROP_ALTERNATE_TIMEZONE = "alternateTimezone";

  /**
   * The Constant PROP_CONTENT_TYPE.
   */
  private static final String PROP_CONTENT_TYPE = "contentType";

  /**
   * The Constant PROP_EVENT_END_DATE.
   */
  private static final String PROP_EVENT_END_DATE = "eventEndDate";

  /**
   * The Constant PROP_EVENT_START_DATE.
   */
  private static final String PROP_EVENT_START_DATE = "eventStartDate";

  /**
   * The cache manager.
   */
  @Reference
  private CacheManagerService cacheManager;

  /**
   * {@inheritDoc}
   */
  @Override
  public void onChange(List<ResourceChange> changes) {
    log.debug("Entered into RecurringEventPageListener");
    changes.stream()
        .filter(item -> item.getType().equals(ResourceChange.ChangeType.ADDED)
            && item.getPath().endsWith(GlobalConstants.JCR_CONTENT_PATH))
        .forEach(change -> generateRecurringEventPages(change.getPath()));
  }

  /**
   * Generate recurring event pages.
   *
   * @param resourcePath the event page path
   */
  public void generateRecurringEventPages(String resourcePath) {
    log.debug("Entered into generateRecurringEventPages method:{}", resourcePath);
    try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      Resource addedResource = resourceResolver.getResource(resourcePath);
      if (null != addedResource && null != addedResource.adaptTo(Node.class)) {
        Node eventNode = addedResource.adaptTo(Node.class);
        if (eventNode != null && eventNode.hasProperty(PROP_RECURRING_EVENTS)
            && eventNode.getProperty(PROP_RECURRING_EVENTS).getString()
            .equalsIgnoreCase("true")) {
          log.debug("Creation of recurring events selected and frequency:{}", resourcePath);
          Calendar startDate = eventNode.getProperty(PROP_EVENT_START_DATE).getDate();
          String eventFrequency = eventNode.getProperty(PROP_EVENT_FREQUENCY).getString();
          EventPeriodEnum period = eventFrequency.equalsIgnoreCase(EVENT_FREQUENCY_MONTHLY)
              ? EventPeriodEnum.MONTHLY
              : EventPeriodEnum.BI_WEEKLY;
          List<LocalDate> eventDatesList = sequentialEventsCalculator(period, startDate);
          log.debug("Sequentials event dates list:{}", eventDatesList);
          if (null != eventDatesList && eventDatesList.size() > 1) {
            String title = eventNode.getProperty(JcrConstants.JCR_TITLE).getString();
            ValueMap map = addedResource.adaptTo(ValueMap.class);
            String eventsRoot = resourcePath.replace(GlobalConstants.JCR_CONTENT_PATH, "");
            createEventPage(resourceResolver, eventDatesList, eventsRoot, title, map);
          }
        }
      }
    } catch (CacheException | RepositoryException exec) {
      log.error("Exception occurred when generateRecurringEventPages method {} ",
          exec.getMessage());
    }
  }

  /**
   * Creates the event page.
   *
   * @param resourceResolver the resource resolver
   * @param eventDatesList   the event dates list
   * @param eventsRootPath   the events root path
   * @param title            the title
   * @param map              the map
   */
  private void createEventPage(ResourceResolver resourceResolver, List<LocalDate> eventDatesList,
                               String eventsRootPath, final String title, ValueMap map) {
    try {
      PageManager pm = resourceResolver.adaptTo(PageManager.class);
      String[] pathElements = eventsRootPath.split("/");
      String pageName = pathElements[pathElements.length - 1];
      int lastIndex = eventsRootPath.lastIndexOf("/");
      if (lastIndex != -1) {
        eventsRootPath = eventsRootPath.substring(0, lastIndex);
        log.debug("Pages to be created under:{}", eventsRootPath);
      }
      String fullPageName;
      int eventLength = getEventLength(map);
      for (LocalDate date : eventDatesList) {
        fullPageName = String.format("%s-%s", pageName, date.toString());
        log.debug("Page to be created in this iteration:{}", fullPageName);
        Page newPage = pm.create(eventsRootPath, fullPageName,
            WorkflowConstants.EVENT_TEMPLATE_PATH, title, true);
        addRequiredPageProps(resourceResolver, newPage, map, date, eventLength);
      }
    } catch (WCMException exec) {
      log.error("Exception occurred when createEventPage method {} ", exec.getMessage());
    }
  }

  /**
   * Gets the event length.
   *
   * @param valueMap the value map
   * @return the event length
   */
  private int getEventLength(ValueMap valueMap) {
    Calendar eventStartDateCalInstance = valueMap.get(PROP_EVENT_START_DATE, Calendar.class);
    Calendar eventEndDateCalInstance = valueMap.get(PROP_EVENT_END_DATE, Calendar.class);
    if (null != eventStartDateCalInstance && null != eventEndDateCalInstance) {
      int result = eventEndDateCalInstance.compareTo(eventStartDateCalInstance);
      if (result > 0) {
        long differenceInMilliseconds = Math
            .abs(eventEndDateCalInstance.getTimeInMillis()
                - eventStartDateCalInstance.getTimeInMillis());
        long differenceInDays = differenceInMilliseconds / (24 * 60 * 60 * 1000);
        return (int) differenceInDays;
      }
    }
    return 0;
  }

  /**
   * Sequential events calculator.
   *
   * @param period   the period
   * @param calendar the calendar
   * @return the list
   */
  public List<LocalDate> sequentialEventsCalculator(EventPeriodEnum period, Calendar calendar) {
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-based in Calendar
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    LocalDate startDate = LocalDate.of(year, month, day);

    // Calculate end date (1 year from start date)
    LocalDate endDate = startDate.plusYears(1);

    List<LocalDate> datesList = new ArrayList<>();
    while (startDate.isBefore(endDate)) {
      startDate = period.equals(EventPeriodEnum.MONTHLY) ? startDate.plusMonths(1)
          : startDate.plusWeeks(2);
      datesList.add(startDate);
    }
    return datesList;
  }

  /**
   * Adds the required page props.
   *
   * @param resourceResolver the resource resolver
   * @param newPage          the new page
   * @param valueMap         the value map
   * @param date             the date
   * @param eventLength      the event length
   */
  private void addRequiredPageProps(ResourceResolver resourceResolver, Page newPage,
                                    ValueMap valueMap,
                                    LocalDate date, int eventLength) {
    try {
      Node node = resourceResolver.resolve(newPage.getPath() + GlobalConstants.JCR_CONTENT_PATH)
          .adaptTo(Node.class);
      addAllTagsAndMetaData(node, valueMap, date, eventLength);
      node.getSession().save();
    } catch (RepositoryException exec) {
      log.error("Exception occurred in addRequiredPageProps method {} ", exec.getMessage());
    }
  }

  /**
   * Adds the all tags and metadata.
   *
   * @param node        the node
   * @param valueMap    the value map
   * @param localDate   the local date
   * @param eventLength the event length
   * @throws RepositoryException the repository exception
   */
  private void addAllTagsAndMetaData(Node node, ValueMap valueMap, LocalDate localDate,
                                     int eventLength)
      throws RepositoryException {
    // All Tag type Props
    String[] tags = new String[] {GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, EVENT_FORMAT,
        EVENT_AUDIENCE, RELEASE_TAGS, PRODUCT_TAGS, USING_WORKDAY_TAGS, INDUSTRY_TAGS,
        PROGRAMS_TOOLS_TAGS, USER_TAGS, REGION_COUNTRY_TAGS, PROP_CONTENT_TYPE};
    for (String tag : tags) {
      String[] tagValue = valueMap.get(tag, String[].class);
      if (doNullCheckForStringArray(tagValue)) {
        node.setProperty(tag, tagValue);
      }
    }

    // All String type Props
    String[] props =
        new String[] {GlobalConstants.PROP_AUTHOR, PROP_ALTERNATE_TIMEZONE, PROP_EVENT_HOST, PROP_EVENT_LOCATION};
    for (String prop : props) {
      String propVal = valueMap.get(prop, String.class);
      if (StringUtils.isNotBlank(propVal)) {
        node.setProperty(prop, propVal);
      }
    }

    // Event Start Date from Calculated Value, below valuemap reading was required to maintain event
    // time from base page of recurring events.
    Calendar eventStartDateCalInstance = valueMap.get(PROP_EVENT_START_DATE, Calendar.class);
    eventStartDateCalInstance.set(localDate.getYear(), localDate.getMonthValue() - 1,
        localDate.getDayOfMonth());
    node.setProperty(PROP_EVENT_START_DATE, eventStartDateCalInstance);


    // Event End Date was calculated based on source page event gap(End date - Start Date)
    // Same gap will be maintained across all auto created pages.
    Calendar eventEndDateCalInstance = valueMap.get(PROP_EVENT_END_DATE, Calendar.class);
    eventEndDateCalInstance.set(localDate.getYear(), localDate.getMonthValue() - 1,
        localDate.getDayOfMonth());
    if (eventLength > 0) {
      eventEndDateCalInstance.add(Calendar.DAY_OF_MONTH, eventLength);
    }
    node.setProperty(PROP_EVENT_END_DATE, eventEndDateCalInstance);
  }

  /**
   * Do null check for string array.
   *
   * @param inputArray the input array
   * @return true, if successful
   */
  private boolean doNullCheckForStringArray(String[] inputArray) {
    return (ArrayUtils.isNotEmpty(inputArray) && inputArray.length > 0);
  }

}
