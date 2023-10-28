package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class UserServiceImplTest {
  private final AemContext context = new AemContext();

  /**
   * The userService.
   */
  @InjectMocks
  UserServiceImpl userService;

  /**
   * The ResourceResolver class.
   */
  @Mock
  ResourceResolver resourceResolver;

  @Mock
  SearchApiConfigService searchConfigService;

  @Mock
  SnapService snapService;

  @Mock
  ResourceResolverFactory resResolverFactory;

  CacheManagerServiceImpl cacheManager;

  /**
   * The UserManager class.
   */
  @Mock
  UserManager userManager;

  /**
   * The Session class.
   */
  @Mock
  Session session;

  /**
   * The mocked user.
   */
  @Mock
  User user;

  @Mock
  RunModeConfigService runModeConfigService;

  @BeforeEach
  public void setUp() throws Exception {
    cacheManager = new CacheManagerServiceImpl();
    CacheConfig cacheConfig = TestUtil.getCacheConfig();
    cacheManager.activate(cacheConfig);
    cacheManager.setResourceResolverFactory(resResolverFactory);
    context.registerService(CacheManagerServiceImpl.class, cacheManager);

    userService.setCacheManager(cacheManager);
    lenient().when(runModeConfigService.getInstance()).thenReturn(GlobalConstants.PUBLISH);
    session = mock(Session.class);
    userManager = mock(UserManager.class);
    user = mock(User.class);
    lenient().when(resResolverFactory.getServiceResourceResolver(any()))
        .thenReturn(resourceResolver);
    lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
    lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    lenient().when(userManager.getAuthorizable(anyString())).thenReturn(user);
  }

  /**
   * Test deleteUser method.
   *
   * @throws RepositoryException RepositoryException object.
   */
  @Test
  public void testDeleteUser() throws RepositoryException, CacheException {
    String userId = "testUser";
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    lenient().when(request.getResourceResolver()).thenReturn(resourceResolver);
    lenient().when(session.getUserID()).thenReturn(userId);
    lenient().when(session.isLive()).thenReturn(true);
    lenient().when(user.getPath()).thenReturn("/workdaycommunity/okta");

    userService.invalidCurrentUser(request, false);
    verify(user).remove();
    verify(session, times(2)).logout();
  }
}
