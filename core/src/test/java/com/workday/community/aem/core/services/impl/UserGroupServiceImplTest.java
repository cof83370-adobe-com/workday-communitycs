package com.workday.community.aem.core.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
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

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class UserGroupServiceImplTest {

    @Mock
    ResourceResolverFactory resResolverFactory;

    @Mock
    UserServiceImpl userService;

    /** The CoveoIndexApiConfigService service. */
    @Mock
    SnapConfig config;

    /** The IndexServicesImpl service. */
    @InjectMocks
    UserGroupServiceImpl userGroupService;

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
    void getUserGroupsBySfIdUserHasGroupsAssignedInAem() throws RepositoryException {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        mockResolver.when(() -> ResolverUtil.newResolver(any(), any())).thenReturn(resourceResolver);

        List<String> testAemGroups = List.of("aem-group1", "aem-group2");
        Group group1 = mock(Group.class);
        when(group1.getID()).thenReturn(testAemGroups.get(0));
        Group group2 = mock(Group.class);
        when(group2.getID()).thenReturn(testAemGroups.get(1));
        List<Group> testGroups = List.of(group1, group2);

        User mockUser = mock(User.class);
        when(mockUser.memberOf()).thenReturn(testGroups.iterator());

        mockCommonUtils.when(() -> CommonUtils.getLoggedInUser(resourceResolver)).thenReturn(mockUser);

        assertEquals(testAemGroups, userGroupService.getLoggedInUsersGroups());
    }

    @Test
    void getUserGroupsBySfIdUserNoGroupsAssignedInAem() throws RepositoryException {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        mockResolver.when(() -> ResolverUtil.newResolver(any(), any())).thenReturn(resourceResolver);

        Value mockValue = mock(Value.class);
        when(mockValue.getString()).thenReturn("123");
        Value[] values = { mockValue };

        String userId = "test-user";
        User mockUser = mock(User.class);
        when(mockUser.memberOf()).thenReturn(Collections.emptyIterator());
        when(mockUser.getProperty(eq(WccConstants.PROFILE_SOURCE_ID))).thenReturn(values);
        when(mockUser.getID()).thenReturn(userId);

        mockCommonUtils.when(() -> CommonUtils.getLoggedInUser(resourceResolver)).thenReturn(mockUser);
        List<String> testSfGroups = List.of("sf-group1", "sf-group2");
        List<String> testAemGroups = List.of("aem-group1", "aem-group2");
        UserGroupServiceImpl userGroupServiceMock = Mockito.spy(userGroupService);
        doReturn(testSfGroups).when(userGroupServiceMock).getUserGroupsFromSnap("123");
        doReturn(testAemGroups).when(userGroupServiceMock).convertSfGroupsToAemGroups(eq(testSfGroups));
        assertEquals(testAemGroups, userGroupServiceMock.getLoggedInUsersGroups());
        verify(userService).updateUser(eq(userId), any(), eq(testAemGroups));
    }

    @Test
    void convertSfGroupsArrayToAemGroups() {
        String testGroupMap = "{\"test sf group\" : [\"aem-group1\", \"aem-group2\"]}";
        Gson gson = new Gson();
        JsonObject testGroupObj = gson.fromJson(testGroupMap, JsonObject.class);

        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        mockResolver.when(() -> ResolverUtil.newResolver(any(), any())).thenReturn(resourceResolver);

        when(config.sfToAemUserGroupMap()).thenReturn("/path/to/file");
        mockDamUtils.when(() -> DamUtils.readJsonFromDam(eq(resourceResolver), eq("/path/to/file"))).thenReturn(testGroupObj);
        assertEquals(List.of("aem-group1", "aem-group2"), userGroupService.convertSfGroupsToAemGroups(List.of("test sf group")));

        testGroupMap = "{\"test sf group\" : \"aem-group\"}";
        testGroupObj = gson.fromJson(testGroupMap, JsonObject.class);
        userGroupService.groupMap = null;
        mockDamUtils.when(() -> DamUtils.readJsonFromDam(eq(resourceResolver), eq("/path/to/file"))).thenReturn(testGroupObj);
        assertEquals(List.of("aem-group"), userGroupService.convertSfGroupsToAemGroups(List.of("test sf group")));
    }

    @AfterEach
    public void after() {
        mockResolver.close();
        mockCommonUtils.close();
        mockDamUtils.close();
    }
}