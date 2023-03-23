package com.workday.community.aem.core.servlets;

import com.drew.lang.annotations.NotNull;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.SearchService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import static com.workday.community.aem.core.constants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.RestApiConstants.APPLICATION_SLASH_JSON;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SearchConstants.SEARCH_EMAIL_SECURITY_PROVIDER;
import static com.workday.community.aem.core.constants.SearchConstants.SEARCH_TOKEN_USER_TYPE;

/**
 * The search Token servlet class.
 */
@Component(
    service = Servlet.class,
    immediate = true,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Search Token Servlet",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=/search/token",
    }
)
public class SearchTokenServlet extends SlingAllMethodsServlet {
  private static final Logger logger = LoggerFactory.getLogger(SearchTokenServlet.class);

  @ObjectClassDefinition(name = "My Servlet Configuration")
  public @interface Config {
    @AttributeDefinition(name = "Object Mapper", description = "Inject an instance of ObjectMapper")
    String object_mapper() default "";
  }

  @Reference
  private transient SearchService searchService;

  @Reference
  private transient SnapService snapService;

  @Reference
  private transient HttpClient httpClient;

  private transient ObjectMapper objectMapper = new ObjectMapper();

  private final transient Gson gson = new Gson();

  /**
   * Pass in ObjectMapper for the search service.
   * @param objectMapper the pass-in ObjectMapper object.
   */
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Implementation of the servlet GET method
   * @param request The HttpServletRequest object.
   * @param response The HttpServletResponse object.
   * @throws IOException if the method call fails with IOException.
   */
  @Override
  protected void doGet(@NotNull SlingHttpServletRequest request,
                       @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
    logger.debug("start to receive request for fetching search token");

    String utfName = StandardCharsets.UTF_8.name();
    response.setContentType(APPLICATION_SLASH_JSON);
    response.setCharacterEncoding(utfName);

    Cookie coveoCookie = HttpUtils.getCookie(request, COVEO_COOKIE_NAME);
    if (coveoCookie != null) {
      String coveoInfo = URLDecoder.decode(coveoCookie.getValue(), utfName);
      response.setStatus(200);
      response.getWriter().write(coveoInfo);
      return;
    }

    String sfId = OurmUtils.getSalesForceId(request.getResourceResolver());
    int tokenExpiryTime = searchService.getTokenValidTime() / 1000;

    JsonObject userContext = snapService.getUserContext(sfId);
    if (userContext.has(EMAIL_NAME)) {
      String email = userContext.get(EMAIL_NAME).getAsString();
      String searchToken = getToken(email, searchService.getSearchTokenAPIKey());
      String recommendationToken = getToken(email, searchService.getRecommendationAPIKey());
      String upcomingEventToken = getToken(email, searchService.getUpcomingEventAPIKey());

      userContext.addProperty("searchToken", searchToken);
      userContext.addProperty("recommendationToken", recommendationToken);
      userContext.addProperty("upcomingEventToken", upcomingEventToken);
      userContext.addProperty("orgId", searchService.getOrgId());
      userContext.remove("contactId");
      userContext.remove("email");
      String coveoInfo = gson.toJson(userContext);

      Cookie cookie = new Cookie(COVEO_COOKIE_NAME, URLEncoder.encode(coveoInfo, utfName));
      HttpUtils.setCookie(cookie, response, true, tokenExpiryTime, "/", searchService.isDevMode());
      response.setStatus(HttpStatus.SC_OK);
    }
  }

  private String getToken(String email, String apiKey) throws IOException {
    HashMap<String, String> result;

    HttpPost request = new HttpPost(searchService.getSearchTokenAPI());
    StringEntity entity = new StringEntity(getTokenPayload(email));

    request.addHeader("authorization", BEARER_TOKEN.token(apiKey));
    request.addHeader("accept", APPLICATION_SLASH_JSON);
    request.addHeader("Content-Type", APPLICATION_SLASH_JSON);
    request.setEntity(entity);

    HttpResponse response = httpClient.execute(request);
    int status = response.getStatusLine().getStatusCode();
    if (status == HttpStatus.SC_OK) {
      result = objectMapper.readValue(response.getEntity().getContent(),
          HashMap.class);

      return result.get("token");
    }

    logger.error("Retrieve token returns empty");
    return "";
  }

  private String getTokenPayload(String email) {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    HashMap<String, String> userMap = new HashMap<>();
    HashMap<String, Object> payloadMap = new HashMap<>();

    userMap.put("name", email);
    userMap.put("provider", SEARCH_EMAIL_SECURITY_PROVIDER);
    userMap.put("type", SEARCH_TOKEN_USER_TYPE);

    String jsonString = gson.toJson(userMap);
    ArrayList<String> userArray = new ArrayList<>();
    userArray.add(jsonString);
    payloadMap.put("validFor", searchService.getTokenValidTime());
    payloadMap.put("userIds", userArray.toString());
    JsonObject jsonObj = gson.fromJson(payloadMap.toString(), JsonObject.class);

    return jsonObj.toString();
  }
}
