package com.workday.community.aem.core.utils;

import static com.workday.community.aem.core.constants.GlobalConstants.REST_API_UTIL_MESSAGE;
import static com.workday.community.aem.core.constants.HttpConstants.HTTP_TIMEMOUT;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.RestApiConstants.CLIENT_CREDENTIALS;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.dto.AemContentDto;
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
      return executeGetRequest(req);
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
      return executeGetRequest(apiRequestInfo).getResponseBody();
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
  private static ApiResponse executeGetRequest(ApiRequest req) throws RestException {
    req.setMethod(HttpConstants.METHOD_GET);
    return executeRequest(req);
  }

  /**
   * Executes the Delete request call.
   *
   * @param req API request.
   * @return Response from API.
   * @throws RestException APIException object.
   */
  private static ApiResponse executeDeleteRequest(ApiRequest req) throws RestException {
    req.setMethod(HttpConstants.METHOD_DELETE);
    return executeRequest(req);
  }

  /**
   * Executes the request call.
   *
   * @param req API request.

   * @return Response from API.
   * @throws RestException APIException object.
   */
  private static ApiResponse executeRequest(ApiRequest req) throws RestException {
    String method = req.getMethod();
    if (StringUtils.isBlank(method)) {
      method = HttpConstants.METHOD_GET;
    }
    log.debug("RESTAPIUtil executeRequest: Calling REST executeRequest() for method: {}", method);
    HttpClient httpclient =
        HttpClient.newBuilder().connectTimeout(Duration.ofMillis(HTTP_TIMEMOUT)).build();
    log.debug("RestAPIUtil executeRequest: method:{}, uri: {}", method, req.getUri().toString());

    Builder builder = HttpRequest.newBuilder().uri(req.getUri());

    // Add the headers.
    for (Map.Entry<String, String> entry : req.getHeaders().entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }

    // Build the request.
    HttpRequest request = method.equalsIgnoreCase(HttpConstants.METHOD_DELETE) ? builder.DELETE().build() :
        builder.GET().build();

    HttpResponse<String> response;
    try {
      // Send the request using the configured HttpClient.
      response = httpclient.send(request, BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw new RestException("Exception in executeRequest method while executing the request: %s", e.getMessage());
    }

    int statusCode = response.statusCode();
    String body = response.body();
    log.debug("HTTP response {}, code : {}", statusCode, body);

    ApiResponse apiResponse = new ApiResponse();
    apiResponse.setResponseCode(statusCode);
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
      apiResponse.setResponseBody(body);
      log.debug("Sending the Response for GET call");
    } else if (statusCode == HttpStatus.SC_NO_CONTENT) {
      apiResponse.setResponseBody(body);
      log.debug("Sending the Response for DELETE call");
    } else {
      throw new RestException("Error returned from %s request call: status: %s, response body: %s",
          method, statusCode, body);
    }

    return apiResponse;
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
    HttpRequest.BodyPublisher bodyPublisher = buildFormDataFromMap(request.getFormData());
    return executePostRequest(request, bodyPublisher);
  }


  /**
   * Executes the post request using the java net Httpclient.
   *
   * @param request API Request object
   * @return API Response object
   * @throws RestException APIException object.
   */
  private static ApiResponse executePostRequest(ApiRequest request, HttpRequest.BodyPublisher bodyPublisher)
      throws RestException {
    ApiResponse apiresponse = new ApiResponse();

    log.debug("RESTAPIUtil executePostRequest: Calling REST executePostRequest().");

    HttpClient httpClient =
        HttpClient.newBuilder().connectTimeout(Duration.ofMillis(HTTP_TIMEMOUT)).build();

    Builder builder = HttpRequest.newBuilder().uri(request.getUri());

    // Add the headers.
    for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }

    try {
      // Build the request.
      HttpRequest httpRequest = builder.POST(bodyPublisher).build();

      HttpResponse<String> response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      int resCode = response.statusCode();
      String resBody = response.body();
      if (resCode != HttpStatus.SC_OK && resCode != HttpStatus.SC_CREATED) {
        log.error("HTTP response {}, code : {}", resBody, resCode);
      }
      apiresponse.setResponseCode(resCode);
      apiresponse.setResponseBody(resBody);
    } catch (IOException | InterruptedException e) {
      throw new RestException("Exception in executePostRequest method while executing the request = %s",
          e.getMessage());
    }

    return apiresponse;
  }

  /**
   * Executes the post request using the java net Httpclient.
   *
   * @param request API Request object
   * @return API Response object
   * @throws RestException APIException object.
   */
  private static ApiResponse executeAemContentEntityPostRequest(ApiRequest request, AemContentDto aemContentDto)
      throws RestException {
    log.debug("RESTAPIUtil executePostRequest: Calling REST executePostRequest().");

    try {
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = mapper.writeValueAsString(aemContentDto);

      HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);
      return executePostRequest(request, bodyPublisher);
    } catch (JsonProcessingException e) {
      throw new RestException("Exception while processing JSON: %s", e.getMessage());
    }
  }

  /**
   * Returns the basic authentication header.
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

      return executeGetRequest(apiRequestInfo);
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
   * Frames the request and gets the response.
   *
   * @param url          Url
   * @return API Response
   * @throws DrupalException DrupalException object.
   */
  public static ApiResponse doDrupalCsrfTokenGet(String url) throws DrupalException {
    try {
      // Construct the request header.
      log.debug("RestAPIUtil: Calling REST doDrupalTokenGet()...= {}", url);
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(CONTENT_TYPE, org.apache.http.client.utils.URLEncodedUtils.CONTENT_TYPE);
      return executePostRequest(apiRequestInfo);
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
  public static ApiResponse doDrupalUserDataGet(String url, String bearerToken) throws DrupalException {
    try {
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
          .addHeader(CONTENT_TYPE, ContentType.JSON);

      return executeGetRequest(apiRequestInfo);
    } catch (RestException e) {
      throw new DrupalException(
          String.format("Exception in doDrupalUserDataGet method while executing the request = %s", e.getMessage()));
    }
  }

  /**
   * Frames the Drupal API user search get request.
   *
   * @param url         Url
   * @param bearerToken Bearer Token
   * @return API Response
   * @throws DrupalException DrupalException object.
   */
  public static ApiResponse doDrupalUserSearchGet(String url, String bearerToken) throws DrupalException {
    try {
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
          .addHeader(CONTENT_TYPE, ContentType.JSON);

      return executeGetRequest(apiRequestInfo);
    } catch (RestException e) {
      throw new DrupalException(
          String.format(REST_API_UTIL_MESSAGE, e.getMessage()));
    }
  }

  /**
   * Frames the Drupal API user search get request.
   *
   * @param url         Url
   * @param bearerToken Bearer Token
   * @return API Response
   * @throws DrupalException DrupalException object.
   */
  public static ApiResponse doDrupalCreateOrUpdateEntity(String url, AemContentDto aemContentDto,
                                                         String bearerToken, String csrfToken) throws DrupalException {
    try {
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
          .addHeader(CONTENT_TYPE, ContentType.JSON)
          .addHeader(GlobalConstants.X_CSRF_TOKEN, csrfToken);

      return executeAemContentEntityPostRequest(apiRequestInfo, aemContentDto);
    } catch (RestException e) {
      throw new DrupalException(
          String.format(REST_API_UTIL_MESSAGE, e.getMessage()));
    }
  }

  /**
   * Frames the Drupal API user search get request.
   *
   * @param url         Url
   * @param bearerToken Bearer Token
   * @param csrfToken   Url
   * @param pagePath    pagePath
   * @return API Response
   * @throws DrupalException DrupalException object.
   */
  public static ApiResponse doDrupalDeleteEntity(String url, String bearerToken, String csrfToken, String pagePath)
      throws DrupalException {
    try {
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpConstants.HEADER_ACCEPT, ContentType.JSON)
          .addHeader(CONTENT_TYPE, ContentType.JSON)
          .addHeader(GlobalConstants.X_CSRF_TOKEN, csrfToken)
          .addHeader(GlobalConstants.X_AEM_IDENTIFIER, pagePath);

      return executeDeleteRequest(apiRequestInfo);
    } catch (RestException e) {
      throw new DrupalException(
          String.format(REST_API_UTIL_MESSAGE, e.getMessage()));
    }
  }

}
