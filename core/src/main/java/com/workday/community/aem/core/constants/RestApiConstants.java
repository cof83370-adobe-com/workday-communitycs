package com.workday.community.aem.core.constants;

import com.workday.community.aem.core.constants.lambda.BearerToken;
import org.apache.oltu.oauth2.common.OAuth;

/**
 * Interface defining REST API constants.
 */
public interface RestApiConstants {

  /**
   * Returns a bearer token header value.
   */
  BearerToken BEARER_TOKEN = (token) -> String.format("%s %s", OAuth.OAUTH_HEADER_NAME, token);

  /**
   * The name of the API key header.
   */
  String X_API_KEY = "X-api-key";

  /**
   * The name of the Amazon trace ID header.
   */
  String TRACE_ID = "X-Amzn-Trace-Id";

  /**
   * Basic Authorization header.
   */
  String BASIC = "Basic";

  /**
   * The constant CLIENT_CREDENTIALS.
   */
  String CLIENT_CREDENTIALS = "client_credentials";
}
