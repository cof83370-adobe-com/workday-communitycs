package com.workday.community.aem.core.utils;

import static com.workday.community.aem.core.constants.GlobalConstants.CLOUD_CONFIG_NULL_VALUE;
import static com.workday.community.aem.core.constants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTEXT_INFO_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Coveo functionality.
 */
public class CoveoUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CoveoUtils.class);

  /**
   * Executes a search.
   *
   * @param request The request object.
   * @param response The response object.
   * @param searchApiConfigService The search API config.
   * @param snapService The Snap logic service.
   * @param userService The user service.
   * @param gson The Gson object.
   * @param objectMapper The object mapper.
   * @param servletCallback The servlet callback.
   *
   * @throws ServletException If the user does not have an email address or no search token.
   * @throws IOException If the callback execution fails.
   */
  public static void executeSearchForCallback(SlingHttpServletRequest request,
                                              SlingHttpServletResponse response,
                                              SearchApiConfigService searchApiConfigService,
                                              SnapService snapService,
                                              UserService userService,
                                              Gson gson,
                                              ObjectMapper objectMapper,
                                              ServletCallback servletCallback)
      throws ServletException, IOException {
    String utfName = StandardCharsets.UTF_8.name();
    response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
    response.setCharacterEncoding(utfName);

    Cookie coveoCookie = HttpUtils.getCookie(request, COVEO_COOKIE_NAME);
    if (coveoCookie != null) {
      LOGGER.debug("Coveo cookie still active, decode it to send data back");
      String coveoInfo = URLDecoder.decode(coveoCookie.getValue(), utfName);
      servletCallback.execute(request, response, coveoInfo);
      return;
    }

    String sfId = OurmUtils.getSalesForceId(request, userService);
    int tokenExpiryTime = searchApiConfigService.getTokenValidTime() / 1000;
    boolean isDevMode = searchApiConfigService.isDevMode();
    JsonObject userContext = snapService.getUserContext(sfId);
    String email;
    if (isDevMode) {
      LOGGER.debug("dev mode is enabled");
      email = searchApiConfigService.getDefaultEmail();
    } else {
      email = userContext.has(EMAIL_NAME) ? userContext.get(EMAIL_NAME).getAsString() : null;
    }
    if (StringUtils.isEmpty(email)) {
      throw new ServletException("Email for current user is empty, ");
    }

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      String searchToken = getSearchToken(searchApiConfigService, httpClient, gson, objectMapper,
          email, searchApiConfigService.getSearchTokenApiKey());
      if (StringUtils.isEmpty(searchToken)) {
        throw new ServletException(
            "There is no search token generated, please contact community admin.");
      }
      String recommendationToken =
          getSearchToken(searchApiConfigService, httpClient, gson, objectMapper,
              email, searchApiConfigService.getRecommendationApiKey());
      String upcomingEventToken =
          getSearchToken(searchApiConfigService, httpClient, gson, objectMapper,
              email, searchApiConfigService.getUpcomingEventApiKey());

      userContext.addProperty("searchToken", searchToken);
      userContext.addProperty("recommendationToken", recommendationToken);
      userContext.addProperty("upcomingEventToken", upcomingEventToken);
      userContext.addProperty("orgId", searchApiConfigService.getOrgId());
      userContext.addProperty("validFor", searchApiConfigService.getTokenValidTime());
      userContext.remove("contactId");
      userContext.remove("email");

      String coveoInfo = gson.toJson(userContext);
      Cookie cookie = new Cookie(COVEO_COOKIE_NAME, URLEncoder.encode(coveoInfo, utfName));
      HttpUtils.setCookie(cookie, response, true, tokenExpiryTime, "/",
          searchApiConfigService.isDevMode());

      Cookie visitIdCookie = new Cookie("coveo_visitorId", userService.getUserUuid(sfId));
      HttpUtils.addCookie(visitIdCookie, response);
      servletCallback.execute(request, response, coveoInfo);
    } catch (IOException exception) {
      LOGGER.error("Get Token call fails with message: {} ", exception.getMessage());
    }
  }

  /**
   * Retrieves a search token.
   *
   * @param searchApiConfigService The search API config.
   * @param httpClient An HTTP client.
   * @param gson The Gson object.
   * @param objectMapper The object mapper.
   * @param email The user's email address.
   * @param apiKey The API key.
   *
   * @return The token.
   *
   * @throws IOException If there is an error retrieving the token.
   */
  public static String getSearchToken(SearchApiConfigService searchApiConfigService,
                                      CloseableHttpClient httpClient,
                                      Gson gson,
                                      ObjectMapper objectMapper,
                                      String email, String apiKey) throws IOException {
    if (StringUtils.isEmpty(apiKey) || apiKey.equalsIgnoreCase(CLOUD_CONFIG_NULL_VALUE)) {
      LOGGER.debug("Pass-in API key is empty or null");
      return "";
    }

    HttpPost request = new HttpPost(searchApiConfigService.getSearchTokenApi());
    StringEntity entity =
        new StringEntity(CoveoUtils.getTokenPayload(searchApiConfigService, gson, email));

    request.addHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(apiKey));
    request.addHeader(HttpHeaders.ACCEPT, "*/*");
    request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    request.setEntity(entity);

    HttpResponse response = httpClient.execute(request);
    int status = response.getStatusLine().getStatusCode();
    Map result =
        Collections.unmodifiableMap(objectMapper.readValue(response.getEntity().getContent(),
            HashMap.class));

    if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
      LOGGER.debug("getSearchToken API call is successful.");
      return (String) result.get("token");
    }

    LOGGER.error("getSearchToken API call failed. error {}",
        objectMapper.writeValueAsString(result));
    return "";
  }

  /**
   * Return the current user context from salesforce if it is set.
   *
   * @param request     the Request object.
   * @param snapService The Snap service object.
   * @return The current user context as string.
   */
  public static String getCurrentUserContext(SlingHttpServletRequest request,
                                             SnapService snapService,
                                             UserService userService) {
    String sfId = OurmUtils.getSalesForceId(request, userService);
    JsonObject contextString = snapService.getUserContext(sfId);

    if (contextString.has(USER_CONTEXT_INFO_KEY)) {
      return contextString.get(USER_CONTEXT_INFO_KEY).getAsJsonObject().toString();
    }

    return "";
  }

  /**
   * Return the search Configuration object.
   *
   * @param searchConfigService the search configuration service object.
   * @param request             the incoming sling request
   * @param snapService         the snap logic service object
   * @param userService         the user service object
   * @return the search configuration object used by component.
   */
  public static JsonObject getSearchConfig(SearchApiConfigService searchConfigService,
                                           SlingHttpServletRequest request,
                                           SnapService snapService,
                                           UserService userService) {
    JsonObject config = new JsonObject();
    String sfId = OurmUtils.getSalesForceId(request, userService);
    config.addProperty("orgId", searchConfigService.getOrgId());
    config.addProperty("searchHub", searchConfigService.getSearchHub());
    config.addProperty("analytics", true);
    config.addProperty("clientId", userService.getUserUuid(sfId));
    config.addProperty("userContext", getCurrentUserContext(request, snapService, userService));
    return config;
  }

  private static String getTokenPayload(
      SearchApiConfigService searchApiConfigService, Gson gson, String email) {
    String searchHub = searchApiConfigService.getSearchHub();
    LOGGER.debug(
        String.format("Inside getTokenPayload method, and the configured Search hub is: %s",
            searchHub));

    HashMap<String, String> userMap = new HashMap<>();
    userMap.put("name", email);
    userMap.put("provider", searchApiConfigService.getUserIdProvider());
    String userIdType = searchApiConfigService.getUserIdType();
    if (!StringUtils.isEmpty(userIdType) && !userIdType.equalsIgnoreCase(CLOUD_CONFIG_NULL_VALUE)) {
      LOGGER.debug(String.format("UserIdType is: %s", userIdType));
      userMap.put("type", userIdType);
    }

    String jsonString = gson.toJson(userMap);
    ArrayList<String> userArray = new ArrayList<>();
    userArray.add(jsonString);

    HashMap<String, Object> payloadMap = new HashMap<>();
    payloadMap.put("validFor", searchApiConfigService.getTokenValidTime());
    payloadMap.put("userIds", userArray.toString());
    payloadMap.put("searchHub", searchHub);
    JsonObject jsonObj = gson.fromJson(payloadMap.toString(), JsonObject.class);

    return jsonObj.toString();
  }

}
