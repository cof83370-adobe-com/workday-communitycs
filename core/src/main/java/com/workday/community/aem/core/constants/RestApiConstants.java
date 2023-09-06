package com.workday.community.aem.core.constants;

import com.workday.community.aem.core.constants.lambda.BearerToken;

/**
 * The Class RESTAPIConstants.
 */
public interface RestApiConstants {
  /**
   * The constant AUTHORIZATION
   */
  String AUTHORIZATION = "Authorization";

  /**
   * The constant BEARER_TOKEN
   */
  BearerToken BEARER_TOKEN = (token) -> String.format("Bearer %s", token);

  /**
   * The constant X_API_KEY
   */
  String X_API_KEY = "X-api-key";

  /**
   * The constant ACCEPT
   */
  String ACCEPT = "Accept";

  /**
   * The constant APPLICATION_SLASH_JSON
   */
  String STAR_SLASH_STAR = "*/*";

  /**
   * The constant CONTENT_TYPE
   */
  String CONTENT_TYPE = "Content-Type";

  /**
   * The constant APPLICATION_SLASH_JSON
   */
  String APPLICATION_SLASH_JSON = "application/json";

  /**
   * The constant TRACE_ID
   */
  String TRACE_ID = "X-Amzn-Trace-Id";

  /**
   * The constant GET_API
   */
  String GET_API = org.apache.sling.api.servlets.HttpConstants.METHOD_GET;

  /**
   * The constant TIMEOUT
   */
  int TIMEOUT = 10000;

  /**
   * The constant Basic
   */
  String BASIC = "Basic";

  /**
   * The constant GRANT_TYPE
   */
  String GRANT_TYPE = "grant_type";

  /**
   * The constant REFRESH_TOKEN
   */
  String REFRESH_TOKEN = "refresh_token";

  /**
   * The constant CLIENT_CREDENTIALS
   */
  String CLIENT_CREDENTIALS = "client_credentials";

  /**
   * The constant CLIENT_ID
   */
  String CLIENT_ID = "client_id";

  /**
   * The constant CLIENT_CREDENTIALS
   */
  String CLIENT_SECRET = "client_secret";

  /**
   * The constant APPLICATION_URL_ENCODED
   */
  String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
}
