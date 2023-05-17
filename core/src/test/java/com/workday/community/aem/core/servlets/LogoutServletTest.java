package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.auth.Authenticator;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class LogoutServletTest {

  @Mock
  OktaService oktaService;

  @Mock
  RunModeConfigService runModeConfigService;

  @Mock
  UserService userService;

  @Mock
  Authenticator authenticator;

  @InjectMocks
  LogoutServlet logoutServlet;

  private final SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
  private final SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

  @Test
  public void testInit() throws ServletException {
    logoutServlet.init();
  }

  @Test
  public void testDoGet() throws IOException {
    logoutServlet.doGet(request, response);

    when(oktaService.getCustomDomain()).thenReturn("http://okta.workday.com");
    when(oktaService.getRedirectUri()).thenReturn("/redirect/uri");

    try (MockedStatic<HttpUtils>  mocked = mockStatic(HttpUtils.class)) {
      mocked.when( ()-> HttpUtils.dropCookies(eq(request), eq(response), anyString(), any())).thenReturn(0);
      ResourceResolver mockResolver = mock(ResourceResolver.class);
      Session session = mock(Session.class);
      lenient().when(request.getResourceResolver()).thenReturn(mockResolver);
      lenient().when(mockResolver.adaptTo(Session.class)).thenReturn(session);
      lenient().when(runModeConfigService.getInstance()).thenReturn(GlobalConstants.PUBLISH);
      lenient().when(session.getUserID()).thenReturn(GlobalConstants.PUBLISH);
      logoutServlet.doGet(request, response);
    }
  }
}
