package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
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

import com.workday.community.aem.core.utils.ResolverUtil;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static com.workday.community.aem.core.services.impl.UserServiceImpl.SERVICE_USER;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class UserServiceImplTest {

    /** The ResourceResolverFactory service. */
    @Mock
    ResourceResolverFactory resourceResolverFactory;

    /** The userService. */
    @InjectMocks
    UserServiceImpl userService;

    /** The ResolverUtil class. */
    MockedStatic<ResolverUtil> resolver;

    /** The ResourceResolver class. */
    @Mock
    ResourceResolver resourceResolver;
    
    /** The UserManager class. */
    @Mock
    UserManager userManager;
    
    /** The Session class. */
    @Mock 
    Session session;
    
    /** The mocked user. */
    @Mock
    User user;
    
    @BeforeEach
    public void setUp() throws Exception {
        this.resolver = mockStatic(ResolverUtil.class);
        this.resourceResolver = mock(ResourceResolver.class);
        resolver.when(() -> ResolverUtil.newResolver(any(), eq(SERVICE_USER))).thenReturn(this.resourceResolver);

        session = mock(Session.class);
        userManager = mock(UserManager.class);
        user = mock(User.class);
        lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    }

    /**
     * Test updateUser method.
     * 
     * @throws AuthorizableExistsException
     * @throws RepositoryException
     */
    @Test
    public void testUpdateUser() throws AuthorizableExistsException, RepositoryException {
        String userId = "testUser";
        String groupId = "dummyGroup";
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("email", "test@workday.com");
        List<String> groups = new ArrayList<String>();
        groups.add(groupId);
        Group group = mock(Group.class);
        List<Group> userGroups = new ArrayList<Group>();
        Iterator<Group> it = userGroups.iterator();
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(user);
        lenient().when(userManager.getAuthorizable(groupId)).thenReturn(null);
        lenient().when(userManager.createGroup(groupId)).thenReturn(group);
        lenient().when(user.memberOf()).thenReturn(it);
        ValueFactory valueFactory = mock(ValueFactory.class);
        Value value = valueFactory.createValue("test@workday.com", PropertyType.STRING);
        lenient().when(session.getValueFactory()).thenReturn(valueFactory);
        lenient().when(session.isLive()).thenReturn(true);
        userService.updateUser(userId, fields, groups);
        verify(user).setProperty("email", value);
        verify(userManager).createGroup(groupId);
        verify(group).addMember(user);
        verify(session).logout();
    }

    /**
     * Test updateUser method failed.
     * 
     * @throws AuthorizableExistsException
     * @throws RepositoryException
     */
    @Test
    public void testUpdateUserFail() throws AuthorizableExistsException, RepositoryException {
        String userId = "testUser";
        String groupId = "dummyGroup";
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("email", "test@workday.com");
        ValueFactory valueFactory = mock(ValueFactory.class);
        Value value = valueFactory.createValue("test@workday.com", PropertyType.STRING);
        List<String> groups = new ArrayList<String>();
        groups.add(groupId);
        Group group = mock(Group.class);
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(null);
        lenient().when(session.isLive()).thenReturn(true);
        userService.updateUser(userId, fields, groups);
        verify(user, times(0)).setProperty("email", value);
        verify(userManager, times(0)).createGroup(groupId);
        verify(group, times(0)).addMember(user);
        verify(session).logout();
    }

    /**
     * Test deleteUser method.
     * 
     * @throws RepositoryException
     */
    @Test
    public void testDeleteUser() throws RepositoryException {
        String userId = "testUser";
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(user);
        lenient().when(user.getPath()).thenReturn("/workdaycommunity/okta");
        lenient().when(session.isLive()).thenReturn(true);
        userService.deleteUser(userId);
        verify(user).remove();
        verify(session).logout();
    }

     /**
     * Test deleteUser method failed case.
     * 
     * @throws RepositoryException
     */
    @Test
    public void testDeleteUserFail() throws RepositoryException {
        String userId = "testTest";
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(user);
        lenient().when(user.getPath()).thenReturn("/test");
        lenient().when(session.isLive()).thenReturn(true);
        userService.deleteUser(userId);
        verify(user, times(0)).remove();
        verify(session).logout();
    }

    /**
     * Test getUser method.
     * 
     * @throws RepositoryException
     */
    @Test
    public void testGetUser() throws RepositoryException {
        // Success case.
        String userId = "testUser";
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(user);
        User test = userService.getUser(userId);
        assertEquals(test, user); 

        // Failed case.
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(null);
        User fail = userService.getUser(userId);
        assertNull(fail);
    }

    /**
     * Test getUser method.
     *
     * @throws RepositoryException
     */
    @Test
    public void testGetUserWithResourceResolver() throws RepositoryException {
        // Success case.
        String userId = "testUser";
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(user);
        User test = userService.getUser(userId,resourceResolver);
        assertEquals(test, user);

        // Failed case.
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(null);
        User fail = userService.getUser(userId, resourceResolver);
        assertNull(fail);
    }

    @AfterEach
    public void tearDown() {
        resolver.close();
    }

}
