package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.utils.HttpUtils;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class LogoutServletTest {

  @Mock
  OktaService oktaService;

  @InjectMocks
  LogoutServlet logoutServlet;

  @Test
  public void testDoGet() throws IOException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

    when(oktaService.getCustomDomain()).thenReturn("http://okta.workday.com");
    when(oktaService.getRedirectUri()).thenReturn("/redirect/uri");

    try (MockedStatic<HttpUtils>  mocked = mockStatic(HttpUtils.class)) {
      mocked.when( ()-> HttpUtils.dropCookies(eq(request), eq(response), anyString())).thenReturn(0);
      logoutServlet.doGet(request, response);
    }
  }
}
