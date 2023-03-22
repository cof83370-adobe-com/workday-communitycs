package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.SearchTokenService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.workday.community.aem.core.constants.GlobalConstants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.GlobalConstants.RESTAPIConstants.APPLICATION_SLASH_JSON;

@Component(
  service = Servlet.class,
  immediate = true,
  property = {
    Constants.SERVICE_DESCRIPTION + "=Search Token Servlet" + "sling.servlet.paths=/bin/search",
    "sling.servlet.methods=GET" })
public class SearchTokenServlet extends SlingAllMethodsServlet {
  private static final Logger logger = LoggerFactory.getLogger(SearchTokenServlet.class);

  @Reference
  transient SearchTokenService searchService;

  @Reference
  transient SnapService snapService;

  transient  ObjectMapper objectMapper = new ObjectMapper();

  transient Gson gson = new Gson();


  protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException {
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
    if (userContext.has("email")) {
      String email =  userContext.get("email").getAsString();
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
      HttpUtils.setCookie(cookie, response, true, tokenExpiryTime, "/");
      response.setStatus(200);
    }



  }

  private String getToken(String email, String apiKey) {
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String, String> result;

    try (CloseableHttpClient client = HttpClients.createDefault()) {
      StringEntity entity = new StringEntity(getTokenPayload(email));
      HttpPost request = new HttpPost(searchService.getSearchTokenAPI());

      request.addHeader("authorization", "Bearer " + apiKey);
      request.addHeader("accept", "application/json");
      request.addHeader("Content-Type", "application/json");
      request.setEntity(entity);

      try (CloseableHttpResponse response = client.execute(request)) {
        int status = response.getStatusLine().getStatusCode();
        if (status == 200) {
          result = mapper.readValue(response.getEntity().getContent(),
              HashMap.class);

          return result.get("token");
        }
      }
    } catch (Exception ex) {
      logger.debug(ex.getMessage());
    }

    logger.error("Fetching search Token fails");
    return "";
  }


  public String getTokenPayload(String emailId) {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    HashMap<String, String> userMap = new HashMap<>();
    HashMap<String, Object> payloadMap = new HashMap<>();

    userMap.put("name", emailId);
    userMap.put("provider", "Email Security Provider");
    userMap.put("type", "User");

    String jsonString = gson.toJson(userMap);
    ArrayList<String> userArray = new ArrayList<>();
    userArray.add(jsonString);
    payloadMap.put("validFor", searchService.getTokenValidTime());
    payloadMap.put("userIds", userArray.toString());
    JsonObject jsonObj = gson.fromJson (payloadMap.toString(), JsonObject.class);
    return jsonObj.toString();
  }
}
