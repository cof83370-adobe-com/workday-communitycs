package com.workday.community.aem.core.filters;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.services.OktaService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.request.RequestPathInfo;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class AuthorizationFilterTest {

    private final AemContext context = new AemContext();

    @Mock
    ResourceResolverFactory resolverFactory;

    @Mock
    ResourceResolver resolver;

    @Mock
    Session jcrSession;

    @Mock
    UserManager userManager;

    @Mock
    PageManager pageManager;


    @Mock
    Authorizable user;

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
    void testdoFilterWithoutValidUser() throws ServletException, IOException, RepositoryException, org.apache.sling.api.resource.LoginException {

        authorizationFilter.init(filterConfig);

        final ServletOutputStream outputStream = mock(ServletOutputStream.class);

        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");

        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn("/content/workday-community/test");
        //when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("admin");
        when(resolverFactory.getServiceResourceResolver(serviceParams)).thenReturn(resolver);
        when(resolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("admin")).thenReturn(user);

        authorizationFilter.doFilter(request, response, filterChain);


    }


    @Test
    void testdoFilterWithValidUser() throws ServletException, IOException, RepositoryException, org.apache.sling.api.resource.LoginException {

        authorizationFilter.init(filterConfig);

        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        String pagePath = "/content/workday-community/test";
        Tag[] tags = new Tag[10];
        tags[0] = context.create().tag("work-day:groups/everyone");

        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");

        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
        when(request.getRequestPathInfo().getResourcePath()).thenReturn("/content/workday-community/test");
        //when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(request.getResourceResolver()).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
        when(jcrSession.getUserID()).thenReturn("admin");
        when(resolverFactory.getServiceResourceResolver(serviceParams)).thenReturn(resolver);
        when(resolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("admin")).thenReturn(user);
        //when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
        //when(pageManager.getPage(pagePath)).thenReturn(pageObj);
        //when(pageObj.getTags()).thenReturn(tags);
        authorizationFilter.doFilter(request, response, filterChain);


    }

}
