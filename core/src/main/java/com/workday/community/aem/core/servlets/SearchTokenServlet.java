package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
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
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import static com.workday.community.aem.core.constants.RestApiConstants.AUTHORIZATION;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static com.workday.community.aem.core.constants.RestApiConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SearchConstants.SEARCH_EMAIL_SECURITY_PROVIDER;

/**
 * The search Token servlet class.
 */
@Component(
    service = Servlet.class,
    property = {
        org.osgi.framework.Constants.SERVICE_DESCRIPTION + "= Search Token Servlet",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.paths=" + "/bin/search/token"
    }
)
public class SearchTokenServlet extends SlingAllMethodsServlet {
  private static final Logger logger = LoggerFactory.getLogger(SearchTokenServlet.class);

  @Reference
  private transient SearchApiConfigService searchApiConfigService;

  @Reference
  private transient SnapService snapService;

  private transient CloseableHttpClient httpClient = HttpClients.createDefault();

  private transient ObjectMapper objectMapper = new ObjectMapper();

  private final transient Gson gson = new Gson();

  /**
   * Pass in ObjectMapper for the search service.
   * @param objectMapper the pass-in ObjectMapper object.
   */
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void init() throws ServletException {
    super.init();
    logger.debug("initialize Search token service");
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  /**
   * Implementation of the servlet GET method
   * @param request The HttpServletRequest object.
   * @param response The HttpServletResponse object.
   * @throws IOException if the method call fails with IOException.
   */
  @Override
  protected void doGet(SlingHttpServletRequest request,
                       SlingHttpServletResponse response) throws ServletException, IOException {
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
    int tokenExpiryTime = this.searchApiConfigService.getTokenValidTime() / 1000;

    JsonObject userContext = this.snapService.getUserContext(sfId);
    String email = userContext.has(EMAIL_NAME) ?  userContext.get(EMAIL_NAME).getAsString()
        : this.searchApiConfigService.isDevMode() ? this.searchApiConfigService.getDefaultEmail() : null;
    if (email == null) {
      throw new ServletException("User email is not in session, please contact admin");
    }

    String searchToken = getToken(httpClient, email, this.searchApiConfigService.getSearchTokenAPIKey());
    String recommendationToken = getToken(httpClient, email, this.searchApiConfigService.getRecommendationAPIKey());
    String upcomingEventToken = getToken(httpClient, email, this.searchApiConfigService.getUpcomingEventAPIKey());

    userContext.addProperty("searchToken", searchToken);
    userContext.addProperty("recommendationToken", recommendationToken);
    userContext.addProperty("upcomingEventToken", upcomingEventToken);
    userContext.addProperty("orgId", this.searchApiConfigService.getOrgId());
    userContext.addProperty("validFor", this.searchApiConfigService.getTokenValidTime());
    userContext.remove("contactId");
    userContext.remove("email");
    String coveoInfo = gson.toJson(userContext);

    Cookie cookie = new Cookie(COVEO_COOKIE_NAME, URLEncoder.encode(coveoInfo, utfName));
    HttpUtils.setCookie(cookie, response, true, tokenExpiryTime, "/", this.searchApiConfigService.isDevMode());
    response.setStatus(HttpStatus.SC_OK);
    httpClient.close();
    response.getWriter().write(coveoInfo);
  }

  private String getToken(CloseableHttpClient httpClient, String email, String apiKey) throws IOException {
    HashMap<String, String> result;

    HttpPost request = new HttpPost(searchApiConfigService.getSearchTokenAPI());
    StringEntity entity = new StringEntity(getTokenPayload(email));

    request.addHeader(AUTHORIZATION, BEARER_TOKEN.token(apiKey));
    request.addHeader(HttpConstants.HEADER_ACCEPT, "*/*");
    request.addHeader(CONTENT_TYPE, APPLICATION_SLASH_JSON);
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
    HashMap<String, String> userMap = new HashMap<>();
    HashMap<String, Object> payloadMap = new HashMap<>();

    userMap.put("name", email);
    userMap.put("provider", this.searchApiConfigService.getUserIdProvider());
    String userIdType = this.searchApiConfigService.getUserIdType();
    if (!StringUtils.isBlank(userIdType)) {
      userMap.put("type", userIdType);
    }

    String jsonString = gson.toJson(userMap);
    ArrayList<String> userArray = new ArrayList<>();
    userArray.add(jsonString);
    payloadMap.put("validFor", searchApiConfigService.getTokenValidTime());
    payloadMap.put("userIds", userArray.toString());
    payloadMap.put("searchHub", this.searchApiConfigService.getSearchHub());
    JsonObject jsonObj = gson.fromJson(payloadMap.toString(), JsonObject.class);

    return jsonObj.toString();
  }
}
