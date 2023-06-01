package com.workday.community.aem.core.services.impl;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.User;
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

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class UserGroupServiceImplTest {

    @Mock
    ResourceResolverFactory resourceResolverFactory;

    @Mock
    UserServiceImpl userService;

    @Mock
    SnapService snapService;

    /**
     * The CoveoIndexApiConfigService service.
     */
    @Mock
    SnapConfig config;

    /**
     * The IndexServicesImpl service.
     */
    @InjectMocks
    UserGroupServiceImpl userGroupService;

    @Mock
    ResourceResolver jcrSessionResourceResolver;


    @Mock
    Session jcrSession;

    MockedStatic<ResolverUtil> mockResolver;

    MockedStatic<CommonUtils> mockCommonUtils;

    MockedStatic<DamUtils> mockDamUtils;

    @BeforeEach
    public void setUp() throws Exception {
        mockResolver = mockStatic(ResolverUtil.class);
        mockCommonUtils = mockStatic(CommonUtils.class);
        mockDamUtils = mockStatic(DamUtils.class);
    }

    @Test
    void getUserGroupsBySfIdUserNodeHasGroups() throws RepositoryException, OurmException {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        mockResolver.when(() -> ResolverUtil.newResolver(any(), any())).thenReturn(resourceResolver);

        List<String> testAemGroups = List.of("role1", "role2");
        User mockUser = mock(User.class);
        mockCommonUtils.when(() -> CommonUtils.getLoggedInUser(resourceResolver)).thenReturn(mockUser);

        Value value = mock(Value.class);
        Value[] values = {value};
        String expectedSfId = "testsfid";
        lenient().when(value.getString()).thenReturn(expectedSfId);
        lenient().when(mockUser.getProperty(WccConstants.PROFILE_SOURCE_ID)).thenReturn(values);
        Node mockNode = mock(Node.class);
        Resource mockResource = mock(Resource.class);

        when(resourceResolver.getResource(mockUser.getPath())).thenReturn(mockResource);
        when(mockResource.adaptTo(Node.class)).thenReturn(mockNode);
        when(mockNode.hasProperty("roles")).thenReturn(true);
        String mockUserRole = "role1;role2";
        Property mockProperty = mock(Property.class);
        when(mockNode.getProperty("roles")).thenReturn(mockProperty);
        when(mockProperty.getString()).thenReturn(mockUserRole);


        assertEquals(testAemGroups, userGroupService.getLoggedInUsersGroups(resourceResolver));
    }

    @Test
    void getUserGroupsBySfIdUserNodeDoesNotHaveAnyGroups() throws RepositoryException, OurmException, LoginException {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        mockResolver.when(() -> ResolverUtil.newResolver(any(), any())).thenReturn(resourceResolver);
        String userId = "test-user";
        Value mockValue = mock(Value.class);
        when(mockValue.getString()).thenReturn(userId);
        Value[] values = {mockValue};


        User mockUser = mock(User.class);
        when(mockUser.getProperty(eq(WccConstants.PROFILE_SOURCE_ID))).thenReturn(values);

        mockCommonUtils.when(() -> CommonUtils.getLoggedInUser(resourceResolver)).thenReturn(mockUser);
        List<String> testSfGroups = List.of("sf-group1", "sf-group2");


        Node mockNode = mock(Node.class);
        Resource mockResource = mock(Resource.class);

        when(resourceResolver.getResource(mockUser.getPath())).thenReturn(mockResource);
        when(mockResource.adaptTo(Node.class)).thenReturn(mockNode);

        Property mockProperty = mock(Property.class);
        when(mockNode.hasProperty("roles")).thenReturn(false);

        UserGroupServiceImpl userGroupServiceMock = Mockito.spy(userGroupService);
        doReturn(testSfGroups).when(userGroupServiceMock).getUserGroupsFromSnap(userId);

        when(resourceResolverFactory.getServiceResourceResolver(any())).thenReturn(jcrSessionResourceResolver);

        when(jcrSessionResourceResolver.adaptTo(Session.class)).thenReturn(jcrSession);
        Mockito.doNothing().when(jcrSession).save();

        assertEquals(testSfGroups, userGroupServiceMock.getLoggedInUsersGroups(resourceResolver));

    }

    @Test
    void testSnapService() throws NoSuchFieldException, IllegalAccessException {
        HashMap<String, String> customerRoleMap = new HashMap<>();
        customerRoleMap.put("Named Support Contact", "customer_name_support_contact");
        customerRoleMap.put("Training Coordinator", "customer_training_coordinator");
        Field customerRoleField = userGroupService.getClass().getDeclaredField("customerRoleMapping");
        customerRoleField.setAccessible(true);
        customerRoleField.set(userGroupService, customerRoleMap);

        HashMap<String, String> nscMap = new HashMap<>();
        nscMap.put("Adaptive Planning", "customer_adaptive_only");
        nscMap.put("Scout", "customer_scount_only");
        nscMap.put("Peakon", "customer_peakon_only");
        nscMap.put("VNDLY", "customer_vndly_only");
        Field nscField = userGroupService.getClass().getDeclaredField("nscSupportingMapping");
        nscField.setAccessible(true);
        nscField.set(userGroupService, nscMap);
        
        String SF_ID = "test=123";
        JsonObject context = new JsonObject();
        JsonObject contextInfoObj = new JsonObject();
        contextInfoObj.addProperty("contactRole", "Named Support Contact;Training Coordinator");
        contextInfoObj.addProperty("type", "customer");
        contextInfoObj.addProperty("isWorkmate", false);
        JsonObject contactInformationObj = new JsonObject();
        contactInformationObj.addProperty("propertyAccess", "Community");
        contactInformationObj.addProperty("nscSupporting", "Adaptive Planning;VNDLY");
        context.add("contextInfo", contextInfoObj);
        context.add("contactInformation", contactInformationObj);
        when(snapService.getUserContext(SF_ID)).thenReturn(context);
        List<String> groups = userGroupService.getUserGroupsFromSnap(SF_ID);
        assertTrue(groups.contains("authenticated"));
        assertTrue(groups.contains("customer_adaptive_only"));
        assertTrue(groups.contains("customer_vndly_only"));
        assertTrue(groups.contains("customer_name_support_contact"));
        assertTrue(groups.contains("customer_training_coordinator"));
        assertTrue(groups.contains("customer_all"));
    }


    @AfterEach
    public void after() {
        mockResolver.close();
        mockCommonUtils.close();
        mockDamUtils.close();
    }
}