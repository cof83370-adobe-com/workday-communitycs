package com.workday.community.aem.core.filters;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
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
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class AuthorizationFilterTest {

    private final AemContext context = new AemContext();

    @Mock
    ResourceResolverFactory resolverFactory;

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
    RequestPathInfo requestPathInfo;

    @InjectMocks
    private AuthorizationFilter authorizationFilter;

    @Test
    void testD0FilterWithoutValidUser() throws ServletException, IOException, RepositoryException, org.apache.sling.api.resource.LoginException, OurmException {
        authorizationFilter.init(filterConfig);

        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
        when(resolverFactory.getServiceResourceResolver(serviceParams)).thenReturn(resolver);
        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn("/content/workday-community/test");
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("test-user1");

        when(userService.getUser(any(), anyString())).thenReturn(user);
        when(user.getPath()).thenReturn("home/users/test-user1");

        authorizationFilter.doFilter(request, response, filterChain);

        verify(userGroupService, times(0)).getLoggedInUsersGroups(any());
    }

    @Test
    void testDoFilterWithValidUserAndEveryoneTag() throws ServletException, IOException, RepositoryException, LoginException, OurmException {

        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
        when(resolverFactory.getServiceResourceResolver(serviceParams)).thenReturn(resolver);
        authorizationFilter.init(filterConfig);

        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        String pagePath = "/content/workday-community/test";
        Tag[] tags = new Tag[2];
        tags[0] = context.create().tag("work-day:groups/everyone");
        tags[1] = context.create().tag("work-day:groups/workmate");

        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn(pagePath);
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("workday-user1");
        when(userService.getUser(any(), anyString())).thenReturn(user);
        when(user.getPath()).thenReturn("home/users/workdaycommunity/okta/workday-user1");
        when(jcrSession.itemExists(anyString())).thenReturn(true);
        when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
        when(pageManager.getPage(anyString())).thenReturn(pageObj);
        when(pageObj.getTags()).thenReturn(tags);

        authorizationFilter.doFilter(request, response, filterChain);

        verify(userGroupService, times(0)).getLoggedInUsersGroups(any());
    }

    @Test
    void testDoFilterWithValidUserAndWithOutEveryoneTag() throws ServletException, IOException, RepositoryException, LoginException, OurmException {

        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
        when(resolverFactory.getServiceResourceResolver(serviceParams)).thenReturn(resolver);
        authorizationFilter.init(filterConfig);

        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        String pagePath = "/content/workday-community/test";
        Tag[] tags = new Tag[2];
        tags[0] = context.create().tag("work-day:groups/customer");
        tags[1] = context.create().tag("work-day:groups/workmate");


        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn(pagePath);
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("workday-user1");
        when(userService.getUser(any(), anyString())).thenReturn(user);
        when(user.getPath()).thenReturn("home/users/workdaycommunity/okta/workday-user1");
        when(jcrSession.itemExists(anyString())).thenReturn(true);
        when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
        when(pageManager.getPage(anyString())).thenReturn(pageObj);
        when(pageObj.getTags()).thenReturn(tags);

        authorizationFilter.doFilter(request, response, filterChain);

        verify(userGroupService, times(1)).getLoggedInUsersGroups(any());
    }

}
