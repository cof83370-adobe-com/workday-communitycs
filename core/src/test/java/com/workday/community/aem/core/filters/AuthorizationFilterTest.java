package com.workday.community.aem.core.filters;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.services.impl.UserGroupServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.IOException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
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

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class AuthorizationFilterTest {

  private final AemContext context = new AemContext();

  @Mock
  ResourceResolver resolver;

  @Mock
  UserGroupServiceImpl userGroupService;

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
  void setup() {
  }

  @Test
  void testD0FilterWithoutValidUser()
      throws ServletException, IOException, RepositoryException, CacheException {
    authorizationFilter.init(filterConfig);
    when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
    when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
    when(request.getRequestPathInfo().getResourcePath()).thenReturn(
        "/content/workday-community/en-us/test");
    when(request.getResourceResolver()).thenReturn(resolver);
    when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
    when(jcrSession.getUserID()).thenReturn("test-user1");

    when(userService.getCurrentUser(any())).thenReturn(user);
    when(user.getPath()).thenReturn("home/users/test-user1");

    authorizationFilter.doFilter(request, response, filterChain);

    verify(userGroupService, times(0)).getCurrentUserGroups(any());
  }

  @Test
  void testDoFilterWithValidUserAndAuthenticatedTag()
      throws ServletException, IOException, RepositoryException, CacheException {
    authorizationFilter.init(filterConfig);

    String pagePath = "/content/workday-community/en-us/test";
    when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
    when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
    when(request.getRequestPathInfo().getResourcePath()).thenReturn(pagePath);
    when(request.getResourceResolver()).thenReturn(resolver);
    when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
    when(jcrSession.getUserID()).thenReturn("workday-user1");
    when(userService.getCurrentUser(any())).thenReturn(user);
    when(user.getPath()).thenReturn("home/users/workdaycommunity/okta/workday-user1");

    authorizationFilter.doFilter(request, response, filterChain);

    verify(userGroupService, times(1)).validateCurrentUser(any(), anyString());
  }

  @Test
  void testDoFilterWithValidUserAndWithOutAuthenticatedTag()
      throws ServletException, IOException, RepositoryException, CacheException {
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
    when(userService.getCurrentUser(any())).thenReturn(user);
    when(user.getPath()).thenReturn("home/users/workdaycommunity/okta/workday-user1");

    authorizationFilter.doFilter(request, response, filterChain);

    verify(userGroupService, times(1)).validateCurrentUser(any(), anyString());
  }

}
