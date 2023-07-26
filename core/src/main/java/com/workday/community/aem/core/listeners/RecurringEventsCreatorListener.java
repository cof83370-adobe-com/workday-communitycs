package com.workday.community.aem.core.listeners;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.utils.ResolverUtil;

/**
 * The listener interface for receiving recurringEventsCreator events.
 * The class that is interested in processing a recurringEventsCreator
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addRecurringEventsCreatorListener<code> method. When
 * the recurringEventsCreator event occurs, that object's appropriate
 * method is invoked.
 *
 * @see RecurringEventsCreatorEvent
 */
@Component(service = ResourceChangeListener.class, immediate = true, property = {
        ResourceChangeListener.PATHS + "=" + GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH,
        ResourceChangeListener.CHANGES + "=" + "ADDED",
})

@ServiceDescription("RecurringEventsCreatorListener")
public class RecurringEventsCreatorListener implements ResourceChangeListener {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(RecurringEventsCreatorListener.class);

    /** The Constant REGION_COUNTRY_TAGS. */
    private static final String REGION_COUNTRY_TAGS = "regionCountryTags";

    /** The Constant USER_TAGS. */
    private static final String USER_TAGS = "userTags";

    /** The Constant PROGRAMS_TOOLS_TAGS. */
    private static final String PROGRAMS_TOOLS_TAGS = "programsToolsTags";

    /** The Constant INDUSTRY_TAGS. */
    private static final String INDUSTRY_TAGS = "industryTags";

    /** The Constant USING_WORKDAY_TAGS. */
    private static final String USING_WORKDAY_TAGS = "usingWorkdayTags";

    /** The Constant PRODUCT_TAGS. */
    private static final String PRODUCT_TAGS = "productTags";

    /** The Constant RELEASE_TAGS. */
    private static final String RELEASE_TAGS = "releaseTags";

    /** The Constant EVENT_AUDIENCE. */
    private static final String EVENT_AUDIENCE = "eventAudience";

    /** The Constant EVENT_FORMAT. */
    private static final String EVENT_FORMAT = "eventFormat";

    /** The Constant ACCESS_CONTROL_TAGS. */
    private static final String ACCESS_CONTROL_TAGS = "accessControlTags";

    /** The Constant PROP_EVENT_LOCATION. */
    private static final String PROP_EVENT_LOCATION = "eventLocation";

    /** The Constant PROP_EVENT_HOST. */
    private static final String PROP_EVENT_HOST = "eventHost";

    /** The Constant PROP_ALTERNATE_TIMEZONE. */
    private static final String PROP_ALTERNATE_TIMEZONE = "alternateTimezone";

    /** The Constant PROP_UPDATED_DATE. */
    private static final String PROP_UPDATED_DATE = "updatedDate";

    /** The Constant PROP_RETIREMENT_DATE. */
    private static final String PROP_RETIREMENT_DATE = "retirementDate";

    /** The Constant PROP_AUTHOR. */
    private static final String PROP_AUTHOR = "author";

    /** The Constant PROP_CONTENT_TYPE. */
    private static final String PROP_CONTENT_TYPE = "contentType";

    /** The Constant PROP_EVENT_END_DATE. */
    private static final String PROP_EVENT_END_DATE = "eventEndDate";

    /** The Constant PROP_EVENT_START_DATE. */
    private static final String PROP_EVENT_START_DATE = "eventStartDate";

    /** The Constant EVENT_TEMPLATE_PATH. */
    static final String EVENT_TEMPLATE_PATH = "/conf/workday-community/settings/wcm/templates/events";

    /** The Constant EVENT_FREQUENCY_MONTHLY. */
    static final String EVENT_FREQUENCY_MONTHLY = "monthly";

    /** The Constant PROP_RECURRING_EVENTS. */
    static final String PROP_RECURRING_EVENTS = "recurringEvents";

    /** The Constant PROP_EVENT_FREQUENCY. */
    static final String PROP_EVENT_FREQUENCY = "eventFrequency";

    /** The resource resolver factory. */
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /**
     * On change.
     *
     * @param changes the changes
     */
    @Override
    public void onChange(List<ResourceChange> changes) {
        logger.info("Entered into RecurringEventPageListener");
        changes.stream()
                .filter(item -> "ADDED".equals(item.getType().toString())
                        && item.getPath().endsWith(GlobalConstants.JCR_CONTENT_PATH))
                .forEach(change -> generateRecurringEventPages(change.getPath()));
    }

    /**
     * Generate recurring event pages.
     *
     * @param eventPagePath the event page path
     */
    public void generateRecurringEventPages(String eventPagePath) {
        logger.info("Entered into generateRecurringEventPages method of RecurringEventsCreatorListener:{}",
                eventPagePath);
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory,
                "workday-community-administrative-service")) {
            Node eventNode = resourceResolver.getResource(eventPagePath).adaptTo(Node.class);
            if (eventNode != null && eventNode.hasProperty(PROP_RECURRING_EVENTS)
                    && eventNode.getProperty(PROP_RECURRING_EVENTS).getString()
                            .equalsIgnoreCase("true")) {
                String title = eventNode.getProperty("jcr:title").getString();
                ValueMap map = resourceResolver.getResource(eventPagePath).adaptTo(ValueMap.class);
                String eventFrequency = eventNode.getProperty(PROP_EVENT_FREQUENCY).getString();
                logger.debug("Creation of recurring events selected and frequency:{}", eventPagePath);
                Calendar startDate = eventNode.getProperty(PROP_EVENT_START_DATE).getDate();
                EventPeriodEnum period = eventFrequency.equalsIgnoreCase(EVENT_FREQUENCY_MONTHLY)
                        ? EventPeriodEnum.MONTHLY
                        : EventPeriodEnum.BI_WEEKLY;
                List<LocalDate> eventDatesList = sequentialEventsCalculator(period, startDate);
                logger.debug("Sequentials event dates list:{}", eventDatesList);
                if (null != eventDatesList && eventDatesList.size() > 1) {
                    createEventPage(resourceResolver, eventDatesList,
                            eventPagePath.replace(GlobalConstants.JCR_CONTENT_PATH, ""), title,
                            map);
                }
            }
        } catch (Exception exec) {
            logger.error("Exception occurred when generateRecurringEventPages method {} ", exec.getMessage());
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
                logger.debug("Pages to be created under:{}", eventsRootPath);
            }
            String fullPageName = "";
            for (LocalDate date : eventDatesList) {
                fullPageName = String.format("%s-%s", pageName, date.toString());
                logger.debug("Page to be created in this iteration:{}", fullPageName);
                Page newPage = pm.create(eventsRootPath, fullPageName, EVENT_TEMPLATE_PATH, title, true);
                addRequiredPageProps(resourceResolver, newPage, map);
            }
        } catch (Exception exec) {
            logger.error("Exception occurred when createEventPage method {} ", exec.getMessage());
        }
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
            datesList.add(startDate);
            startDate = period.equals(EventPeriodEnum.MONTHLY) ? startDate.plusMonths(1)
                    : startDate.plus(2, ChronoUnit.WEEKS);
        }
        return datesList;
    }

    /**
     * Adds the required page props.
     *
     * @param resourceResolver the resource resolver
     * @param newPage          the new page
     * @param valueMap         the value map
     */
    private void addRequiredPageProps(ResourceResolver resourceResolver, Page newPage, ValueMap valueMap) {
        try {

            Node node = resourceResolver.resolve(newPage.getPath() + GlobalConstants.JCR_CONTENT_PATH)
                    .adaptTo(Node.class);
            addAllTagsData(node, valueMap);
            addAllMetaData(node, valueMap);
            node.getSession().save();
        } catch (Exception exec) {
            logger.error("Exception occurred in addRequiredPageProps method {} ", exec.getMessage());
        }
    }

    /**
     * Adds the all meta data.
     *
     * @param node     the node
     * @param valueMap the value map
     * @throws RepositoryException the repository exception
     */
    private void addAllMetaData(Node node, ValueMap valueMap) throws RepositoryException {
        String author = valueMap.get(PROP_AUTHOR, String.class);
        if (StringUtils.isNotBlank(author))
            node.setProperty(PROP_AUTHOR, author);

        String[] contentType = valueMap.get(PROP_CONTENT_TYPE, String[].class);
        if (doNullCheckForStringArray(contentType))
            node.setProperty(PROP_CONTENT_TYPE, contentType);

        Calendar eventEndDate = valueMap.get(PROP_EVENT_END_DATE, Calendar.class);
        if (null != eventEndDate)
            node.setProperty(PROP_EVENT_END_DATE, eventEndDate);

        Calendar eventStartDate = valueMap.get(PROP_EVENT_START_DATE, Calendar.class);
        if (null != eventStartDate)
            node.setProperty(PROP_EVENT_START_DATE, eventStartDate);

        Calendar retirementDate = valueMap.get(PROP_RETIREMENT_DATE, Calendar.class);
        if (null != retirementDate)
            node.setProperty(PROP_RETIREMENT_DATE, retirementDate);

        Calendar updatedDate = valueMap.get(PROP_UPDATED_DATE, Calendar.class);
        if (null != updatedDate)
            node.setProperty(PROP_UPDATED_DATE, updatedDate);

        String alternateTimezone = valueMap.get(PROP_ALTERNATE_TIMEZONE, String.class);
        if (StringUtils.isNotBlank(alternateTimezone))
            node.setProperty(PROP_ALTERNATE_TIMEZONE, alternateTimezone);

        String eventHost = valueMap.get(PROP_EVENT_HOST, String.class);
        if (StringUtils.isNotBlank(eventHost))
            node.setProperty(PROP_EVENT_HOST, eventHost);

        String eventLocation = valueMap.get(PROP_EVENT_LOCATION, String.class);
        if (StringUtils.isNotBlank(eventLocation))
            node.setProperty(PROP_EVENT_LOCATION, eventLocation);
    }

    /**
     * Adds the all tags data.
     *
     * @param node     the node
     * @param valueMap the value map
     * @throws RepositoryException the repository exception
     */
    private void addAllTagsData(Node node, ValueMap valueMap) throws RepositoryException {
        String[] aclTags = valueMap.get(ACCESS_CONTROL_TAGS, String[].class);
        if (doNullCheckForStringArray(aclTags))
            node.setProperty(ACCESS_CONTROL_TAGS, aclTags);

        String[] eventFormat = valueMap.get(EVENT_FORMAT, String[].class);
        if (doNullCheckForStringArray(eventFormat))
            node.setProperty(EVENT_FORMAT, eventFormat);

        String[] eventAudience = valueMap.get(EVENT_AUDIENCE, String[].class);
        if (doNullCheckForStringArray(eventAudience))
            node.setProperty(EVENT_AUDIENCE, eventAudience);

        String[] releaseTags = valueMap.get(RELEASE_TAGS, String[].class);
        if (doNullCheckForStringArray(releaseTags))
            node.setProperty(RELEASE_TAGS, releaseTags);

        String[] productTags = valueMap.get(PRODUCT_TAGS, String[].class);
        if (doNullCheckForStringArray(productTags))
            node.setProperty(PRODUCT_TAGS, productTags);

        String[] usingWorkdayTags = valueMap.get(USING_WORKDAY_TAGS, String[].class);
        if (doNullCheckForStringArray(usingWorkdayTags))
            node.setProperty(USING_WORKDAY_TAGS, usingWorkdayTags);

        String[] industryTags = valueMap.get(INDUSTRY_TAGS, String[].class);
        if (doNullCheckForStringArray(industryTags))
            node.setProperty(INDUSTRY_TAGS, industryTags);

        String[] programsToolsTags = valueMap.get(PROGRAMS_TOOLS_TAGS, String[].class);
        if (doNullCheckForStringArray(programsToolsTags))
            node.setProperty(PROGRAMS_TOOLS_TAGS, programsToolsTags);

        String[] userTags = valueMap.get(USER_TAGS, String[].class);
        if (doNullCheckForStringArray(userTags))
            node.setProperty(USER_TAGS, userTags);

        String[] regionCountryTags = valueMap.get(REGION_COUNTRY_TAGS, String[].class);
        if (doNullCheckForStringArray(regionCountryTags))
            node.setProperty(REGION_COUNTRY_TAGS, regionCountryTags);
    }

    /**
     * Do null check for string array.
     *
     * @param inputArray the input array
     * @return true, if successful
     */
    private boolean doNullCheckForStringArray(String[] inputArray) {
        if (null != inputArray && inputArray.length > 0) {
            return true;
        }
        return false;
    }

    /**
     * The Enum EventPeriodEnum.
     */
    public enum EventPeriodEnum {

        /** The monthly. */
        MONTHLY,

        /** The bi weekly. */
        BI_WEEKLY
    }
}
