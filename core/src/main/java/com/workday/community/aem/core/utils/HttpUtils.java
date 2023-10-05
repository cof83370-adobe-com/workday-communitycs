package com.workday.community.aem.core.utils;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Http request/response related code.
 */
public class HttpUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

  /**
   * Get cookie from the http servlet request object given the cookie name.
   *
   * @param request    The http servlet request object.
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

    LOGGER.debug(String.format("There is no value for %s", cookieName));
    return null;
  }

  /**
   * Set Cookie.
   *
   * @param cookie     The Cookie object.
   * @param response   The http servlet response object.
   * @param isHttpOnly If request is httpOnly request.
   * @param expiryTime The token required expiry time.
   * @param path       The current request path.
   * @param secure     If the cooke should be secured boolean.
   */
  public static void setCookie(final Cookie cookie, final HttpServletResponse response,
                               boolean isHttpOnly,
                               int expiryTime, String path, boolean secure) {
    cookie.setMaxAge(expiryTime);
    cookie.setHttpOnly(isHttpOnly);
    cookie.setPath((null == path) ? "/" : path);
    cookie.setSecure(secure);
    addCookie(cookie, response);
  }

  /**
   * Add the provided HTTP Cookie to the Response.
   *
   * @param cookie   Cookie to add
   * @param response Response to add Cookie to
   */
  public static void addCookie(final Cookie cookie, final HttpServletResponse response) {
    if (cookie == null || response == null) {
      return;
    }

    response.addCookie(cookie);
  }

  /**
   * Clear all cookies.
   *
   * @param request    The HttpServletRequest object.
   * @param response   The HttpServletResponse object.
   * @param cookiePath The Cookie path.
   * @param deleteList the list of cookie names to be deleted.
   * @return the number of cookies being dropped.
   */
  public static int dropCookies(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final String cookiePath, String[] deleteList) {
    final Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return 0;
    }

    int count = 0;
    boolean hasDeleteList = deleteList != null && deleteList.length > 0;
    if (hasDeleteList) {
      List<String> deleteNames = Arrays.asList(deleteList);

      for (final Cookie cookie : cookies) {
        if (deleteNames.contains(cookie.getName())) {
          final Cookie responseCookie = (Cookie) cookie.clone();
          responseCookie.setMaxAge(0);
          responseCookie.setPath(cookiePath);
          responseCookie.setValue("");

          addCookie(responseCookie, response);
          count++;
        }
      }
    }

    return count;
  }
}
