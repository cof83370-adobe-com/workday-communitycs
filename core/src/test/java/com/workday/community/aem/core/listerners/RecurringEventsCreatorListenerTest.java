package com.workday.community.aem.core.listerners;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.listeners.RecurringEventsCreatorListener;
import com.workday.community.aem.core.services.CacheManagerService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.Calendar;
import java.util.Objects;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class RecurringEventsCreatorListenerTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class RecurringEventsCreatorListenerTest {

  /**
   * The context.
   */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  /**
   * The PageResourceListener.
   */
  @InjectMocks
  private RecurringEventsCreatorListener recurringEventsCreatorListener;

  @Mock
  private CacheManagerService cacheManager;

  /**
   * The expected event node.
   */
  private Node expectedEventNode;

  /**
   * The expected event map.
   */
  private ValueMap expectedEventMap;

  /**
   * The page manager.
   */
  @Mock
  private PageManager pageManager;

  /**
   * The session.
   */
  @Mock
  private Session session;

  /**
   * The resolver.
   */
  @Mock
  private ResourceResolver resolver;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    context.load().json(
        "/com/workday/community/aem/core/models/impl/RecurringEventsCreatorTestData.json",
        "/content");
    Page currentPage =
        Objects.requireNonNull(context.currentResource("/content/sample-recurring-events-page"))
            .adaptTo(Page.class);
    context.registerService(Page.class, Objects.requireNonNull(currentPage));
    context.registerService(ResourceResolver.class, resolver);
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
    Resource resource = mock(Resource.class);
    expectedEventNode = mock(Node.class);
    expectedEventMap = mock(ValueMap.class);
    lenient().when(resolver.getResource(
            Objects.requireNonNull(context.currentPage()).getContentResource().getPath()))
        .thenReturn(resource);
    lenient().when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    lenient().when(resource.adaptTo(Node.class)).thenReturn(expectedEventNode);
    lenient().when(resource.adaptTo(ValueMap.class)).thenReturn(expectedEventMap);

  }

  /**
   * Test generate recurring event pages.
   *
   * @throws Exception the exception
   */
  @Test
  void testGenerateRecurringEventPages() throws Exception {
    Property prop = mock(Property.class);
    lenient().when(expectedEventNode.hasProperty("recurringEvents")).thenReturn(true);
    lenient().when(expectedEventNode.getProperty("recurringEvents")).thenReturn(prop);
    lenient().when(prop.getString()).thenReturn("true");

    Property prop2 = mock(Property.class);
    lenient().when(expectedEventNode.getProperty("jcr:title")).thenReturn(prop2);
    lenient().when(prop2.getString()).thenReturn("recurring page title");

    Property prop3 = mock(Property.class);
    lenient().when(expectedEventNode.getProperty("eventFrequency")).thenReturn(prop3);
    lenient().when(prop3.getString()).thenReturn("monthly");

    Property prop4 = mock(Property.class);
    lenient().when(expectedEventNode.getProperty("eventStartDate")).thenReturn(prop4);
    lenient().when(prop4.getDate()).thenReturn(Calendar.getInstance());

    Page newPage = mock(Page.class);
    lenient().when(
            pageManager.create(anyString(), anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(newPage);
    String newlyCreatedPagePath = "/content/wd-coomunity/en-us/events/test-11-10-2023";
    lenient().when(newPage.getPath()).thenReturn(newlyCreatedPagePath);
    Node expectedNewEventNode = mock(Node.class);
    Resource newPageresource = mock(Resource.class);
    lenient().when(resolver.resolve(anyString())).thenReturn(newPageresource);
    lenient().when(newPageresource.adaptTo(Node.class)).thenReturn(expectedNewEventNode);
    String[] eventFormatTags = {"event:event-format/webinar"};
    Calendar eventStartDateCalInstance = mock(Calendar.class);
    Calendar eventEndDateCalInstance = mock(Calendar.class);
    lenient().when(expectedEventMap.get("eventStartDate", Calendar.class))
        .thenReturn(eventStartDateCalInstance);
    lenient().when(expectedEventMap.get("eventEndDate", Calendar.class))
        .thenReturn(eventEndDateCalInstance);
    lenient().when(expectedEventMap.get("eventFormat", String[].class)).thenReturn(eventFormatTags);
    lenient().when(expectedNewEventNode.getSession()).thenReturn(session);
    recurringEventsCreatorListener
        .generateRecurringEventPages(
            Objects.requireNonNull(context.currentPage()).getContentResource().getPath());
    verify(resolver).close();
  }

  /**
   * Test generate recurring event pages when recurring events prop not available.
   *
   * @throws Exception the exception
   */
  @Test
  void testGenerateRecurringEventPagesWhenRecurringEventsPropNotAvailable() throws Exception {

    lenient().when(expectedEventNode.hasProperty("recurringEvents")).thenReturn(false);

    recurringEventsCreatorListener
        .generateRecurringEventPages(
            Objects.requireNonNull(context.currentPage()).getContentResource().getPath());
    verify(resolver).close();
  }

  /**
   * Test generate recurring event pages when recurring events prop with not
   * selected.
   *
   * @throws Exception the exception
   */
  @Test
  void testGenerateRecurringEventPagesWhenRecurringEventsPropWithNotSelected() throws Exception {
    Property prop = mock(Property.class);
    lenient().when(expectedEventNode.getProperty("recurringEvents")).thenReturn(prop);
    lenient().when(prop.getString()).thenReturn("no");
    recurringEventsCreatorListener
        .generateRecurringEventPages(
            Objects.requireNonNull(context.currentPage()).getContentResource().getPath());
    verify(resolver).close();
  }

  /**
   * Test generate recurring event pages when issue with original page creation.
   */
  @Test
  void testGenerateRecurringEventPagesWhenIssueWithOriginalPageCreation() {
    expectedEventNode = null;

    recurringEventsCreatorListener
        .generateRecurringEventPages(
            Objects.requireNonNull(context.currentPage()).getContentResource().getPath());
    verify(resolver).close();
  }

  /**
   * Test generate recurring event pagesfor bi weekly.
   *
   * @throws Exception the exception
   */
  @Test
  void testGenerateRecurringEventPagesforBiWeekly() throws Exception {
    mockPageProps(expectedEventNode, expectedEventMap);
    mockPagePropTags(expectedEventMap);
    Page newPage = mock(Page.class);
    lenient().when(
            pageManager.create(anyString(), anyString(), anyString(), anyString(), anyBoolean()))
        .thenReturn(newPage);
    String newlyCreatedPagePath = "/content/wd-coomunity/en-us/events/test-11-10-2023";
    lenient().when(newPage.getPath()).thenReturn(newlyCreatedPagePath);
    Node expectedNewEventNode = mock(Node.class);
    Resource newPageresource = mock(Resource.class);
    lenient().when(resolver.resolve(anyString())).thenReturn(newPageresource);
    lenient().when(newPageresource.adaptTo(Node.class)).thenReturn(expectedNewEventNode);
    String[] eventFormatTags = {"event:event-format/webinar"};
    lenient().when(expectedEventMap.get("eventFormat", String[].class)).thenReturn(eventFormatTags);
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);
    lenient().when(expectedNewEventNode.getSession()).thenReturn(session);
    recurringEventsCreatorListener
        .generateRecurringEventPages(
            Objects.requireNonNull(context.currentPage()).getContentResource().getPath());
    verify(resolver).close();
  }

  /**
   * Mock page prop tags.
   *
   * @param expectedEventMap the expected event map
   */
  void mockPagePropTags(ValueMap expectedEventMap) {

    lenient().when(expectedEventMap.get("regionCountryTags", String[].class))
        .thenReturn(new String[] {"region-and-country:emea"});
    lenient().when(expectedEventMap.get("userTags", String[].class))
        .thenReturn(new String[] {"user:persona"});
    lenient().when(expectedEventMap.get("programsToolsTags", String[].class))
        .thenReturn(new String[] {"product:"});
    lenient().when(expectedEventMap.get("industryTags", String[].class))
        .thenReturn(new String[] {"industry:construction"});
    lenient().when(expectedEventMap.get("usingWorkdayTags", String[].class))
        .thenReturn(new String[] {"using-workday:7029"});
    lenient().when(expectedEventMap.get("productTags", String[].class))
        .thenReturn(new String[] {"product:107"});
    lenient().when(expectedEventMap.get("releaseTags", String[].class))
        .thenReturn(new String[] {"release:148", "release:149"});
    lenient().when(expectedEventMap.get("accessControlTags", String[].class))
        .thenReturn(new String[] {"access-control:customer_peakon_only",
            "access-control:partner_all"});
    // Return element was empty intentionally
    lenient().when(expectedEventMap.get("eventAudience", String[].class))
        .thenReturn(new String[] {});
    lenient().when(expectedEventMap.get("eventFormat", String[].class))
        .thenReturn(new String[] {"event:event-format/webinar/overview-demo"});

  }

  /**
   * Mock page props.
   *
   * @param expectedEventNode the expected event node
   * @param expectedEventMap  the expected event map
   * @throws RepositoryException the repository exception
   */
  void mockPageProps(Node expectedEventNode, ValueMap expectedEventMap) throws RepositoryException {
    Property prop1 = mock(Property.class);
    lenient().when(expectedEventNode.hasProperty("recurringEvents")).thenReturn(true);
    lenient().when(expectedEventNode.getProperty("recurringEvents")).thenReturn(prop1);
    lenient().when(prop1.getString()).thenReturn("true");

    Property prop2 = mock(Property.class);
    lenient().when(expectedEventNode.getProperty("jcr:title")).thenReturn(prop2);
    lenient().when(prop2.getString()).thenReturn("recurring page title");

    Property prop3 = mock(Property.class);
    lenient().when(expectedEventNode.getProperty("eventFrequency")).thenReturn(prop3);
    lenient().when(prop3.getString()).thenReturn("bi-wwekly");

    Property prop4 = mock(Property.class);
    lenient().when(expectedEventNode.getProperty("eventStartDate")).thenReturn(prop4);
    lenient().when(prop4.getDate()).thenReturn(Calendar.getInstance());

    lenient().when(expectedEventMap.get("eventLocation", String.class)).thenReturn("Virtual");

    lenient().when(expectedEventMap.get("eventHost", String.class)).thenReturn("James");

    lenient().when(expectedEventMap.get("alternateTimezone", String.class)).thenReturn("9 AM PDT");

    lenient().when(expectedEventMap.get("updatedDate", Calendar.class))
        .thenReturn(Calendar.getInstance());

    // Intentionally returned null
    lenient().when(expectedEventMap.get("retirementDate", Calendar.class)).thenReturn(null);

    lenient().when(expectedEventMap.get("author", String.class)).thenReturn("testuser");

    lenient().when(expectedEventMap.get("contentType", String[].class))
        .thenReturn(new String[] {"content-types:event"});

    lenient().when(expectedEventMap.get("eventStartDate", Calendar.class))
        .thenReturn(Calendar.getInstance());

    lenient().when(expectedEventMap.get("eventEndDate", Calendar.class))
        .thenReturn(Calendar.getInstance());
  }

}
