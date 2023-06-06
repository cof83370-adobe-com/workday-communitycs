package com.workday.community.aem.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.workday.community.aem.core.constants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.RestApiConstants.APPLICATION_SLASH_JSON;
import static com.workday.community.aem.core.constants.RestApiConstants.AUTHORIZATION;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.RestApiConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;

public class CoveoUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CoveoUtils.class);

  public static void executeSearchForCallback(SlingHttpServletRequest request,
                                              SlingHttpServletResponse response,
                                              SearchApiConfigService searchApiConfigService,
                                              SnapService snapService,
                                              Gson gson,
                                              ObjectMapper objectMapper,
                                              ServletCallback servletCallback) throws ServletException, IOException {
    String utfName = StandardCharsets.UTF_8.name();
    response.setContentType(APPLICATION_SLASH_JSON);
    response.setCharacterEncoding(utfName);

    Cookie coveoCookie = HttpUtils.getCookie(request, COVEO_COOKIE_NAME);
    if (coveoCookie != null) {
      String coveoInfo = URLDecoder.decode(coveoCookie.getValue(), utfName);
      servletCallback.execute(request, response, coveoInfo);
      return;
    }

    String sfId = OurmUtils.getSalesForceId(request.getResourceResolver());
    int tokenExpiryTime = searchApiConfigService.getTokenValidTime() / 1000;

    JsonObject userContext = snapService.getUserContext(sfId);
    String email = userContext.has(EMAIL_NAME) ? userContext.get(EMAIL_NAME).getAsString()
        : (searchApiConfigService.isDevMode() ? searchApiConfigService.getDefaultEmail() : null);
    if (email == null) {
      throw new ServletException("User email is not in session, please contact admin");
    }

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      String searchToken = CoveoUtils.getSearchToken(searchApiConfigService, httpClient, gson, objectMapper,
          email, searchApiConfigService.getSearchTokenAPIKey());
      if (StringUtils.isEmpty(searchToken)) {
        throw new ServletException("there is no search token generated, please contact community admin.");
      }
      String recommendationToken = CoveoUtils.getSearchToken(searchApiConfigService, httpClient, gson, objectMapper,
          email, searchApiConfigService.getRecommendationAPIKey());
      String upcomingEventToken = CoveoUtils.getSearchToken(searchApiConfigService, httpClient, gson, objectMapper,
          email, searchApiConfigService.getUpcomingEventAPIKey());

      userContext.addProperty("searchToken", searchToken);
      userContext.addProperty("recommendationToken", recommendationToken);
      userContext.addProperty("upcomingEventToken", upcomingEventToken);
      userContext.addProperty("orgId", searchApiConfigService.getOrgId());
      userContext.addProperty("validFor", searchApiConfigService.getTokenValidTime());
      userContext.remove("contactId");
      userContext.remove("email");
      String coveoInfo = gson.toJson(userContext);

      Cookie cookie = new Cookie(COVEO_COOKIE_NAME, URLEncoder.encode(coveoInfo, utfName));
      HttpUtils.setCookie(cookie, response, true, tokenExpiryTime, "/", searchApiConfigService.isDevMode());
      servletCallback.execute(request, response, coveoInfo);
    }
  }

  public static String getSearchToken(SearchApiConfigService searchApiConfigService,
                               CloseableHttpClient httpClient,
                               Gson gson,
                               ObjectMapper objectMapper,
                               String email, String apiKey) throws IOException {
    Map result;

    HttpPost request = new HttpPost(searchApiConfigService.getSearchTokenAPI());
    StringEntity entity = new StringEntity(CoveoUtils.getTokenPayload(searchApiConfigService, gson, email));

    request.addHeader(AUTHORIZATION, BEARER_TOKEN.token(apiKey));
    request.addHeader(HttpConstants.HEADER_ACCEPT, "*/*");
    request.addHeader(CONTENT_TYPE, APPLICATION_SLASH_JSON);
    request.setEntity(entity);

    HttpResponse response = httpClient.execute(request);
    int status = response.getStatusLine().getStatusCode();
    if (status == HttpStatus.SC_OK) {
      result = Collections.unmodifiableMap(objectMapper.readValue(response.getEntity().getContent(),
          HashMap.class));

      return (String)result.get("token");
    }

    return "";
  }

  private static String getTokenPayload(SearchApiConfigService searchApiConfigService,
                                       Gson gson,
                                       String email) {
    HashMap<String, String> userMap = new HashMap<>();
    HashMap<String, Object> payloadMap = new HashMap<>();

    String searchHub = searchApiConfigService.getSearchHub();
    LOGGER.debug(String.format("The configured Search hub is: %s", searchHub));

    userMap.put("name", email);
    userMap.put("provider", searchApiConfigService.getUserIdProvider());
    String userIdType = searchApiConfigService.getUserIdType();
    if (!StringUtils.isBlank(userIdType)) {
      userMap.put("type", userIdType);
    }

    String jsonString = gson.toJson(userMap);
    ArrayList<String> userArray = new ArrayList<>();
    userArray.add(jsonString);
    payloadMap.put("validFor", searchApiConfigService.getTokenValidTime());
    payloadMap.put("userIds", userArray.toString());
    payloadMap.put("searchHub", searchHub);
    JsonObject jsonObj = gson.fromJson(payloadMap.toString(), JsonObject.class);

    return jsonObj.toString();
  }
}
