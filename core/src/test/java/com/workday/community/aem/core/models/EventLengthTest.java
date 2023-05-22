package com.workday.community.aem.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class EventLengthTest.
 */
@ExtendWith(AemContextExtension.class)
public class EventLengthTest {

    /** The context. */
    private final AemContext context = new AemContext();

    /** The event details model. */
    private EventDetailsModel eventDetailsModel;

    /** The current page. */
    private Page currentPage;
    
    /** The resource. */
    private Resource resource;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(EventDetailsModel.class);
       
    }

    /**
     * Test event length less than 8 hours.
     *
     * @throws Exception the exception
     */
    @Test
    void testEventLengthLessThan8Hours() throws Exception {
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("eventStartDate", "2022-11-22T00:44:02.000+05:30");
        pageProperties.put("eventEndDate", "2022-11-22T05:54:02.000+05:30");
        currentPage = context.create().page("/content/workday-community/event",
                "/conf/workday-community/settings/wcm/templates/events", pageProperties);
        resource = context.create().resource(currentPage, "eventslengthpage",
                "sling:resourceType", "workday-community/components/structure/eventspage");
        currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals(0, eventDetailsModel.getEventLengthDays());
        assertEquals(5, eventDetailsModel.getEventLengthHours());
        assertEquals(10, eventDetailsModel.getEventLengthMinutes());
        assertEquals("Minutes", eventDetailsModel.getMinutesLabel());
        assertEquals("Hours", eventDetailsModel.getHoursLabel());
    }

    /**
     * Test event length less than one hour.
     *
     * @throws Exception the exception
     */
    @Test
    void testEventLengthLessThanOneHour() throws Exception {
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("eventStartDate", "2022-11-22T00:44:02.000+05:30");
        pageProperties.put("eventEndDate", "2022-11-22T00:59:02.000+05:30");
        currentPage = context.create().page("/content/workday-community/event",
                "/conf/workday-community/settings/wcm/templates/events", pageProperties);
        resource = context.create().resource(currentPage, "eventslengthpage",
                "sling:resourceType", "workday-community/components/structure/eventspage");
        currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals(0, eventDetailsModel.getEventLengthDays());
        assertEquals(0, eventDetailsModel.getEventLengthHours());
        assertEquals(15, eventDetailsModel.getEventLengthMinutes());
        assertEquals("Minutes", eventDetailsModel.getMinutesLabel());
        assertEquals("Hour", eventDetailsModel.getHoursLabel());
    }

    /**
     * Test event length one day and zero minutes.
     *
     * @throws Exception the exception
     */
    @Test
    void testEventLengthOneDayAndZeroMinutes() throws Exception {
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("eventStartDate", "2022-11-22T00:44:02.000+05:30");
        pageProperties.put("eventEndDate", "2022-11-23T00:44:02.000+05:30");
        currentPage = context.create().page("/content/workday-community/event",
                "/conf/workday-community/settings/wcm/templates/events", pageProperties);
        resource = context.create().resource(currentPage, "eventslengthpage",
                "sling:resourceType", "workday-community/components/structure/eventspage");
        currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals(1, eventDetailsModel.getEventLengthDays());
        assertEquals(0, eventDetailsModel.getEventLengthHours());
        assertEquals(0, eventDetailsModel.getEventLengthMinutes());
    }

    /**
     * Test event length more than day.
     *
     * @throws Exception the exception
     */
    @Test
    void testEventLengthMoreThanDay() throws Exception {
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("eventStartDate", "2022-11-22T00:44:02.000+05:30");
        pageProperties.put("eventEndDate", "2022-11-24T05:54:02.000+05:30");
        currentPage = context.create().page("/content/workday-community/event",
                "/conf/workday-community/settings/wcm/templates/events", pageProperties);
        resource = context.create().resource(currentPage, "eventslengthpage",
                "sling:resourceType", "workday-community/components/structure/eventspage");
        currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals(3, eventDetailsModel.getEventLengthDays());
        assertEquals("Days", eventDetailsModel.getDaysLabel());
    }

    /**
     * Test event length no strat and end dates.
     *
     * @throws Exception the exception
     */
    @Test
    void testEventLengthNoStratAndEndDates() throws Exception {
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("eventStartDate", "");
        pageProperties.put("eventEndDate", "");
        currentPage = context.create().page("/content/workday-community/event",
                "/conf/workday-community/settings/wcm/templates/events", pageProperties);
        resource = context.create().resource(currentPage, "eventslengthpage",
                "sling:resourceType", "workday-community/components/structure/eventspage");
        currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
    }
}
