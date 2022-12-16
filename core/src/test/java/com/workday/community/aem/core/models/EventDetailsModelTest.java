package com.workday.community.aem.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(AemContextExtension.class)
public class EventDetailsModelTest {

    private final AemContext context = new AemContext();

    private EventDetailsModel eventDetailsModel;

    private Page currentPage;
    private Resource resource;
    private TagManager tm;
    private Tag tag;

	private ResourceResolver resolver;

    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(EventDetailsModel.class);
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("startDate", "2022-11-22T00:44:02.000+05:30");
        pageProperties.put("endDate", "2022-11-24T00:44:02.000+05:30");
        pageProperties.put("eventLocation", "California");
        pageProperties.put("eventHost", "workday");
        pageProperties.put("eventFormat", new String[] { "event:event-format/webinar" });
        currentPage = context.create().page("/content/community/event",
                "/conf/community/settings/wcm/templates/event-page-template", pageProperties);
        resource = context.create().resource(currentPage, "eventspage",
                "sling:resourceType", "community/components/eventspage");
        currentPage = context.currentResource("/content/community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
    }

    @Test
    void testGetTimeFormat() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals("00:44", eventDetailsModel.getTimeFormat());
    }

    @Test
    void testGetLength() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals(2, eventDetailsModel.getLength());
    }

    @Test
    void testGetDateFormat() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals("Tuesday, Nov 22, 2022", eventDetailsModel.getDateFormat());
    }

    @Test
    void testGetEventLocation() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals("California", eventDetailsModel.getEventLocation());
    }

    @Test
    void testGetEventHost() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals("workday", eventDetailsModel.getEventHost());
    }

    @Test
    void testIsConfigured() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertTrue(eventDetailsModel.isConfigured());
    }

    @Test
    void testGetDaysLabel() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals("Days", eventDetailsModel.getDaysLabel());
    }

    @Test
    void testGetEventFormatWithoutTags() throws Exception {
        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        assertEquals(new ArrayList<String>(), eventDetailsModel.getEventFormat());
    }

    @Test
    void testGetEventFormatWithTags() throws Exception {
        resolver = Mockito.mock(ResourceResolver.class);
        tm = Mockito.mock(TagManager.class);
        when(resolver.adaptTo(TagManager.class)).thenReturn(tm);
        tag = Mockito.mock(Tag.class);
        when(tm.resolve("event:event-format/webinar")).thenReturn(tag);
        when(tag.getTitle()).thenReturn("Webinar");

        eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
        assertNotNull(eventDetailsModel);
        eventDetailsModel.getEventFormat();
    }
}
