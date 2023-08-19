package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.TagManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.EhCacheManager;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import junitx.framework.Assert;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoListViewModelImplTest {
    /** The AemContext object. */
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
    ResourceResolverFactory resourceResolverFactory;

    @Mock
    TagManager tagManager;

    @Mock
    List<CategoryFacetModel> categories;

    @Mock
    EhCacheManager ehCacheManager;

    MockedStatic<DamUtils> mockDamUtils;

    MockedStatic<ResolverUtil> resolverUtil;

    @InjectMocks
    CoveoListViewModelImpl coveoListViewModel;

    @InjectMocks
    CategoryFacetModel categoryFacetModel;

    @InjectMocks
    CategoryFacetModel categoryFacetModel1;

    @BeforeEach
    public void setUp() {
        context.addModelsForClasses(CoveoListViewModelImpl.class);
        context.addModelsForClasses(CategoryFacetModel.class);
        context.load().json("/com/workday/community/aem/core/models/impl/CoveoListViewModel.json", "/component");
        context.registerService(SearchApiConfigService.class, searchApiConfigService);
        context.registerService(SnapService.class, snapService);
        context.registerService(SlingHttpServletRequest.class, request);
        context.registerService(EhCacheManager.class, ehCacheManager);
        context.registerService(ResourceResolver.class, resourceResolver);

        String fieldMapConfig = "{\"tagIdToCoveoField\": {\"product\" : \"coveo_product\", \"using-workday\": \"coveo_using-workday\"}}";
        Gson gson = new Gson();
        JsonObject fieldMapConfigObj = gson.fromJson(fieldMapConfig, JsonObject.class);

        mockDamUtils = mockStatic(DamUtils.class);

        mockDamUtils.when(() -> DamUtils.readJsonFromDam(eq(resourceResolver), eq("/content/dam/workday-community/resources/coveo-field-map.json")))
                .thenReturn(fieldMapConfigObj);

        resolverUtil = mockStatic(ResolverUtil.class);
        resolverUtil.when(() -> ResolverUtil.newResolver(any(), anyString())).thenReturn(resourceResolver);

    }

    @Test
    void testGetConfig() throws RepositoryException {
        CoveoListViewModel listViewModel = context.currentResource("/component/listView").adaptTo(CoveoListViewModel.class);
        ((CoveoListViewModelImpl)listViewModel).init(request);

        ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
        Session session = mock(Session.class);
        UserManager userManager = mock(UserManager.class);
        User user = mock(User.class);

        Value[] profileSId = new Value[] {new Value() {
            @Override
            public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
                return "testSFId";
            }

            @Override
            public InputStream getStream() throws RepositoryException {
                return null;
            }

            @Override
            public Binary getBinary() throws RepositoryException {
                return null;
            }

            @Override
            public long getLong() throws ValueFormatException, RepositoryException {
                return 0;
            }

            @Override
            public double getDouble() throws ValueFormatException, RepositoryException {
                return 0;
            }

            @Override
            public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
                return null;
            }

            @Override
            public Calendar getDate() throws ValueFormatException, RepositoryException {
                return null;
            }

            @Override
            public boolean getBoolean() throws ValueFormatException, RepositoryException {
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
        JsonParser gsonParser = new JsonParser();
        String testData = "{\"success\":true,\"contactId\":\"sadsadadsa\",\"email\":\"foo@fiooo.com\",\"timeZone\":\"America/Los_Angeles\",\"contextInfo\":{\"functionalArea\":\"Other\",\"contactRole\":\"Workmate;Workday-professionalservices;workday;workday_professional_services;BetaUser\",\"productLine\":\"Other\",\"superIndustry\":\"Communications,Media&Technology\",\"isWorkmate\":true,\"type\":\"customer\"},\"contactInformation\":{\"propertyAccess\":\"Community\",\"nscSupporting\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"wsp\":\"WSP-Guided\",\"lastName\":\"Zhang\",\"firstName\":\"Wangchun\",\"customerOf\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"customerSince\":\"2019-01-28\"}}";
        JsonObject userContext = gsonParser.parse(testData).getAsJsonObject();
        userContext.addProperty("email", "testEmailFoo@workday.com");

        lenient().when(snapService.getUserContext(anyString())).thenReturn(userContext);
        lenient().when(searchApiConfigService.getOrgId()).thenReturn("TestOrgId");
        lenient().when(searchApiConfigService.getSearchHub()).thenReturn("TestSearchHub");
        JsonObject config = listViewModel.getSearchConfig();
        Assert.assertEquals(config.get("clientId").getAsString(), "eb6f7b59-e3d5-5199-8019-394c8982412b");

        assertEquals("TestOrgId",config.get("orgId").getAsString());
        assertEquals("TestSearchHub", config.get("searchHub").getAsString());
    }

    @Test
    void testGetCategories() throws RepositoryException {
        lenient().when(categories.get(0)).thenReturn(categoryFacetModel);
        lenient().when(categories.get(1)).thenReturn(categoryFacetModel1);
        lenient().when(categories.size()).thenReturn(2);
        lenient().when(categories.toArray()).thenReturn(new CategoryFacetModel[]{categoryFacetModel, categoryFacetModel1});

        List<CategoryFacetModel> categoryFacetModels = coveoListViewModel.getCategories();
        assertEquals(2, categoryFacetModels.size());
    }

    @AfterEach
    public void after() {
        resourceResolver.close();
        mockDamUtils.close();
        resolverUtil.close();
    }

}
