package com.workday.community.aem.core.utils;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.pojos.restclient.APIRequest;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import com.workday.community.aem.core.constants.GlobalConstants;

import static com.workday.community.aem.core.constants.GlobalConstants.WRCConstants.HTTP_TIMEMOUT;

/**
 * The Class RESTAPIUtil.
 */
public class RestApiUtil {

  /**
   * The Constant logger.
   */
  private static final Logger logger = LoggerFactory.getLogger(RestApiUtil.class);

  /**
   * Constructs the API request header.
   *
   * @param url       URL of the API endpoint.
   * @param authToken API token.
   * @param xapiKey   API secret key.
   * @return APIRequest API request object.
   */
  public static APIRequest constructHeader(String url, String authToken, String xapiKey, String traceId) {
    APIRequest apiRequestInfo = new APIRequest();
    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(GlobalConstants.RESTAPIConstants.AUTHORIZATION,
        GlobalConstants.RESTAPIConstants.BEARER_TYPE + authToken);
    apiRequestInfo.addHeader(GlobalConstants.RESTAPIConstants.ACCEPT_TYPE,
        GlobalConstants.RESTAPIConstants.APPLICATION_SLASH_JSON);
    apiRequestInfo.addHeader(GlobalConstants.RESTAPIConstants.CONTENT_TYPE,
        GlobalConstants.RESTAPIConstants.APPLICATION_SLASH_JSON);
    apiRequestInfo.addHeader(GlobalConstants.RESTAPIConstants.X_API_KEY, xapiKey);
    apiRequestInfo.addHeader(GlobalConstants.RESTAPIConstants.TRACE_ID, traceId);
    return apiRequestInfo;
  }

  /**
   * Request of method type GET.
   *
   * @param req Request object.
   * @return APIResponse Response object.
   * @throws IOException
   */
  public static APIResponse doGet(APIRequest req) throws IOException {
    req.setMethod(GlobalConstants.RESTAPIConstants.GET_API);
    return executeGetRequest(req);
  }

  /**
   *
   * @param url URL of the API endpoint.
   * @param authToken API token.
   * @param xapiKey API secret key.
   * @return the Json response as String from snap logic API call.
   * @throws IOException
   */
  public static String doSnapGet(String url, String authToken, String xapiKey) throws IOException {
    logger.debug("RestAPIUtil: Calling REST requestSnapJsonResponse()...= {}", url);

    APIRequest apiRequestInfo = new APIRequest();
    apiRequestInfo.setUrl(url);
    apiRequestInfo.addHeader(GlobalConstants.RESTAPIConstants.AUTHORIZATION, GlobalConstants.RESTAPIConstants.BEARER_TYPE + authToken);
    apiRequestInfo.addHeader(GlobalConstants.RESTAPIConstants.X_API_KEY, xapiKey);

    String snapJSONResp = executeGetRequest(apiRequestInfo).getResponseBody();

    return snapJSONResp;
  }

  private static APIResponse executeGetRequest(APIRequest req) throws IOException {
    APIResponse apiresponse = new APIResponse();

    logger.debug("RESTAPIUtil: Calling REST executeGetRequest().");
    if (StringUtils.isBlank(req.getMethod())) {
      req.setMethod("GET");
    }

    // Client with connection pool reused for all requests.
    RequestConfig config = RequestConfig.custom().setConnectTimeout(HTTP_TIMEMOUT)
        .setConnectionRequestTimeout(HTTP_TIMEMOUT)
        .setSocketTimeout(HTTP_TIMEMOUT).build();

    CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build();;
    try {
      URIBuilder builder = new URIBuilder(req.getUri());
      HttpGet request = new HttpGet(builder.build());
      for (Map.Entry<String, String> entry : req.getHeaders().entrySet()) {
        request.setHeader(entry.getKey(), entry.getValue());
      }
      // Send the HttpGet request using the configured HttpClient
      CloseableHttpResponse response = httpclient.execute(request);
      try {
        logger.debug("HTTP response code : {}", response.getStatusLine().getStatusCode());
        apiresponse.setResponseCode(response.getStatusLine().getStatusCode());
        apiresponse.setResponseBody(EntityUtils.toString(response.getEntity()));
      } catch (Exception e) {
        logger.error("Exception in executeGetRequest method while reading the response = {}", e.getMessage());
      } finally {
        response.close();
      }
    } catch (IOException | URISyntaxException e) {
      logger.error("Exception in executeGetRequest method while executing the request = {}", e.getMessage());
    } finally {
      httpclient.close();
    }
    return apiresponse;
  }
}
