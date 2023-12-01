package com.workday.community.aem.utils;

import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junitx.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import javax.servlet.http.Cookie;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;

@ExtendWith({MockitoExtension.class})
public class HttpUtilsTest {
  private final Cookie testCookie = new Cookie("test", "test");

  private SlingHttpServletRequest request;

  private SlingHttpServletResponse response;

  private UserService userService;

  @BeforeEach
  public void setup() throws IOException {
    this.request = mock(SlingHttpServletRequest.class);
    this.response = mock(SlingHttpServletResponse.class);
    this.userService = mock(UserService.class);

    Cookie[] cookies = new Cookie[] {new Cookie("test", "testValue"),
        new Cookie("testName", "testValue1"), new Cookie(EMAIL_NAME, "community@workday.com")};
    lenient().when(request.getCookies()).thenReturn(cookies);
    lenient().when(response.getWriter()).thenReturn(mock(PrintWriter.class));
  }

  @Test
  public void testGetCookie() {
    Cookie cookie = HttpUtils.getCookie(request, "testName");
    assertNotNull(cookie);
    assertEquals("testValue1", cookie.getValue());
  }

  @Test
  public void testSetCookie() {
    HttpUtils.setCookie(testCookie, response, true, 12, "/", true);
    verify(response).addCookie(testCookie);
  }

  @Test
  public void testDropCookies() {
    int count = HttpUtils.dropCookies(request, response, "/", new String[] {"test", "testName"});
    assertEquals(2, count);
  }

  @Test
  public void testForbiddenResponseSuccessful() throws IOException {
    boolean ret = HttpUtils.forbiddenResponse(request, response, userService);
    verify(response, times((1))).getWriter();
    assertTrue(ret);
  }

  @Test
  public void testForbiddenResponseException() throws IOException, CacheException {
    lenient().when(userService.getCurrentUser(request)).thenThrow(new CacheException("test failed"));
    boolean ret = HttpUtils.forbiddenResponse(request, response, userService);
    verify(response, times((1))).getWriter();
    assertFalse(ret);
  }
}
