package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.SnapConstants;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
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
    TagManager tagManager;

    MockedStatic<DamUtils> mockDamUtils;

    MockedStatic<ResolverUtil> resolverUtil;

    @BeforeEach
    public void setUp() {
        context.addModelsForClasses(CoveoListViewModelImpl.class);
        context.addModelsForClasses(CategoryFacetModel.class);
        context.load().json("/com/workday/community/aem/core/models/impl/CoveoListViewModel.json", "/component");
        context.registerService(SearchApiConfigService.class, searchApiConfigService);
        context.registerService(SnapService.class, snapService);
        context.registerService(SlingHttpServletRequest.class, request);

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
    void testComponent() throws RepositoryException {
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
        JsonObject userContext = new JsonObject();
        userContext.addProperty("email", "testEmailFoo@workday.com");
        lenient().when(snapService.getUserContext(anyString())).thenReturn(userContext);

        JsonObject config = listViewModel.getSearchConfig();
        Assert.assertEquals(config.get("clientId").getAsString(), "eb6f7b59-e3d5-5199-8019-394c8982412b");

        assertEquals("TestOrgId",config.get("orgId").getAsString());
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
