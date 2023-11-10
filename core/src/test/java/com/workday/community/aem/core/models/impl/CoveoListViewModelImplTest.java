package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import junitx.framework.Assert;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoListViewModelImplTest {
  /**
   * The AemContext object.
   */
  private final AemContext context = new AemContext();

  @Mock
  SlingHttpServletRequest request;

  @Mock
  DrupalService drupalService;

  @Mock
  SearchApiConfigService searchApiConfigService;

  @Mock
  ResourceResolver resourceResolver;

  @Mock
  TagManager tagManager;

  @Mock
  UserService userService;

  MockedStatic<DamUtils> mockDamUtils;

  MockedStatic<ResolverUtil> resolverUtil;

  @BeforeEach
  public void setUp() {
    context.addModelsForClasses(CoveoListViewModelImpl.class);
    context.addModelsForClasses(CategoryFacetModel.class);
    context.load()
        .json("/com/workday/community/aem/core/models/impl/CoveoListViewModel.json", "/component");
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    context.registerService(DrupalService.class, drupalService);
    context.registerService(SlingHttpServletRequest.class, request);
    context.registerService(UserService.class, userService);

    when(searchApiConfigService.getSearchHub()).thenReturn("TestSearchHub");
    when(searchApiConfigService.getOrgId()).thenReturn("TestOrgId");
    when(resourceResolver.adaptTo(TagManager.class)).thenReturn(tagManager);
    context.registerService(ResourceResolver.class, resourceResolver);

    Tag tag1Namespace = mock(Tag.class);
    when(tag1Namespace.getName()).thenReturn("product");

    Tag tag2Namespace = mock(Tag.class);
    when(tag2Namespace.getName()).thenReturn("using-workday");
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    when(tag1.getNamespace()).thenReturn(tag1Namespace);
    when(tag1.isNamespace()).thenReturn(true);
    when(tag2.getNamespace()).thenReturn(tag2Namespace);
    when(tag2.isNamespace()).thenReturn(true);
    when(tagManager.resolve("product:")).thenReturn(tag1);
    when(tagManager.resolve("using-workday:")).thenReturn(tag2);

    String fieldMapConfig =
        "{\"tagIdToCoveoField\": {\"product\" : \"coveo_product\", \"using-workday\": \"coveo_using-workday\"}, \"simpleFacetFields\":[\"test1\"] }";
    Gson gson = new Gson();
    JsonObject fieldMapConfigObj = gson.fromJson(fieldMapConfig, JsonObject.class);

    mockDamUtils = mockStatic(DamUtils.class);

        mockDamUtils
                .when(() -> DamUtils.readJsonFromDam(eq(resourceResolver),
                        eq("/content/dam/workday-community/resources/coveo-field-map.json")))
                .thenReturn(fieldMapConfigObj);

    resolverUtil = mockStatic(ResolverUtil.class);
    resolverUtil.when(() -> ResolverUtil.newResolver(any(), anyString()))
        .thenReturn(resourceResolver);
  }

  @Test
  void testComponent() throws RepositoryException {
    CoveoListViewModel listViewModel =
        context.currentResource("/component/listView")
                .adaptTo(CoveoListViewModel.class);
    if (listViewModel != null) {
      ((CoveoListViewModelImpl) listViewModel).init(request);
    }

    ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
    Session session = mock(Session.class);
    UserManager userManager = mock(UserManager.class);
    User user = mock(User.class);

    Value[] profileSId = new Value[] { new Value() {
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
    } };
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

    lenient().when(drupalService.getUserContext(anyString())).thenReturn(userContext);
    lenient().when(userService.getUserUuid(anyString()))
        .thenReturn("eb6f7b59-e3d5-5199-8019-394c8982412b");

    JsonObject config = listViewModel.getSearchConfig();
    Assert.assertEquals(config.get("clientId").getAsString(),
        "eb6f7b59-e3d5-5199-8019-394c8982412b");

    assertEquals("TestOrgId", config.get("orgId").getAsString());
    assertEquals("TestSearchHub", config.get("searchHub").getAsString());
    List<CategoryFacetModel> categoryFacetModels = listViewModel.getCategories();
    assertEquals(2, categoryFacetModels.size());
    CategoryFacetModel prod = categoryFacetModels.get(0);
    assertEquals("coveo_product", prod.getField());
    CategoryFacetModel usingWorkday = categoryFacetModels.get(1);
    assertEquals("coveo_using-workday", usingWorkday.getField());
  }

  @AfterEach
  public void after() {
    resourceResolver.close();
    mockDamUtils.close();
    resolverUtil.close();
  }

}
