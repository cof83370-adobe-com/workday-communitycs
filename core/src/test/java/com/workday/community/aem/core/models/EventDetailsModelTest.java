package com.workday.community.aem.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.services.SnapService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class EventDetailsModelTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EventDetailsModelTest {

  /**
   * The context.
   */
  private final AemContext context = new AemContext();

  /**
   * The event details model.
   */
  private EventDetailsModel eventDetailsModel;

  /**
   * The current page.
   */
  private Page currentPage;

  /**
   * The resource.
   */
  private Resource resource;

  /**
   * The Snap Service
   */
  @Mock
  private SnapService snapService;

  /**
   * The user's timezone to be returned by the mocked snap service.
   */
  private final String timeZone = "America/New_York";

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    context.addModelsForClasses(EventDetailsModel.class);
    Map<String, Object> pageProperties = new HashMap<>();
    pageProperties.put("eventStartDate", "2022-11-22T00:44:02.000+05:30");
    pageProperties.put("eventEndDate", "2022-11-24T00:54:02.000+05:30");
    pageProperties.put("eventLocation", "California");
    pageProperties.put("eventHost", "workday");
    pageProperties.put("eventFormat", new String[] {"event:event-format/webinar"});
    currentPage = context.create().page("/content/workday-community/event",
        "/conf/workday-community/settings/wcm/templates/events", pageProperties);
    resource = context.create().resource(currentPage, "eventspage",
        "sling:resourceType", "workday-community/components/structure/eventspage");
    currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    context.registerService(SnapService.class, snapService);

    String profileResponse = String.format("{\"timeZone\":\"%s\"}", this.timeZone);
    lenient().when(snapService.getUserProfile(anyString())).thenReturn(profileResponse);
  }

  /**
   * Tests the user's timezone getter.
   */
  @Test
  void testGetUserTimeZone() {
    eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
    assertEquals(this.timeZone, eventDetailsModel.getUserTimeZone());
  }

  /**
   * Test get time format.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetTimeFormat() throws Exception {
    eventDetailsModel = resource.adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    Date formattedStartDate =
        formatter.parse(currentPage.getProperties().get("eventStartDate", String.class));
    ZonedDateTime localDateTime = formattedStartDate.toInstant().atZone(ZoneId.systemDefault());
    ZonedDateTime originDatetime = localDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
    assertEquals("00:44", DateTimeFormatter.ofPattern("HH:mm").format(originDatetime));
  }

  /**
   * Test get length.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetLength() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    assertEquals(3, eventDetailsModel.getEventLengthDays());
  }

  /**
   * Test get date format.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetDateFormat() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    DateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy");
    Date formattedStartDate = formatter.parse(eventDetailsModel.getDateFormat());
    ZonedDateTime localDateTime = formattedStartDate.toInstant().atZone(ZoneId.systemDefault());
    localDateTime =
        localDateTime.withHour(Integer.valueOf(eventDetailsModel.getTimeFormat().split(":")[0]));
    localDateTime =
        localDateTime.withMinute(Integer.valueOf(eventDetailsModel.getTimeFormat().split(":")[1]));
    ZonedDateTime originDatetime = localDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
    assertEquals("Tuesday, Nov 22, 2022",
        DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy").format(originDatetime));
  }

  /**
   * Test get event location.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetEventLocation() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    assertEquals("California", eventDetailsModel.getEventLocation());
  }

  /**
   * Test get event host.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetEventHost() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    assertEquals("workday", eventDetailsModel.getEventHost());
  }

  /**
   * Test is configured.
   *
   * @throws Exception the exception
   */
  @Test
  void testIsConfigured() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    assertTrue(eventDetailsModel.isConfigured());
  }

  /**
   * Test get days label.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetDaysLabel() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    assertEquals("Days", eventDetailsModel.getDaysLabel());
  }

  /**
   * Test get event format without tags.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetEventFormatWithoutTags() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    assertEquals(new ArrayList<String>(), eventDetailsModel.getEventFormat());
  }

  /**
   * Test get event format with tags.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetEventFormatWithTags() throws Exception {
    eventDetailsModel = context.request().adaptTo(EventDetailsModel.class);
    assertNotNull(eventDetailsModel);
    eventDetailsModel.getEventFormat();
  }
}
