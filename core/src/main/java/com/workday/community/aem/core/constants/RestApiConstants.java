package com.workday.community.aem.core.constants;

import com.workday.community.aem.core.constants.lambda.BearerToken;

/**
 * Interface defining REST API constants.
 */
public interface RestApiConstants {

  /**
   * Returns a bearer token header value.
   */
  BearerToken BEARER_TOKEN = (token) -> String.format("Bearer %s", token);

  /**
   * The name of the API key header.
   */
  String X_API_KEY = "X-api-key";

  /**
   * The name of the Amazon trace ID header.
   */
  String TRACE_ID = "X-Amzn-Trace-Id";

  /**
   * String value of GET request method.
   */
  String GET_API = org.apache.sling.api.servlets.HttpConstants.METHOD_GET;

  /**
   * The timeout length for REST requests.
   */
  int TIMEOUT = 10000;

  /**
   * Basic Authorization header.
   */
  String BASIC = "Basic";

  /**
   * Grant type Oauth parameter.
   */
  String GRANT_TYPE = "grant_type";

  /**
   * Refresh token Oauth parameter.
   */
  String REFRESH_TOKEN = "refresh_token";
}
