package com.workday.community.aem.core.utils;

import static com.workday.community.aem.core.constants.HttpConstants.HTTP_TIMEMOUT;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;

import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.exceptions.ApiException;
import com.workday.community.aem.core.exceptions.LmsException;
import com.workday.community.aem.core.exceptions.OurmException;
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
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

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
    } catch (ApiException e) {
      throw new SnapException(
          String.format("Exception in doMenuGet method while executing the request = %s",
              e.getMessage()));
    }
  }

  /**
   * A generic Request of snap api call.
   *
   * @param url       URL of the API endpoint.
   * @param authToken Photo API token.
   * @param apiKey   API secret key.
   * @return the Json response as String from snap logic API call.
   * @throws SnapException SnapException object.
   */
  public static String doSnapGet(String url, String authToken, String apiKey)
      throws SnapException {
    try {
      log.debug("RestAPIUtil: Calling REST requestSnapJsonResponse()...= {}", url);
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(authToken))
          .addHeader(RestApiConstants.X_API_KEY, apiKey);
      return executeGetRequest(apiRequestInfo).getResponseBody();
    } catch (ApiException e) {
      throw new SnapException(
          String.format("Exception in doSnapGet method while executing the request = %s",
              e.getMessage()));
    }
  }

  /**
   * A generic Request of an OURM api call.
   *
   * @param url URL of the API endpoint.
   * @param header The header.
   *
   * @return The response from the OURM endpoint.
   *
   * @throws OurmException If there is an error with the request.
   */
  public static String doOurmGet(String url, String header) throws OurmException {
    try {
      log.debug("RestAPIUtil: Calling REST requestOurmJsonResponse()...= {}", url);
      ApiRequest apiRequestInfo = new ApiRequest();

      apiRequestInfo.setUrl(url);
      apiRequestInfo.addHeader(HttpHeaders.AUTHORIZATION, header);
      return executeGetRequest(apiRequestInfo).getResponseBody();
    } catch (ApiException e) {
      throw new OurmException(
          String.format("Exception in doOURMGet method while executing the request = %s",
              e.getMessage()));
    }
  }

  /**
   * Executes the get request call.
   *
   * @param req API request.
   * @return Response from API.
   * @throws ApiException APIException object.
   */
  private static ApiResponse executeGetRequest(ApiRequest req) throws ApiException {
    log.debug("RESTAPIUtil executeGetRequest: Calling REST executeGetRequest().");
    if (StringUtils.isBlank(req.getMethod())) {
      req.setMethod(RestApiConstants.GET_API);
    }

    HttpClient httpclient =
        HttpClient.newBuilder().connectTimeout(Duration.ofMillis(HTTP_TIMEMOUT)).build();
    log.debug("RestAPIUtil executeGetRequest: req.getMethod():{}, {}", req.getMethod(),
        req.getUri().toString());

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
      throw new ApiException(
          String.format("Exception in executeGetRequest method while executing the request = %s",
              e.getMessage()));
    }

    int statusCode = response.statusCode();
    log.debug("HTTP response code : {}", statusCode);
    log.debug("HTTP response : {}", response.body());

    ApiResponse apiresponse = new ApiResponse();
    apiresponse.setResponseCode(response.statusCode());
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
      apiresponse.setResponseBody(response.body());
    } else {
      apiresponse.setResponseBody(null);
    }

    return apiresponse;
  }

  /**
   * Frames the nav menu API request object.
   *
   * @param url       Request URL.
   * @param authToken Auth token.
   * @param apiKey   API key.
   * @param traceId   Trace id in header.
   * @return API Request object.
   */
  private static ApiRequest getMenuApiRequest(String url, String authToken, String apiKey,
                                              String traceId) {
    ApiRequest apiRequestInfo = new ApiRequest();

    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(authToken))
        .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
        .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
        .addHeader(RestApiConstants.X_API_KEY, apiKey)
        .addHeader(RestApiConstants.TRACE_ID, traceId);

    return apiRequestInfo;
  }

  /**
   * Executes the post request using the java net Httpclient.
   *
   * @param request API Request object
   * @return API Response object
   * @throws ApiException APIException object.
   */
  private static ApiResponse executePostRequest(ApiRequest request) throws ApiException {
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
    HttpRequest httpRequest = builder.POST(buildFormDataFromMap(request.getFormData())).build();

    try {
      HttpResponse<String> response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      int resCode = response.statusCode();
      String resBody = response.body();
      if (resCode != HttpStatus.SC_OK && resCode != HttpStatus.SC_CREATED) {
        log.debug("HTTP response code : {}", resCode);
        log.debug("HTTP response : {}", resBody);
      }
      apiresponse.setResponseCode(resCode);
      apiresponse.setResponseBody(resBody);
    } catch (IOException | InterruptedException e) {
      throw new ApiException(
          String.format("Exception in executePostRequest method while executing the request = %s",
              e.getMessage()));
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
   * Builds the form data for post call.
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
    apiRequestInfo.addHeader(HttpHeaders.AUTHORIZATION,
            getBasicAuthenticationHeader(username, password))
        .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
        .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
        .addFormData(RestApiConstants.GRANT_TYPE, RestApiConstants.REFRESH_TOKEN)
        .addFormData(RestApiConstants.REFRESH_TOKEN, refreshToken);

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
    } catch (ApiException e) {
      throw new LmsException(
          String.format("Exception in doLmsTokenGet method while executing the request = %s",
              e.getMessage()));
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
      apiRequestInfo.addHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(bearerToken))
          .addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())
          .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

      return executeGetRequest(apiRequestInfo);
    } catch (ApiException e) {
      throw new LmsException(
          String.format("Exception in doLmsCourseDetailGet method while executing the request = %s",
              e.getMessage()));
    }
  }
}
