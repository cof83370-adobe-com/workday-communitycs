package com.workday.community.aem.core.utils;

import static com.workday.community.aem.core.constants.GlobalConstants.CLOUD_CONFIG_NULL_VALUE;
import static com.workday.community.aem.core.constants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SnapConstants.USER_CONTEXT_INFO_KEY;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.oltu.oauth2.common.OAuth.ContentType.JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;

/**
 * Utility class for Coveo functionality.
 */
@Slf4j
public class CoveoUtils {

  /**
   * Executes a search.
   *
   * @param request                The request object.
   * @param response               The response object.
   * @param searchApiConfigService The search API config.
   * @param drupalService          The drupal service.
   * @param userService            The user service.
   * @param gson                   The Gson object.
   * @param objectMapper           The object mapper.
   * @param servletCallback        The servlet callback.
   * @throws ServletException If the user does not have an email address or no search token.
   * @throws IOException      If the callback execution fails.
   */
  public static void executeSearchForCallback(SlingHttpServletRequest request,
                                              SlingHttpServletResponse response,
                                              SearchApiConfigService searchApiConfigService,
                                              DrupalService drupalService,
                                              UserService userService,
                                              Gson gson,
                                              ObjectMapper objectMapper,
                                              ServletCallback servletCallback)
      throws ServletException, IOException, DrupalException {
    String utfName = StandardCharsets.UTF_8.name();
    response.setContentType(JSON);
    response.setCharacterEncoding(utfName);

    Cookie coveoCookie = HttpUtils.getCookie(request, COVEO_COOKIE_NAME);
    if (coveoCookie != null) {
      log.debug("Coveo cookie still active, decode it to send data back");
      String coveoInfo = URLDecoder.decode(coveoCookie.getValue(), utfName);
      servletCallback.execute(request, response, coveoInfo);
      return;
    }

    String sfId = OurmUtils.getSalesForceId(request, userService);
    int tokenExpiryTime = searchApiConfigService.getTokenValidTime() / 1000;
    boolean isDevMode = searchApiConfigService.isDevMode();

    String userData = drupalService.getUserData(sfId);
    JsonObject userDataObject = gson.fromJson(userData, JsonObject.class);

    JsonObject userContext = userDataObject != null && !userDataObject.isJsonNull()
        ? userDataObject.getAsJsonObject(USER_CONTEXT_INFO_KEY)
        : new JsonObject();
    if (userContext == null || userContext.isJsonNull()
        || (userContext.has("error") && userContext.get("error").getAsBoolean())) {
      log.error("the userContext is null or not fetched correctly");
      userContext = new JsonObject();
    }

    String email;
    if (isDevMode) {
      log.debug("dev mode is enabled");
      email = searchApiConfigService.getDefaultEmail();
    } else {
      email = userDataObject != null && !userDataObject.isJsonNull() && userDataObject.has(EMAIL_NAME)
          ? userDataObject.get(EMAIL_NAME).getAsString() : null;
    }
    if (StringUtils.isEmpty(email)) {
      throw new ServletException("Email for current user is empty");
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

      JsonObject cookeObject = new JsonObject();
      cookeObject.addProperty("searchToken", searchToken);
      cookeObject.addProperty("recommendationToken", recommendationToken);
      cookeObject.addProperty("upcomingEventToken", upcomingEventToken);
      cookeObject.addProperty("orgId", searchApiConfigService.getOrgId());
      cookeObject.addProperty("validFor", searchApiConfigService.getTokenValidTime());
      cookeObject.addProperty("firstName",
          userContext.has("firstName") ? userContext.get("firstName").getAsString() : "");
      cookeObject.addProperty("lastName",
          userContext.has("lastName") ? userContext.get("lastName").getAsString() : "");
      cookeObject.add("address",
          userContext.has("address") ? userContext.get("address").getAsJsonObject() : new JsonObject());

      String coveoInfo = gson.toJson(cookeObject);
      Cookie cookie = new Cookie(COVEO_COOKIE_NAME, URLEncoder.encode(coveoInfo, utfName));
      HttpUtils.setCookie(cookie, response, true, tokenExpiryTime, "/",
          searchApiConfigService.isDevMode());

      Cookie visitIdCookie = new Cookie("coveo_visitorId", userService.getUserUuid(sfId));
      HttpUtils.addCookie(visitIdCookie, response);
      servletCallback.execute(request, response, coveoInfo);
    } catch (IOException exception) {
      log.error("Get Token call fails with message: {} ", exception.getMessage());
    }
  }

  /**
   * Retrieves a search token.
   *
   * @param searchApiConfigService The search API config.
   * @param httpClient             An HTTP client.
   * @param gson                   The Gson object.
   * @param objectMapper           The object mapper.
   * @param email                  The user's email address.
   * @param apiKey                 The API key.
   * @return The token.
   * @throws IOException If there is an error retrieving the token.
   */
  public static String getSearchToken(SearchApiConfigService searchApiConfigService,
                                      CloseableHttpClient httpClient,
                                      Gson gson,
                                      ObjectMapper objectMapper,
                                      String email, String apiKey) throws IOException {
    if (StringUtils.isEmpty(apiKey) || apiKey.equalsIgnoreCase(CLOUD_CONFIG_NULL_VALUE)) {
      log.debug("Pass-in API key is empty or null");
      return "";
    }

    HttpPost request = new HttpPost(searchApiConfigService.getSearchTokenApi());
    StringEntity entity =
        new StringEntity(CoveoUtils.getTokenPayload(searchApiConfigService, gson, email));

    request.addHeader(AUTHORIZATION, BEARER_TOKEN.token(apiKey));
    request.addHeader(HttpConstants.HEADER_ACCEPT, "*/*");
    request.addHeader(CONTENT_TYPE, JSON);
    request.setEntity(entity);

    HttpResponse response = httpClient.execute(request);
    int status = response.getStatusLine().getStatusCode();
    Map result =
        Collections.unmodifiableMap(objectMapper.readValue(response.getEntity().getContent(),
            HashMap.class));

    if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
      log.debug("getSearchToken API call is successful.");
      return (String) result.get("token");
    }

    log.error("getSearchToken API call failed. error {}", objectMapper.writeValueAsString(result));
    return "";
  }

  /**
   * Return the current user context from salesforce if it is set.
   *
   * @param request       the Request object.
   * @param drupalService The Drupal service object.
   * @return The current user context as string.
   */
  public static String getCurrentUserContext(SlingHttpServletRequest request,
                                             DrupalService drupalService,
                                             UserService userService) {
    String sfId = OurmUtils.getSalesForceId(request, userService);
    JsonObject contextObject = drupalService.getUserContext(sfId);
    if (contextObject != null && !contextObject.isJsonNull()) {
      return contextObject.toString();
    }
    return "";
  }

  /**
   * Return the search Configuration object.
   *
   * @param searchConfigService the search configuration service object.
   * @param request             the incoming sling request
   * @param drupalService       the drupal service object
   * @param userService         the user service object
   * @return the search configuration object used by component.
   */
  public static JsonObject getSearchConfig(SearchApiConfigService searchConfigService,
                                           SlingHttpServletRequest request,
                                           DrupalService drupalService,
                                           UserService userService) {
    JsonObject config = new JsonObject();
    String sfId = OurmUtils.getSalesForceId(request, userService);
    config.addProperty("orgId", searchConfigService.getOrgId());
    config.addProperty("searchHub", searchConfigService.getSearchHub());
    config.addProperty("analytics", true);
    config.addProperty("clientId", userService.getUserUuid(sfId));
    config.addProperty("userContext", getCurrentUserContext(request, drupalService, userService));
    return config;
  }

  private static String getTokenPayload(
      SearchApiConfigService searchApiConfigService, Gson gson, String email) {
    String searchHub = searchApiConfigService.getSearchHub();
    log.debug(
        String.format("Inside getTokenPayload method, and the configured Search hub is: %s",
            searchHub));

    Map<String, String> userMap = new HashMap<>();
    userMap.put("name", email);
    userMap.put("provider", searchApiConfigService.getUserIdProvider());
    String userIdType = searchApiConfigService.getUserIdType();
    if (!StringUtils.isEmpty(userIdType) && !userIdType.equalsIgnoreCase(CLOUD_CONFIG_NULL_VALUE)) {
      log.debug(String.format("UserIdType is: %s", userIdType));
      userMap.put("type", userIdType);
    }

    String jsonString = gson.toJson(userMap);
    List<String> userArray = new ArrayList<>();
    userArray.add(jsonString);

    Map<String, Object> payloadMap = new HashMap<>();
    payloadMap.put("validFor", searchApiConfigService.getTokenValidTime());
    payloadMap.put("userIds", userArray.toString());
    payloadMap.put("searchHub", searchHub);
    JsonObject jsonObj = gson.fromJson(payloadMap.toString(), JsonObject.class);

    return jsonObj.toString();
  }

}
