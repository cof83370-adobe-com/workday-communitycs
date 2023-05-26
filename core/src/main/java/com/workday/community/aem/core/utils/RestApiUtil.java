package com.workday.community.aem.core.utils;

import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.exceptions.SnapException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
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
  private static final Logger logger = LoggerFactory.getLogger(RestApiUtil.class);

  /**
   * Request of Common nav menu.
   *
   * @param url      url of menu API
   * @param apiToken api token
   * @param apiKey   apiKey
   * @param traceId  traceId
   *
   * @return the API repsonse from menu API call
   */
  public static APIResponse doGetMenu(String url, String apiToken, String apiKey, String traceId) throws SnapException {
    // Construct the request header.
    APIRequest req = getMenuApiRequest(url, apiToken, apiKey, traceId);

    return executeGetRequest(req);
  }

  /**
   *
   * @param url       URL of the API endpoint.
   * @param authToken Photo API token.
   * @param xapiKey   API secret key.
   * @return the Json response as String from snap logic API call.
   */
  public static String doSnapGet(String url, String authToken, String xapiKey) throws SnapException {
    logger.debug("RestAPIUtil: Calling REST requestSnapJsonResponse()...= {}", url);
    APIRequest apiRequestInfo = new APIRequest();

    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(authToken))
        .addHeader(RestApiConstants.X_API_KEY, xapiKey);
    return executeGetRequest(apiRequestInfo).getResponseBody();
  }

  private static APIResponse executeGetRequest(APIRequest req) throws SnapException {
    APIResponse apiresponse = new APIResponse();

    logger.debug("RESTAPIUtil: Calling REST executeGetRequest().");
    if (StringUtils.isBlank(req.getMethod())) {
      req.setMethod(RestApiConstants.GET_API);
    }

    // Client with connection pool reused for all requests.
    RequestConfig config = RequestConfig.custom().setConnectTimeout(HTTP_TIMEMOUT)
        .setConnectionRequestTimeout(HTTP_TIMEMOUT)
        .setSocketTimeout(HTTP_TIMEMOUT).build();

    try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
      URIBuilder builder = new URIBuilder(req.getUri());
      HttpGet request = new HttpGet(builder.build());
      for (Map.Entry<String, String> entry : req.getHeaders().entrySet()) {
        request.setHeader(entry.getKey(), entry.getValue());
      }

      // Send the HttpGet request using the configured HttpClient
      CloseableHttpResponse response = httpclient.execute(request);

      logger.debug("HTTP response code : {}", response.getStatusLine().getStatusCode());
      String responseStr = EntityUtils.toString(response.getEntity());
      logger.debug("HTTP response : {}", responseStr);
      apiresponse.setResponseCode(response.getStatusLine().getStatusCode());
      apiresponse.setResponseBody(responseStr);
    } catch (IOException | URISyntaxException e) {
      throw new SnapException(
          String.format("Exception in executeGetRequest method while executing the request = %s", e.getMessage()));
    }
    return apiresponse;
  }

  private static APIRequest getMenuApiRequest(String url, String authToken, String xapiKey, String traceId) {
    APIRequest apiRequestInfo = new APIRequest();

    apiRequestInfo.setUrl(url);

    apiRequestInfo.addHeader(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(authToken))
        .addHeader(HttpConstants.HEADER_ACCEPT, RestApiConstants.APPLICATION_SLASH_JSON)
        .addHeader(RestApiConstants.CONTENT_TYPE, RestApiConstants.APPLICATION_SLASH_JSON)
        .addHeader(RestApiConstants.X_API_KEY, xapiKey)
        .addHeader(RestApiConstants.TRACE_ID, traceId);

    return apiRequestInfo;
  }
}
