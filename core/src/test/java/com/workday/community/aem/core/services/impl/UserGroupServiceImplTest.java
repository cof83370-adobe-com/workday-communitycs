package com.workday.community.aem.core.services.impl;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class UserGroupServiceImplTest {

    @Mock
    ResourceResolverFactory resourceResolverFactory;

    @Mock
    SlingHttpServletRequest request;

    @Mock
    JcrUserServiceImpl userService;

    @Mock
    SnapService snapService;

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
    void getUserGroupsBySfIdUserNodeHasGroups() throws RepositoryException {
        User mockUser = TestUtil.getMockUser();
        when(request.getResourceResolver()).thenReturn(mockResolver);
        when(userService.getCurrentUser(request)).thenReturn(mockUser);
        when(mockResolver.getResource(mockUser.getPath())).thenReturn(mockResource);
        when(mockResource.adaptTo(Node.class)).thenReturn(mockNode);

        List<String> testAemGroups = List.of("role1", "role2");
        when(mockNode.hasProperty("roles")).thenReturn(true);
        String mockUserRole = "role1;role2";
        Property mockProperty = mock(Property.class);
        when(mockNode.getProperty("roles")).thenReturn(mockProperty);
        when(mockProperty.getString()).thenReturn(mockUserRole);

        assertEquals(testAemGroups, userGroupService.getCurrentUsersGroups(request));
    }

    @Test
    void getUserGroupsBySfIdUserNodeDoesNotHaveAnyGroups() throws RepositoryException, LoginException {
        User mockUser = TestUtil.getMockUser();
        when(request.getResourceResolver()).thenReturn(mockResolver);
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
        doReturn(testSfGroups).when(userGroupServiceMock).getUserGroupsFromSnap(userId);

        Session mockSession = mock(Session.class);
        when(mockResolver.adaptTo(Session.class)).thenReturn(mockSession);

        List<String> res = userGroupServiceMock.getCurrentUsersGroups(request);
        assertEquals(testSfGroups, res);
    }

    @Test
    void testCustomerRoles() throws NoSuchFieldException, IllegalAccessException {
        HashMap<String, String> customerRoleMap = new HashMap<>();
        customerRoleMap.put("Named Support Contact", "customer_name_support_contact");
        customerRoleMap.put("Training Coordinator", "customer_training_coordinator");
        Field customerRoleField = userGroupService.getClass().getDeclaredField("customerRoleMapping");
        customerRoleField.setAccessible(true);
        customerRoleField.set(userGroupService, customerRoleMap);

        HashMap<String, String> nscMap = new HashMap<>();
        nscMap.put("Adaptive Planning", "customer_adaptive");
        nscMap.put("Scout", "customer_scount");
        nscMap.put("Peakon", "customer_peakon");
        nscMap.put("VNDLY", "customer_vndly");
        Field nscField = userGroupService.getClass().getDeclaredField("customerOfMapping");
        nscField.setAccessible(true);
        nscField.set(userGroupService, nscMap);

        HashMap<String, String> wspMap = new HashMap<>();
        wspMap.put("Customer - WSP Enhanced", "customer_wsp_enhanced");
        Field wspField = userGroupService.getClass().getDeclaredField("wspMapping");
        wspField.setAccessible(true);
        wspField.set(userGroupService, wspMap);

        String SF_ID = "test=123";
        JsonObject context = new JsonObject();
        JsonObject contextInfoObj = new JsonObject();
        contextInfoObj.addProperty("contactRole", "Named Support Contact;Training Coordinator");
        contextInfoObj.addProperty("type", "customer");
        contextInfoObj.addProperty("isWorkmate", false);
        JsonObject contactInformationObj = new JsonObject();
        contactInformationObj.addProperty("propertyAccess", "Community");
        contactInformationObj.addProperty("customerOf", "Adaptive Planning;VNDLY");
        contactInformationObj.addProperty("wsp", "Customer - WSP Enhanced");
        context.add("contextInfo", contextInfoObj);
        context.add("contactInformation", contactInformationObj);
        when(snapService.getUserContext(SF_ID)).thenReturn(context);
        List<String> groups = userGroupService.getUserGroupsFromSnap(SF_ID);
        assertTrue(groups.contains("authenticated"));
        assertTrue(groups.contains("customer_adaptive"));
        assertTrue(groups.contains("customer_vndly"));
        assertTrue(groups.contains("customer_wsp_enhanced"));
        assertTrue(groups.contains("customer_name_support_contact"));
        assertTrue(groups.contains("customer_training_coordinator"));
        assertTrue(groups.contains("customer_all"));
    }

    @Test
    void testPartnerRoles() throws NoSuchFieldException, IllegalAccessException {
        HashMap<String, String> partnerRoleMap = new HashMap<>();
        partnerRoleMap.put("Innovation", "partner_innovation_track");
        partnerRoleMap.put("Sales", "partner_sales_track");
        partnerRoleMap.put("Services", "partner_services_track");
        Field partnerTrackMappingField = userGroupService.getClass().getDeclaredField("partnerTrackMapping");
        partnerTrackMappingField.setAccessible(true);
        partnerTrackMappingField.set(userGroupService, partnerRoleMap);

        String SF_ID = "test=123";
        JsonObject context = new JsonObject();
        JsonObject contextInfoObj = new JsonObject();
        contextInfoObj.addProperty("type", "partner");
        contextInfoObj.addProperty("isWorkmate", false);
        contextInfoObj.addProperty("contactRole", "");
        JsonObject contactInformationObj = new JsonObject();
        contactInformationObj.addProperty("propertyAccess", "Community");
        contactInformationObj.addProperty("partnerTrack", "Innovation;Sales");
        contactInformationObj.addProperty("wsp", "");
        context.add("contextInfo", contextInfoObj);
        context.add("contactInformation", contactInformationObj);
        when(snapService.getUserContext(SF_ID)).thenReturn(context);
        List<String> groups = userGroupService.getUserGroupsFromSnap(SF_ID);
        assertTrue(groups.contains("partner_all"));
        assertTrue(groups.contains("partner_innovation_track"));
        assertTrue(groups.contains("partner_sales_track"));
    }

    @Test
    void testCheckLoggedInUserHasAccessControlTags()
        throws IllegalStateException, RepositoryException {
        User mockUser = TestUtil.getMockUser();
        when(request.getResourceResolver()).thenReturn(mockResolver);
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