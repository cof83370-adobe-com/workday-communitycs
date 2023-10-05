package com.workday.community.aem.core.models.impl;

import static java.util.Calendar.JUNE;
import static java.util.Calendar.OCTOBER;
import static junitx.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoEventFeedModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.DamUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoEventFeedModelImplTest {
  /**
   * AemContext
   */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  @Mock
  SlingHttpServletRequest request;

  @Mock
  SearchApiConfigService searchApiConfigService;

  @Mock
  SnapService snapService;

  @Mock
  UserService userService;

  private CoveoEventFeedModel coveoEventFeedModel;

  @BeforeEach
  public void setup() {
    context.load().json("/com/workday/community/aem/core/models/impl/CoveoEventFeedTestData.json",
        "/content");
    Resource res = context.request().getResourceResolver().getResource("/content/event-feed-page");
    Page currentPage = res.adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    context.registerService(SnapService.class, snapService);
    context.registerService(SlingHttpServletRequest.class, request);
    context.registerService(UserService.class, userService);
    context.addModelsForClasses(CoveoEventFeedModelImpl.class);

    coveoEventFeedModel =
        context.getService(ModelFactory.class).createModel(res, CoveoEventFeedModel.class);
  }

  @Test
  void testGetSearchConfig() throws RepositoryException {
    ((CoveoEventFeedModelImpl) coveoEventFeedModel).init(request);
    ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
    Session session = mock(Session.class);
    UserManager userManager = mock(UserManager.class);
    User user = mock(User.class);

    Value[] profileSId = new Value[] {new Value() {
      @Override
      public String getString() throws IllegalStateException {
        return "testSFId";
      }

      @Override
      public InputStream getStream() {
        return null;
      }

      @Override
      public Binary getBinary() {
        return null;
      }

      @Override
      public long getLong() {
        return 0;
      }

      @Override
      public double getDouble() {
        return 0;
      }

      @Override
      public BigDecimal getDecimal() {
        return null;
      }

      @Override
      public Calendar getDate() {
        return null;
      }

      @Override
      public boolean getBoolean() {
        return false;
      }

      @Override
      public int getType() {
        return 0;
      }
    }};
    lenient().when(request.getResourceResolver()).thenReturn(mockResourceResolver);
    lenient().when(mockResourceResolver.adaptTo(Session.class)).thenReturn(session);
    lenient().when(session.getUserID()).thenReturn("userId");
    lenient().when(mockResourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
    lenient().when(userManager.getAuthorizable(eq("userId"))).thenReturn(user);
    lenient().when(user.getProperty(eq(SnapConstants.PROFILE_SOURCE_ID))).thenReturn(profileSId);

    String testData =
        "{\"success\":true,\"contactId\":\"sadsadadsa\",\"email\":\"foo@fiooo.com\",\"timeZone\":\"America/Los_Angeles\",\"contextInfo\":{\"functionalArea\":\"Other\",\"contactRole\":\"Workmate;Workday-professionalservices;workday;workday_professional_services;BetaUser\",\"productLine\":\"Other\",\"superIndustry\":\"Communications,Media&Technology\",\"isWorkmate\":true,\"type\":\"customer\"},\"contactInformation\":{\"propertyAccess\":\"Community\",\"nscSupporting\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"wsp\":\"WSP-Guided\",\"lastName\":\"Zhang\",\"firstName\":\"Wangchun\",\"customerOf\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"customerSince\":\"2019-01-28\"}}";
    JsonObject userContext = JsonParser.parseString(testData).getAsJsonObject();
    userContext.addProperty("email", "testEmailFoo@workday.com");

    lenient().when(snapService.getUserContext(anyString())).thenReturn(userContext);
    lenient().when(userService.getUserUUID(anyString()))
        .thenReturn("eb6f7b59-e3d5-5199-8019-394c8982412b");
    JsonObject searchConfig = coveoEventFeedModel.getSearchConfig();
    assertEquals(5, searchConfig.size());
    assertEquals(searchConfig.get("clientId").getAsString(),
        "eb6f7b59-e3d5-5199-8019-394c8982412b");
  }

  @Test
  void testGetFeatureEventNotResolved() throws RepositoryException {
    ((CoveoEventFeedModelImpl) coveoEventFeedModel).init(request);

    ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
    PageManager pageManager = mock(PageManager.class);
    lenient().when(request.getResourceResolver()).thenReturn(mockResourceResolver);
    lenient().when(mockResourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);

    Map<String, String> test = coveoEventFeedModel.getFeatureEvent();
    assertEquals(0, test.size());
  }

  @Test
  void testGetEventCriteria() throws DamException {
    try (MockedStatic<DamUtils> mocked = mockStatic(DamUtils.class)) {
      ((CoveoEventFeedModelImpl) coveoEventFeedModel).init(this.request);
      JsonObject modelConfig = new JsonObject();
      modelConfig.addProperty("eventCriteria", "foo");
      mocked.when(
              () -> DamUtils.readJsonFromDam(eq(this.request.getResourceResolver()), anyString()))
          .thenReturn(modelConfig);

      String res = coveoEventFeedModel.getEventCriteria();
      assertEquals("(foo)", res);
    }
  }

  @Test
  void testOthers() throws DamException {
    try (MockedStatic<DamUtils> mocked = mockStatic(DamUtils.class)) {
      ((CoveoEventFeedModelImpl) coveoEventFeedModel).init(this.request);
      JsonObject modelConfig = new JsonObject();
      modelConfig.addProperty("sortCriteria", "foo");
      modelConfig.addProperty("allEventsUrl", "foo1");
      modelConfig.addProperty("extraCriteria", "foo2");
      mocked.when(
              () -> DamUtils.readJsonFromDam(eq(this.request.getResourceResolver()), anyString()))
          .thenReturn(modelConfig);

      assertEquals("foo", coveoEventFeedModel.getSortCriteria());
      assertEquals("foo1", coveoEventFeedModel.getAllEventsUrl());
      assertEquals("foo2", coveoEventFeedModel.getExtraCriteria());
    }
  }

  @Test
  void testGetFeatureEventResolved() throws RepositoryException {
    ((CoveoEventFeedModelImpl) coveoEventFeedModel).init(request);

    ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
    PageManager pageManager = mock(PageManager.class);
    Page page = mock(Page.class);

    ValueMap testValues = new ValueMapDecorator(ImmutableMap.of(
        "eventStartDate", new GregorianCalendar(2023, JUNE, 3),
        "eventEndDate", new GregorianCalendar(2023, OCTOBER, 3),
        "eventLocation", "Bay area"
    ));

    lenient().when(request.getResourceResolver()).thenReturn(mockResourceResolver);
    lenient().when(mockResourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    lenient().when(pageManager.getPage(anyString())).thenReturn(page);
    lenient().when(page.getProperties()).thenReturn(testValues);

    Map<String, String> test = coveoEventFeedModel.getFeatureEvent();
    assertEquals(9, test.size());
    assertEquals("featureEventPath.html", test.get("link"));
  }
}
