package com.workday.community.aem.core.utils;

import static com.workday.community.aem.core.constants.HttpConstants.HTTP_TIMEMOUT;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.RestApiConstants.CLIENT_CREDENTIALS;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.sling.api.servlets.HttpConstants.METHOD_POST;

import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.exceptions.LmsException;
import com.workday.community.aem.core.exceptions.RestException;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.pojos.restclient.ApiRequest;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuth.ContentType;
import org.apache.sling.api.servlets.HttpConstants;

/**
 * The Class RESTAPIUtil.
 */
@Slf4j
public class RestApiUtil {

  /**
   * Request of Common nav menu.
   *
   * @param url      url of menu API
   * @param apiToken api token
   * @param apiKey   apiKey
   * @param traceId  traceId
   * @return the API response from menu API call
   * @throws SnapException SnapException object.
   */
  public static ApiResponse doMenuGet(String url, String apiToken, String apiKey, String traceId)
      throws SnapException {
    try {
      // Construct the request header.
      log.debug("RestAPIUtil: Calling REST doMenuGet()...= {}", url);
      ApiRequest req = getMenuApiRequest(url, apiToken, apiKey, traceId);
      return executeRequest(req);
    } catch (RestException e) {
      throw new SnapException("Exception in doMenuGet method while executing the request = %s", e.getMessage());
    }
  }

  /**
   * A generic Request of snap api call.
   *
   * @param url       URL of the API endpoint.
   * @param authToken Photo API token.
   * @param apiKey    API secret key.
   * @return the Json response as String from snap logic API call.
   * @throws SnapException SnapException object.
   */
  public static String doSnapGet(String url, String authToken, String apiKey)
      throws SnapException {
    try {
      log.debug("RestAPIUtil: Calling REST requestSnapJsonResponse()...= {}", url);
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(authToken))
          .addHeader(RestApiConstants.X_API_KEY, apiKey);
      return executeRequest(apiRequestInfo).getResponseBody();
    } catch (RestException e) {
      throw new SnapException("Exception in doSnapGet method while executing the request = %s", e.getMessage());
    }
  }

  /**
   * Executes the get request call.
   *
   * @param req API request.
   * @return Response from API.
   * @throws RestException APIException object.
   */
  private static ApiResponse executeRequest(ApiRequest req) throws RestException {
    log.debug("RESTAPIUtil executeGetRequest: Calling REST executeGetRequest().");
    if (StringUtils.isBlank(req.getMethod())) {
      req.setMethod(HttpConstants.METHOD_GET);
    }

    HttpClient httpclient =
        HttpClient.newBuilder().connectTimeout(Duration.ofMillis(HTTP_TIMEMOUT)).build();
    log.debug("RestAPIUtil executeGetRequest: method:{}, uri: {}", req.getMethod(), req.getUri().toString());

    Builder builder = HttpRequest.newBuilder().uri(req.getUri());

    // Add the headers.
    for (Map.Entry<String, String> entry : req.getHeaders().entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }

    // Build the request.
    HttpRequest request = builder.GET().build();
    HttpResponse<String> response;
    try {
      // Send the HttpGet request using the configured HttpClient.
      response = httpclient.send(request, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new RestException("Exception in executeGetRequest method while executing the request %s", e.getMessage());
    }

    int statusCode = response.statusCode();
    String body = response.body();
    log.debug("HTTP response {}, code : {}", body, statusCode);

    ApiResponse apiresponse = new ApiResponse();
    apiresponse.setResponseCode(response.statusCode());
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
      apiresponse.setResponseBody(response.body());
    } else {
      throw new RestException("Error return from Get request call: status: %s, response body; %s ", statusCode, body);
    }

    return apiresponse;
  }

  /**
   * Frames the nav menu API request object.
   *
   * @param url       Request URL.
   * @param authToken Auth token.
   * @param apiKey    API key.
   * @param traceId   Trace id in header.
   * @return API Request object.
   */
  private static ApiRequest getMenuApiRequest(String url, String authToken, String apiKey,
                                              String traceId) {
    ApiRequest apiRequestInfo = new ApiRequest();

    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(authToken))
        .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
        .addHeader(CONTENT_TYPE, ContentType.JSON)
        .addHeader(RestApiConstants.X_API_KEY, apiKey)
        .addHeader(RestApiConstants.TRACE_ID, traceId);

    return apiRequestInfo;
  }

  /**
   * Executes the post request using the java net Httpclient.
   *
   * @param request API Request object
   * @return API Response object
   * @throws RestException APIException object.
   */
  private static ApiResponse executePostRequest(ApiRequest request) throws RestException {
    ApiResponse apiresponse = new ApiResponse();

    log.debug("RESTAPIUtil executePostRequest: Calling REST executePostRequest().");

    HttpClient httpClient =
        HttpClient.newBuilder().connectTimeout(Duration.ofMillis(HTTP_TIMEMOUT)).build();

    Builder builder = HttpRequest.newBuilder().uri(request.getUri());

    // Add the headers.
    for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }

    // Build the request.
    HttpRequest.BodyPublisher bodyPublisher = null;
    String payload = request.getBody();
    if (!StringUtils.isEmpty(payload)) {
      bodyPublisher = buildFormDataFromPayload(payload);
    } else {
      Map<String, String> formData = request.getFormData();
      if (formData != null && !formData.isEmpty()) {
        bodyPublisher =  buildFormDataFromMap(formData);
      }
    }

    if (bodyPublisher != null) {
      HttpRequest httpRequest = builder.POST(bodyPublisher).build();

      try {
        HttpResponse<String> response =
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int resCode = response.statusCode();
        String resBody = response.body();
        if (resCode != HttpStatus.SC_OK && resCode != HttpStatus.SC_CREATED) {
          log.error("HTTP response {}, code : {}", resBody, resCode);
          return apiresponse;
        }
        apiresponse.setResponseCode(resCode);
        apiresponse.setResponseBody(resBody);
      } catch (IOException | InterruptedException e) {
        throw new RestException("Exception in executePostRequest method while executing the request = %s",
            e.getMessage());
      }
    }

    return apiresponse;
  }

  /**
   * Retruns the basic authentication header.
   *
   * @param username Username
   * @param password Password
   * @return Header string
   */
  private static String getBasicAuthenticationHeader(String username, String password) {
    String valueToEncode = username + ":" + password;
    return RestApiConstants.BASIC
        + " " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
  }

  /**
   * Builds the form url encoded data for post call.
   *
   * @param data Map with input data
   * @return Body publisher
   */
  private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> data) {
    var builder = new StringBuilder();
    for (Map.Entry<String, String> entry : data.entrySet()) {
      if (builder.length() > 0) {
        builder.append("&");
      }
      builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
      builder.append("=");
      builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
    }
    return HttpRequest.BodyPublishers.ofString(builder.toString());
  }

  private static HttpRequest.BodyPublisher buildFormDataFromPayload(String payload) {
    return HttpRequest.BodyPublishers.ofString(payload);
  }

  /**
   * Frames the Lms Token call request.
   *
   * @param url          Url
   * @param username     Client Id
   * @param password     Client Secret
   * @param refreshToken Refresh Token
   * @return API Request
   */
  private static ApiRequest getLmsTokenRequest(String url, String username, String password,
                                               String refreshToken) {
    ApiRequest apiRequestInfo = new ApiRequest();

    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(AUTHORIZATION, getBasicAuthenticationHeader(username, password))
        .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
        .addHeader(CONTENT_TYPE, ContentType.JSON)
        .addFormData(OAuth.OAUTH_GRANT_TYPE, OAuth.OAUTH_REFRESH_TOKEN)
        .addFormData(OAuth.OAUTH_REFRESH_TOKEN, refreshToken);

    return apiRequestInfo;
  }

  /**
   * Frames the request and gets the response.
   *
   * @param url          Url
   * @param clientId     Client Id
   * @param clientSecret Client Secret
   * @param refreshToken Refresh Token
   * @return API Response
   * @throws LmsException LmsException object.
   */
  public static ApiResponse doLmsTokenGet(String url, String clientId, String clientSecret,
                                          String refreshToken) throws LmsException {
    // Construct the request header.
    log.debug("RestAPIUtil: Calling REST doLmsTokenGet()...= {}", url);
    ApiRequest req = getLmsTokenRequest(url, clientId, clientSecret, refreshToken);

    try {
      return executePostRequest(req);
    } catch (RestException e) {
      throw new LmsException("Exception in doLmsTokenGet method while executing the request = %s",
              e.getMessage());
    }
  }

  /**
   * Frames the Lms Course Detail API request.
   *
   * @param url         Url
   * @param bearerToken Bearer Token
   * @return API Response
   * @throws LmsException LmsException object.
   */
  public static ApiResponse doLmsCourseDetailGet(String url, String bearerToken)
      throws LmsException {
    try {
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
          .addHeader(CONTENT_TYPE, ContentType.JSON);

      return executeRequest(apiRequestInfo);
    } catch (RestException e) {
      throw new LmsException("Exception in doLmsCourseDetailGet method while executing the request = %s",
              e.getMessage());
    }
  }

  /**
   * Frames the Drupal Token call request.
   *
   * @param url          Url
   * @param clientId     Client Id
   * @param clientSecret Client Secret
   * @return API Request
   */
  private static ApiRequest getDrupalTokenRequest(String url, String clientId, String clientSecret) {
    ApiRequest apiRequestInfo = new ApiRequest();

    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(CONTENT_TYPE, org.apache.http.client.utils.URLEncodedUtils.CONTENT_TYPE)
        .addFormData(OAuth.OAUTH_GRANT_TYPE, CLIENT_CREDENTIALS)
        .addFormData(OAuth.OAUTH_CLIENT_ID, clientId)
        .addFormData(OAuth.OAUTH_CLIENT_SECRET, clientSecret);

    return apiRequestInfo;
  }

  /**
   * Frames the request and gets the response.
   *
   * @param url          Url
   * @param clientId     Client Id
   * @param clientSecret Client Secret
   * @return API Response
   * @throws DrupalException DrupalException object.
   */
  public static ApiResponse doDrupalTokenGet(String url, String clientId, String clientSecret) throws DrupalException {
    try {
      // Construct the request header.
      log.debug("RestAPIUtil: Calling REST doDrupalTokenGet()...= {}", url);
      ApiRequest req = getDrupalTokenRequest(url, clientId, clientSecret);
      return executePostRequest(req);
    } catch (RestException e) {
      throw new DrupalException(
          String.format("Exception in doDrupalTokenGet method while executing the request = %s", e.getMessage()));
    }
  }

  /**
   * Frames the Drupal API user data get request.
   *
   * @param url         Url
   * @param bearerToken Bearer Token
   * @return API Response
   * @throws DrupalException DrupalException object.
   */
  public static ApiResponse doDrupalGet(String url, String bearerToken) throws DrupalException {
    try {
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
          .addHeader(CONTENT_TYPE, ContentType.JSON);

      return executeRequest(apiRequestInfo);
    } catch (RestException e) {
      throw new DrupalException(
          String.format("Exception in doDrupalUserDataGet method while executing the request = %s", e.getMessage()));
    }
  }

  /**
   * Do Drupal Post.
   *
   * @param url the pass-in url
   * @param bearerToken the pass-in bearer token
   * @param payload  the payload
   * @return an ApiResponse object
   * @throws DrupalException if drupal post fails.
   */
  public static ApiResponse doDrupalPost(String url, String bearerToken, String payload) throws DrupalException {
    try {
      ApiRequest apiRequestInfo = new ApiRequest();
      apiRequestInfo.setMethod(METHOD_POST);

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
          .addHeader(CONTENT_TYPE, ContentType.JSON);
      apiRequestInfo.setBody(payload);
      return executePostRequest(apiRequestInfo);
    } catch (RestException e) {
      throw new DrupalException(
          String.format("Exception in doDrupalUserDataGet method while executing the request = %s", e.getMessage()));
    }
  }
}
