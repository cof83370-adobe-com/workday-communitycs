package com.workday.community.aem.core.filters;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class AuthorizationFilterTest {

    private final AemContext context = new AemContext();

    @Mock
    ResourceResolver resolver;

    @Mock
    UserGroupService userGroupService;

    @Mock
    UserService userService;

    @Mock
    Session jcrSession;

    @Mock
    UserManager userManager;

    @Mock
    PageManager pageManager;

    @Mock
    User user;

    @Mock
    Page pageObj;

    @Spy
    @InjectMocks
    MockSlingHttpServletRequest request = context.request();

    @Spy
    @InjectMocks
    MockSlingHttpServletResponse response = context.response();

    @Mock
    FilterChain filterChain;

    @Mock
    OktaService oktaService;

    @Mock
    FilterConfig filterConfig;

    @Mock
    CacheManagerService cacheManagerService;

    @Mock
    RequestPathInfo requestPathInfo;

    @InjectMocks
    private AuthorizationFilter authorizationFilter;

    @BeforeEach
    void setup() throws CacheException {
        when(cacheManagerService.getServiceResolver(anyString())).thenReturn(resolver);
    }

    @Test
    void testD0FilterWithoutValidUser() throws ServletException, IOException, RepositoryException, OurmException {
        authorizationFilter.init(filterConfig);
        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn("/content/workday-community/en-us/test");
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("test-user1");

        when(userService.getUser(any(), anyString())).thenReturn(user);
        when(user.getPath()).thenReturn("home/users/test-user1");

        authorizationFilter.doFilter(request, response, filterChain);

        verify(userGroupService, times(0)).getLoggedInUsersGroups(any());
    }

    @Test
    void testDoFilterWithValidUserAndAuthenticatedTag() throws ServletException, IOException, RepositoryException {
        authorizationFilter.init(filterConfig);

        String pagePath = "/content/workday-community/en-us/test";
        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn(pagePath);
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("workday-user1");
        when(userService.getUser(any(), anyString())).thenReturn(user);
        when(user.getPath()).thenReturn("home/users/workdaycommunity/okta/workday-user1");

        authorizationFilter.doFilter(request, response, filterChain);

        verify(userGroupService, times(1)).validateTheUser(any(),any(), anyString());
    }

    @Test
    void testDoFilterWithValidUserAndWithOutAuthenticatedTag() throws ServletException, IOException, RepositoryException {
        authorizationFilter.init(filterConfig);

        String pagePath = "/content/workday-community/en-us/test";
        Tag[] tags = new Tag[2];
        tags[0] = context.create().tag("access-control:customer_all");
        tags[1] = context.create().tag("access-control:customer_name_support_contact");

        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn(pagePath);
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("workday-user1");
        when(userService.getUser(any(), anyString())).thenReturn(user);
        when(user.getPath()).thenReturn("home/users/workdaycommunity/okta/workday-user1");

        authorizationFilter.doFilter(request, response, filterChain);

        verify(userGroupService, times(1)).validateTheUser(any(),any(), anyString());
    }

}
