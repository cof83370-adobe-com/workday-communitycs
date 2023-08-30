package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
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

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class JcrUserServiceImplTest {
    /** The userService. */
    @InjectMocks
    JcrJcrUserServiceImpl userService;

    /** The ResolverUtil class. */
    MockedStatic<ResolverUtil> resolver;

    /** The ResourceResolver class. */
    @Mock
    ResourceResolver resourceResolver;

    @Mock
    CacheManagerService cacheManager;
    
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
        lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(this.resourceResolver);
        session = mock(Session.class);
        userManager = mock(UserManager.class);
        user = mock(User.class);
        lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    }

    /**
     * Test deleteUser method.
     * 
     * @throws RepositoryException RepositoryException object.
     */
    @Test
    public void testDeleteUser() throws RepositoryException {
        String userId = "testUser";
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(user);
        lenient().when(user.getPath()).thenReturn("/workdaycommunity/okta");
        lenient().when(session.isLive()).thenReturn(true);
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        userService.invalidCurrentUser(request, false);
        verify(user).remove();
        verify(session).logout();
    }

     /**
     * Test deleteUser method failed case.
     * 
     * @throws RepositoryException RepositoryException object.
     */
    @Test
    public void testDeleteUserFail() throws RepositoryException {
        String userId = "testTest";
        lenient().when(userManager.getAuthorizableByPath(userId)).thenReturn(user);
        lenient().when(user.getPath()).thenReturn("/test");
        lenient().when(session.isLive()).thenReturn(true);
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        userService.invalidCurrentUser(request, true);
        verify(user, times(0)).remove();
        verify(session).logout();
    }

    /**
     * Test getUser method.
     *
     * @throws RepositoryException RepositoryException object.
     */
    @Test
    public void testGetUserWithResourceResolver() throws RepositoryException, CacheException {
        // Success case.
        String userId = "testUser";
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(user);
        User test = userService.getUser(WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE, userId);
        assertEquals(test, user);

        // Failed case.
        lenient().when(userManager.getAuthorizable(userId)).thenReturn(null);
        User fail = userService.getUser(WORKDAY_COMMUNITY_ADMINISTRATIVE_SERVICE, userId);
        assertNull(fail);
    }

    @AfterEach
    public void tearDown() {
        resolver.close();
    }

}
