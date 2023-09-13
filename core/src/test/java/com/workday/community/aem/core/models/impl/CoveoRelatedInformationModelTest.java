package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoRelatedInformationModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import junitx.framework.Assert;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class CoveoRelatedInformationModelTest {
  /**
   * AemContext
   */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  @Mock
  SlingHttpServletRequest request;
  @Mock
  SearchApiConfigService searchApiConfigService;
  @Mock
  DrupalService drupalService;

  @Mock
  ResourceResolver resourceResolver;

  @Mock
  TagManager tagManager;

  @Mock
  UserService userService;

  @Mock
  CacheManagerService cacheManager;

  @InjectMocks
  CoveoRelatedInformationModelImpl coveoRelatedInformationModel;

  MockedStatic<DamUtils> mockDamUtils;
  MockedStatic<ResolverUtil> resolverUtil;

  @BeforeEach
  public void setUp() throws CacheException {
    context.addModelsForClasses(CoveoRelatedInformationModel.class);
    context.load().json("/com/workday/community/aem/core/models/impl/CoveoRelatedInformationModel.json", "/component");
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    context.registerService(DrupalService.class, drupalService);
    context.registerService(SlingHttpServletRequest.class, request);
    context.registerService(ResourceResolver.class, resourceResolver);
    context.registerService(UserService.class, userService);
    lenient().when(resourceResolver.adaptTo(TagManager.class)).thenReturn(tagManager);
    Tag tag1Namespace = mock(Tag.class);
    lenient().when(tag1Namespace.getName()).thenReturn("product");
    Tag tag2Namespace = mock(Tag.class);
    lenient().when(tag2Namespace.getName()).thenReturn("using-workday");
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);

    ResourceResolver resolver = mock(ResourceResolver.class);
    lenient().when(request.getResourceResolver()).thenReturn(resolver);
    String pagePath = "/content/foo.html";
    lenient().when(request.getPathInfo()).thenReturn(pagePath);
    PageManager pageManager = mock(PageManager.class);
    lenient().when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    Page page = mock(Page.class);
    lenient().when(pageManager.getPage(anyString())).thenReturn(page);
    lenient().when(page.getTags()).thenReturn(new Tag[] { tag1, tag2 });
    lenient().when(tag1.getNamespace()).thenReturn(tag1Namespace);
    lenient().when(tag2.getNamespace()).thenReturn(tag2Namespace);
    lenient().when(tagManager.resolve("product:")).thenReturn(tag1);
    lenient().when(tagManager.resolve("using-workday:")).thenReturn(tag2);

    String fieldMapConfig = "{\"tagIdToCoveoField\": {\"product\" : \"coveo_product\", \"using-workday\": \"coveo_using-workday\"}}";
    Gson gson = new Gson();
    JsonObject fieldMapConfigObj = gson.fromJson(fieldMapConfig, JsonObject.class);

    mockDamUtils = mockStatic(DamUtils.class);

    mockDamUtils
        .when(() -> DamUtils.readJsonFromDam(eq(resourceResolver),
            eq("/content/dam/workday-community/resources/coveo-field-map.json")))
        .thenReturn(fieldMapConfigObj);

    resolverUtil = mockStatic(ResolverUtil.class);
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resourceResolver);
  }

  @Test
  public void testGetFacetFields() throws DamException {
    List<String> facetFields = coveoRelatedInformationModel.getFacetFields();
    assertEquals(2, facetFields.size());
    assertEquals("coveo_product", facetFields.get(0));
    assertEquals("coveo_using-workday", facetFields.get(1));
  }

  @Test
  void testGetSearchConfig() throws RepositoryException {
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
    String testData = "{\"success\":true,\"contactId\":\"sadsadadsa\",\"email\":\"foo@fiooo.com\",\"timeZone\":\"America/Los_Angeles\",\"contextInfo\":{\"functionalArea\":\"Other\",\"contactRole\":\"Workmate;Workday-professionalservices;workday;workday_professional_services;BetaUser\",\"productLine\":\"Other\",\"superIndustry\":\"Communications,Media&Technology\",\"isWorkmate\":true,\"type\":\"customer\"},\"contactInformation\":{\"propertyAccess\":\"Community\",\"nscSupporting\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"wsp\":\"WSP-Guided\",\"lastName\":\"Zhang\",\"firstName\":\"Wangchun\",\"customerOf\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"customerSince\":\"2019-01-28\"}}";
    lenient().when(userService.getUserUUID(anyString())).thenReturn("eb6f7b59-e3d5-5199-8019-394c8982412b");
    JsonObject userContext = JsonParser.parseString(testData).getAsJsonObject();
    userContext.addProperty("email", "testEmailFoo@workday.com");
    lenient().when(drupalService.getUserContext(anyString())).thenReturn(userContext);

    JsonObject searchConfig = coveoRelatedInformationModel.getSearchConfig();
    assertEquals(5, searchConfig.size());
    Assert.assertEquals(searchConfig.get("clientId").getAsString(), "eb6f7b59-e3d5-5199-8019-394c8982412b");
  }

  @AfterEach
  public void after() {
    resourceResolver.close();
    mockDamUtils.close();
    resolverUtil.close();
  }
}