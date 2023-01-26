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

@ExtendWith(AemContextExtension.class)
public class EventLengthTest {

    private final AemContext context = new AemContext();

    private EventDetailsModel eventDetailsModel;

    private Page currentPage;
    private Resource resource;

    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(EventDetailsModel.class);
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("startDate", "2022-11-22T00:44:02.000+05:30");
        pageProperties.put("endDate", "2022-11-22T05:54:02.000+05:30");
        currentPage = context.create().page("/content/community/event",
                "/conf/community/settings/wcm/templates/event-page-template", pageProperties);
        resource = context.create().resource(currentPage, "eventslengthpage",
                "sling:resourceType", "community/components/eventspage");
        currentPage = context.currentResource("/content/community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
    }

    @Test
    void testEventLengthLessThan8Hours() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals(0, eventDetailsModel.getEventLengthDays());
        assertEquals(5, eventDetailsModel.getEventLengthHours());
        assertEquals(10, eventDetailsModel.getEventLengthMinutes());
    }
}
