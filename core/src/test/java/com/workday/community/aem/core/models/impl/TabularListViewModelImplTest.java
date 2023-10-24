package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.day.cq.commons.Filter;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.FeedTabModel;
import com.workday.community.aem.core.models.TabularListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import junit.framework.Assert;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class TabularListViewModelImplTest {
  /**
   * The AemContext object.
   */
  private final AemContext context = new AemContext();

  @Mock
  SlingHttpServletRequest request;

  @Mock
  SnapService snapService;

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

  JsonObject modelConfig = new JsonObject();

  @BeforeEach
  public void setUp() {
    context.addModelsForClasses(TabularListViewModel.class);
    context.addModelsForClasses(FeedTabModel.class);
    context.load().json("/com/workday/community/aem/core/models/impl/TabularListViewModel.json", "/component");
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    context.registerService(SnapService.class, snapService);
    context.registerService(SlingHttpServletRequest.class, request);
    context.registerService(UserService.class, userService);

    when(resourceResolver.adaptTo(TagManager.class)).thenReturn(tagManager);
    context.registerService(ResourceResolver.class, resourceResolver);

    Tag parentTag = new Tag() {

      @Override
      public <AdapterType> AdapterType adaptTo(Class<AdapterType> aClass) {
        return null;
      }

      @Override
      public String getName() {
        return "product";
      }

      @Override
      public String getTagID() {
        return "product:";
      }

      @Override
      public String getLocalTagID() {
        return null;
      }

      @Override
      public String getPath() {
        return null;
      }

      @Override
      public String getTitle() {
        return null;
      }

      @Override
      public String getTitle(Locale locale) {
        return null;
      }

      @Override
      public String getLocalizedTitle(Locale locale) {
        return null;
      }

      @Override
      public Map<Locale, String> getLocalizedTitles() {
        return null;
      }

      @Override
      public String getDescription() {
        return null;
      }

      @Override
      public String getTitlePath() {
        return null;
      }

      @Override
      public String getTitlePath(Locale locale) {
        return null;
      }

      @Override
      public Map<Locale, String> getLocalizedTitlePaths() {
        return null;
      }

      @Override
      public long getCount() {
        return 0;
      }

      @Override
      public long getLastModified() {
        return 0;
      }

      @Override
      public String getLastModifiedBy() {
        return null;
      }

      @Override
      public boolean isNamespace() {
        return false;
      }

      @Override
      public Tag getNamespace() {
        return null;
      }

      @Override
      public Tag getParent() {
        return null;
      }

      @Override
      public Iterator<Tag> listChildren() {
        return null;
      }

      @Override
      public Iterator<Tag> listChildren(Filter<Tag> filter) {
        return null;
      }

      @Override
      public Iterator<Tag> listAllSubTags() {
        return null;
      }

      @Override
      public Iterator<Resource> find() {
        return null;
      }

      @Override
      public String getXPathSearchExpression(String s) {
        return null;
      }

      @Override
      public String getGQLSearchExpression(String s) {
        return null;
      }
    };
    Tag productTag = new Tag() {
      @Override
      public <AdapterType> AdapterType adaptTo(Class<AdapterType> aClass) {
        return null;
      }

      @Override
      public String getName() {
        return "product";
      }

      @Override
      public String getTagID() {
        return "product:";
      }

      @Override
      public String getLocalTagID() {
        return null;
      }

      @Override
      public String getPath() {
        return null;
      }

      @Override
      public String getTitle() {
        return "Financial Management";
      }

      @Override
      public String getTitle(Locale locale) {
        return null;
      }

      @Override
      public String getLocalizedTitle(Locale locale) {
        return null;
      }

      @Override
      public Map<Locale, String> getLocalizedTitles() {
        return null;
      }

      @Override
      public String getDescription() {
        return null;
      }

      @Override
      public String getTitlePath() {
        return null;
      }

      @Override
      public String getTitlePath(Locale locale) {
        return null;
      }

      @Override
      public Map<Locale, String> getLocalizedTitlePaths() {
        return null;
      }

      @Override
      public long getCount() {
        return 0;
      }

      @Override
      public long getLastModified() {
        return 0;
      }

      @Override
      public String getLastModifiedBy() {
        return null;
      }

      @Override
      public boolean isNamespace() {
        return false;
      }

      @Override
      public Tag getNamespace() {
        return parentTag;
      }

      @Override
      public Tag getParent() {
        return parentTag;
      }

      @Override
      public Iterator<Tag> listChildren() {
        return null;
      }

      @Override
      public Iterator<Tag> listChildren(Filter<Tag> filter) {
        return null;
      }

      @Override
      public Iterator<Tag> listAllSubTags() {
        return null;
      }

      @Override
      public Iterator<Resource> find() {
        return null;
      }

      @Override
      public String getXPathSearchExpression(String s) {
        return null;
      }

      @Override
      public String getGQLSearchExpression(String s) {
        return null;
      }
    };

    when(tagManager.resolve(anyString())).thenReturn(productTag);

    String fieldMapConfig =
        "{\"tagIdToCoveoField\": {\"product\" : \"coveo_product\", \"using-workday\": \"coveo_using-workday\"}}";
    Gson gson = new Gson();
    JsonObject fieldMapConfigObj = gson.fromJson(fieldMapConfig, JsonObject.class);

    mockDamUtils = mockStatic(DamUtils.class);

    mockDamUtils.when(() -> DamUtils.readJsonFromDam(eq(resourceResolver),
            eq("/content/dam/workday-community/resources/coveo-field-map.json")))
        .thenReturn(fieldMapConfigObj);

    resolverUtil = mockStatic(ResolverUtil.class);
    resolverUtil.when(() -> ResolverUtil.newResolver(any(), anyString())).thenReturn(resourceResolver);

    JsonArray fields = new JsonArray();
    JsonObject field = new JsonObject();
    field.addProperty("name", "whats_new");
    field.addProperty("desc", "What's New Post");
    field.addProperty("dataExpression", "(@commcontenttype==(\"What's New Post\") OR @filetype==(whats_new))");
    fields.add(field);
    JsonObject field1 = new JsonObject();
    field1.addProperty("name", "article");
    field1.addProperty("desc", "Article");
    field1.addProperty("dataExpression", "\"(@commcontenttype==(Article) OR @filetype==(article))\"");
    fields.add(field1);
    modelConfig.add("fields", fields);

    JsonObject extra = new JsonObject();
    extra.addProperty("value", "(NOT @druwdcworkflowworkflowstate==retired)");
    modelConfig.add("extraCriteria", extra);

    mockDamUtils.when(() -> DamUtils.readJsonFromDam(eq(resourceResolver),
            eq("/content/dam/workday-community/resources/tab-list-criteria-data.json")))
        .thenReturn(modelConfig);
  }

  @Test
  void testComponent() throws RepositoryException, DamException {
    TabularListViewModel tabListViewModel =
        context.currentResource("/component/tabularListView").adaptTo(TabularListViewModel.class);
    if (tabListViewModel != null) {
      ((TabularListViewModelImpl) tabListViewModel).init(request);
    }

    List<FeedTabModel> feedTabModels = tabListViewModel.getSearches();
    assertEquals(2, feedTabModels.size());
    FeedTabModel article = feedTabModels.get(0);
    assertEquals("Articles", article.getTabTitle());
    FeedTabModel question = feedTabModels.get(1);
    assertEquals("News", question.getTabTitle());

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
    lenient().when(userService.getUserUuid(anyString())).thenReturn("eb6f7b59-e3d5-5199-8019-394c8982412b");

    mockDamUtils.when(() -> DamUtils.readJsonFromDam(eq(mockResourceResolver),
            eq("/content/dam/workday-community/resources/tab-list-criteria-data.json")))
        .thenReturn(modelConfig);
    JsonArray res = tabListViewModel.getSelectedFields();
    Assert.assertEquals(2, res.size());
  }

  @AfterEach
  public void after() {
    resourceResolver.close();
    mockDamUtils.close();
    resolverUtil.close();
  }

}