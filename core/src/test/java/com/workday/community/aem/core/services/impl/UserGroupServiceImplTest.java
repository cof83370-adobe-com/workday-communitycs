package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.exceptions.CacheException;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class UserGroupServiceImplTest {
    @Mock
    SlingHttpServletRequest request;

    @Mock
    UserServiceImpl userService;

    @Mock
    SnapService snapService;

    @Mock
    DrupalService drupalService;

    /**
     * The SnapConfig.
     */
    @Mock
    SnapConfig config;

    /**
     * The UserGroupServiceImpl service.
     */
    @InjectMocks
    UserGroupServiceImpl userGroupService;

    @Mock
    ResourceResolverFactory resResolverFactory;

    CacheManagerServiceImpl cacheManager;

    @Mock
    Session jcrSession;

    @Mock
    ResourceResolver mockResolver;

    MockedStatic<CommonUtils> mockCommonUtils;

    MockedStatic<DamUtils> mockDamUtils;

    Resource mockResource;
    Node mockNode;

    @BeforeEach
    public void setUp() throws Exception {
        cacheManager = new CacheManagerServiceImpl();
        CacheConfig cacheConfig = TestUtil.getCacheConfig();
        cacheManager.activate(cacheConfig);
        cacheManager.setResourceResolverFactory(resResolverFactory);
        userGroupService.setCacheManager(cacheManager);

        mockCommonUtils = mockStatic(CommonUtils.class);
        mockDamUtils = mockStatic(DamUtils.class);
        mockResource = mock(Resource.class);
        mockNode = mock(Node.class);
    }

    @Test
    void getUserGroupsBySfIdUserNodeHasGroups() throws RepositoryException, CacheException, LoginException {
        User mockUser = TestUtil.getMockUser();
        when(resResolverFactory.getServiceResourceResolver(any())).thenReturn(mockResolver);
        when(userService.getCurrentUser(request)).thenReturn(mockUser);
        when(mockResolver.getResource(mockUser.getPath())).thenReturn(mockResource);
        when(mockResource.adaptTo(Node.class)).thenReturn(mockNode);

        List<String> testAemGroups = List.of("role1", "role2");
        when(mockNode.hasProperty("roles")).thenReturn(true);
        String mockUserRole = "role1;role2";
        Property mockProperty = mock(Property.class);
        when(mockNode.getProperty("roles")).thenReturn(mockProperty);
        when(mockProperty.getString()).thenReturn(mockUserRole);

        assertEquals(testAemGroups, userGroupService.getCurrentUserGroups(request));
    }

    @Test
    void getUserGroupsBySfIdUserNodeDoesNotHaveAnyGroups() throws RepositoryException, LoginException, CacheException {
        User mockUser = TestUtil.getMockUser();
        when(resResolverFactory.getServiceResourceResolver(any())).thenReturn(mockResolver);
        when(userService.getCurrentUser(request)).thenReturn(mockUser);
        when(mockResolver.getResource(mockUser.getPath())).thenReturn(mockResource);
        when(mockResource.adaptTo(Node.class)).thenReturn(mockNode);

        String userId = "test-user";
        Value mockValue = mock(Value.class);
        when(mockValue.getString()).thenReturn(userId);
        Value[] values = { mockValue };

        when(mockUser.getProperty(eq(WccConstants.PROFILE_SOURCE_ID))).thenReturn(values);
        List<String> testSfGroups = List.of("sf-group1", "sf-group2");
        when(mockNode.hasProperty("roles")).thenReturn(false);

        UserGroupServiceImpl userGroupServiceMock = Mockito.spy(userGroupService);
        doReturn(testSfGroups).when(userGroupServiceMock).getUserGroupsFromDrupal(userId);

        Session mockSession = mock(Session.class);
        when(mockResolver.adaptTo(Session.class)).thenReturn(mockSession);

        List<String> res = userGroupServiceMock.getCurrentUserGroups(request);
        assertEquals(testSfGroups, res);
    }

    @Test
    void testCustomerRoles() throws NoSuchFieldException, IllegalAccessException {
        String SF_ID = "test=123";
        String userDataResponse = "{\"roles\":[\"authenticated\",\"customer_adaptive\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
        when(drupalService.getUserData(SF_ID)).thenReturn(userDataResponse);
        List<String> groups = userGroupService.getUserGroupsFromDrupal(SF_ID);
        assertTrue(groups.contains("authenticated"));
        assertTrue(groups.contains("customer_adaptive"));

    }

    @Test
    void testPartnerRoles() throws NoSuchFieldException, IllegalAccessException {
        String SF_ID = "test=123";
        String userDataResponse = "{\"roles\":[\"authenticated\",\"partner_all\",\"partner_innovation_track\",\"partner_sales_track\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
        when(drupalService.getUserData(SF_ID)).thenReturn(userDataResponse);
        List<String> groups = userGroupService.getUserGroupsFromDrupal(SF_ID);
        assertTrue(groups.contains("partner_all"));
        assertTrue(groups.contains("partner_innovation_track"));
        assertTrue(groups.contains("partner_sales_track"));
    }

    @Test
    void testCheckLoggedInUserHasAccessControlTags()
        throws IllegalStateException, RepositoryException, CacheException, LoginException {
        when(resResolverFactory.getServiceResourceResolver(any())).thenReturn(mockResolver);

        User mockUser = TestUtil.getMockUser();
        when(userService.getCurrentUser(request)).thenReturn(mockUser);
        when(mockResolver.getResource(mockUser.getPath())).thenReturn(mockResource);
        when(mockResource.adaptTo(Node.class)).thenReturn(mockNode);

        List<String> accessControlTags = List.of("authenticated");
        assertTrue(userGroupService.validateCurrentUser(request, accessControlTags));
        List<String> testAemGroups = List.of("role1");

        when(mockNode.hasProperty("roles")).thenReturn(true);
        String mockUserRole = "role1;role2";
        Property mockProperty = mock(Property.class);
        when(mockNode.getProperty("roles")).thenReturn(mockProperty);
        when(mockProperty.getString()).thenReturn(mockUserRole);

        assertTrue(userGroupService.validateCurrentUser(request, testAemGroups));
    }

    @AfterEach
    public void after() {
        mockResolver.close();
        mockCommonUtils.close();
        mockDamUtils.close();
    }
}