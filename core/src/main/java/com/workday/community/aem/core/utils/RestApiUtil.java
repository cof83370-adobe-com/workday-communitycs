package com.workday.community.aem.core.utils;

import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.exceptions.SnapException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.servlets.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.pojos.restclient.APIRequest;
import com.workday.community.aem.core.pojos.restclient.APIResponse;

import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.HttpConstants.HTTP_TIMEMOUT;

/**
 * The Class RESTAPIUtil.
 */
public class RestApiUtil {

  /**
   * The Constant logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RestApiUtil.class);

  /**
   * Request of Common nav menu.
   *
   * @param url      url of menu API
   * @param apiToken api token
   * @param apiKey   apiKey
   * @param traceId  traceId
   *
   * @return the API response from menu API call
   */
  public static APIResponse doMenuGet(String url, String apiToken, String apiKey, String traceId) throws SnapException {
    // Construct the request header.
    LOGGER.debug("RestAPIUtil: Calling REST doMenuGet()...= {}", url);
    APIRequest req = getMenuApiRequest(url, apiToken, apiKey, traceId);
    return executeGetRequest(req);
  }

  /**
   * A generic Request of snap api call.
   *
   * @param url       URL of the API endpoint.
   * @param authToken Photo API token.
   * @param xApiKey   API secret key.
   * @return the Json response as String from snap logic API call.
   */
  public static String doSnapGet(String url, String authToken, String xApiKey) throws SnapException {
    LOGGER.debug("RestAPIUtil: Calling REST requestSnapJsonResponse()...= {}", url);
    APIRequest apiRequestInfo = new APIRequest();

    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(authToken))
        .addHeader(RestApiConstants.X_API_KEY, xApiKey);
    return executeGetRequest(apiRequestInfo).getResponseBody();
  }

  /**
   * Executes the get request call.
   * 
   * @param req API request.
   * @return Response from API.
   * @throws SnapException
   */
  private static APIResponse executeGetRequest(APIRequest req) throws SnapException {
    APIResponse apiresponse = new APIResponse();

    LOGGER.debug("RESTAPIUtil executeGetRequest: Calling REST executeGetRequest().");
    if (StringUtils.isBlank(req.getMethod())) {
      req.setMethod(RestApiConstants.GET_API);
    }

    HttpClient httpclient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(HTTP_TIMEMOUT)).build();
    LOGGER.debug("RestAPIUtil executeGetRequest: req.getMethod():{}, {}", req.getMethod(), req.getUri().toString());

    Builder builder = HttpRequest.newBuilder().uri(req.getUri());

    // Add the headers.
    for (Map.Entry<String, String> entry : req.getHeaders().entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }

    // Build the request.
    HttpRequest request = builder.GET().build();

    HttpResponse<String> response = null;
    try {
      // Send the HttpGet request using the configured HttpClient.
      response = httpclient.send(request, BodyHandlers.ofString());
      LOGGER.debug("HTTP response code : {}", response.statusCode());
      apiresponse.setResponseCode(response.statusCode());
      LOGGER.debug("HTTP response : {}", response.body());
      apiresponse.setResponseBody(response.body());
    } catch (IOException | InterruptedException e) {
      throw new SnapException(
          String.format("Exception in executeGetRequest method while executing the request = %s", e.getMessage()));
    }
    return apiresponse;
  }

  /**
   * Frames the nav menu API request object.
   * 
   * @param url       Request URL.
   * @param authToken Auth token.
   * @param xApiKey   API key.
   * @param traceId   Trace Id in header.
   * @return API Request object.
   */
  private static APIRequest getMenuApiRequest(String url, String authToken, String xApiKey, String traceId) {
    APIRequest apiRequestInfo = new APIRequest();

    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(authToken))
        .addHeader(HttpConstants.HEADER_ACCEPT, RestApiConstants.APPLICATION_SLASH_JSON)
        .addHeader(RestApiConstants.CONTENT_TYPE, RestApiConstants.APPLICATION_SLASH_JSON)
        .addHeader(RestApiConstants.X_API_KEY, xApiKey)
        .addHeader(RestApiConstants.TRACE_ID, traceId);

    return apiRequestInfo;
  }
}
