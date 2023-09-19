package com.workday.community.aem.core.constants;

import com.workday.community.aem.core.constants.lambda.BearerToken;
import org.apache.oltu.oauth2.common.OAuth;

/**
 * The Class RESTAPIConstants.
 */
public interface RestApiConstants {
  /**
   * The constant BEARER_TOKEN
   */
  BearerToken BEARER_TOKEN = (token) -> String.format("%s %s", OAuth.OAUTH_HEADER_NAME, token);

  /**
   * The constant X_API_KEY
   */
  String X_API_KEY = "X-api-key";

  /**
   * The constant TRACE_ID
   */
  String TRACE_ID = "X-Amzn-Trace-Id";

  /**
   * The constant Basic
   */
  String BASIC = "Basic";

  /**
   * The constant CLIENT_CREDENTIALS
   */
  String CLIENT_CREDENTIALS = "client_credentials";
}
