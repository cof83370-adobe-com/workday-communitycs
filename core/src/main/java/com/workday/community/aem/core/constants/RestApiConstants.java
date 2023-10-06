package com.workday.community.aem.core.constants;

import com.workday.community.aem.core.constants.lambda.BearerToken;

/**
 * The Class RESTAPIConstants.
 */
public interface RestApiConstants {

  /**
   * The constant BEARER_TOKEN.
   */
  BearerToken BEARER_TOKEN = (token) -> String.format("Bearer %s", token);

  /**
   * The constant X_API_KEY.
   */
  String X_API_KEY = "X-api-key";

  /**
   * The constant TRACE_ID.
   */
  String TRACE_ID = "X-Amzn-Trace-Id";

  /**
   * The constant GET_API.
   */
  String GET_API = org.apache.sling.api.servlets.HttpConstants.METHOD_GET;

  /**
   * The constant TIMEOUT.
   */
  int TIMEOUT = 10000;

  /**
   * The constant Basic.
   */
  String BASIC = "Basic";

  /**
   * The constant GRANT_TYPE.
   */
  String GRANT_TYPE = "grant_type";

  /**
   * The constant REFRESH_TOKEN.
   */
  String REFRESH_TOKEN = "refresh_token";
}
