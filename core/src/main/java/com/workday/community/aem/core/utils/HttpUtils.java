package com.workday.community.aem.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.adobe.acs.commons.util.CookieUtil.addCookie;

/**
 * Utility class for Http request/response related code.
 */
public class HttpUtils {
  private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

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

    logger.debug(String.format("There is no value for %s", cookieName));
    return null;
  }

  /**
   *
   * @param cookie The Cookie object.
   * @param response The http servlet response object.
   * @param isHttpOnly If request is httpOnly request.
   * @param expiryTime The token required expiry time.
   * @param path  The current request path.
   * @param secure If the cooke should be secured boolean.
   */
  public static void setCookie(final Cookie cookie, final HttpServletResponse response, boolean isHttpOnly,
                               int expiryTime, String path, boolean secure) {
    cookie.setMaxAge(expiryTime);
    cookie.setHttpOnly(isHttpOnly);
    cookie.setPath((null == path) ? "/" : path);
    cookie.setSecure(secure);
    addCookie(cookie, response);
  }
}
