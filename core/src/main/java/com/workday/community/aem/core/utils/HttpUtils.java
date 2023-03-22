package com.workday.community.aem.core.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.adobe.acs.commons.util.CookieUtil.addCookie;

/**
 * Utility class for Http request/response related code.
 */
public class HttpUtils {
  /**
   * Get cookie from the http servlet request object given the cookie name
   * @param request The http servlet request object.
   * @param cookieName The cookie name.
   * @return The Cookie object with the given cookie name
   */
  public static Cookie getCookie(final HttpServletRequest request, final String cookieName) {
    if (!StringUtils.isBlank(cookieName)) {
      final Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (final Cookie cookie : cookies) {
          if (StringUtils.equals(cookieName, cookie.getName())) {
            return cookie;
          }
        }
      }
    }

    return null;
  }

  public static void setCookie(final Cookie cookie, final HttpServletResponse response, boolean isHttpOnly,
                                  int expiryTime, String path) {
    if (path == null) {
      path = "/";
    }
    cookie.setMaxAge(expiryTime);
    cookie.setHttpOnly(isHttpOnly);
    cookie.setPath(path);
    cookie.setSecure(true);
    addCookie(cookie, response);
  }
}
