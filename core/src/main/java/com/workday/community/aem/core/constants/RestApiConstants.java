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
  String GET_API = "GET";
}
