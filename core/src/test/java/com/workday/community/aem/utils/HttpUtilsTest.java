package com.workday.community.aem.utils;

import com.workday.community.aem.core.utils.HttpUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static junit.framework.Assert.assertNotNull;
import static junitx.framework.Assert.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
public class HttpUtilsTest {
  private HttpServletRequest request;
  private HttpServletResponse response;

  private final Cookie testCookie = new Cookie("test", "test");

  @BeforeEach
  public void setup() {
    this.request = mock(HttpServletRequest.class);
    this.response = mock(HttpServletResponse.class);

    Cookie[] cookies = new Cookie[] {new Cookie("test", "testValue"),
    new Cookie("testName", "testValue1"), new Cookie(EMAIL_NAME, "community@workday.com")};
    lenient().when(request.getCookies()).thenReturn(cookies);
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
    int count = HttpUtils.dropCookies(request, response, "/");
    assertEquals(2, count);
  }
}
