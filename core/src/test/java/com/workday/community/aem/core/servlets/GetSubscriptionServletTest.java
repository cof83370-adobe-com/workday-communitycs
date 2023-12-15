package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.OurmUtils;
import com.workday.community.aem.core.utils.PageUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetSubscriptionServletTest {

  @Mock
  SearchApiConfigService searchApiConfigService;

  @Mock
  DrupalService drupalService;

  @Mock
  private UserService userService;

  @Mock
  private ResourceResolverFactory resourceResolverFactory;

  @InjectMocks
  private GetSubscriptionServlet getSubscriptionServlet;

  @Test
  void testDoGet() throws IOException, DrupalException, LoginException {
    try (MockedStatic<PageUtils> mockPageUtils = mockStatic(PageUtils.class);
         MockedStatic<OurmUtils> mockOurmUtils = mockStatic(OurmUtils.class)) {
      MockSlingHttpServletRequest request = mock(MockSlingHttpServletRequest.class);
      MockSlingHttpServletResponse response = mock(MockSlingHttpServletResponse.class);
      lenient().when(drupalService.isSubscribed(anyString(), anyString())).thenReturn(true);
      Enumeration<String> headers = mock(Enumeration.class);
      lenient().when(request.getHeaders("Referer")).thenReturn(headers);
      lenient().when(headers.nextElement()).thenReturn("/foo/foo.html");
      lenient().when(ResolverUtil.newResolver(this.resourceResolverFactory, READ_SERVICE_USER)).thenReturn(mock(ResourceResolver.class));
      mockPageUtils.when(() -> PageUtils.getPageUuid(any(), anyString())).thenReturn("fakeUUID");
      mockOurmUtils.when(() -> OurmUtils.getSalesForceId(request, userService)).thenReturn("fakeSFid");
      mockOurmUtils.when(() -> OurmUtils.getUserEmail("fakeSFid", searchApiConfigService, drupalService)).thenReturn("foo@workday.com");
      mockOurmUtils.when(
          () -> OurmUtils.getUserEmail("fakeUUID", searchApiConfigService, drupalService)
      ).thenReturn("foo@workday.com");

      PrintWriter pw = mock(PrintWriter.class);
      lenient().when(response.getWriter()).thenReturn(pw);

      getSubscriptionServlet.doGet(request, response);
      verify(response, times(1)).setStatus(HttpStatus.SC_OK);
    }
  }
}
