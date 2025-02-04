package com.workday.community.aem.core.servlets;

import static com.workday.community.aem.core.constants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.http.Cookie;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SearchTokenServletTest {

  private final Gson gson = new Gson();

  @Mock
  SearchApiConfigService searchApiConfigService;

  @Mock
  DrupalService drupalService;

  @Mock
  private UserService userService;

  @InjectMocks
  SearchTokenServlet searchTokenServlet;

  @Test
  public void testDoGetWithExistingCookieInRequest() {

    Cookie[] testCookies = new Cookie[] {
        new Cookie("test", "testValue"), new Cookie(COVEO_COOKIE_NAME, "coveo_cookie_value")
    };

    MockSlingHttpServletRequest request = mock(MockSlingHttpServletRequest.class);
    MockSlingHttpServletResponse response = mock(MockSlingHttpServletResponse.class);
    lenient().when(request.getCookies()).thenReturn(testCookies);
    PrintWriter pr = mock(PrintWriter.class);
    lenient().when(response.getWriter()).thenReturn(pr);

    // Invoke your servlet
    try (MockedStatic<HttpUtils> mockHttpUtils = mockStatic(HttpUtils.class)) {
      mockHttpUtils.when(() -> HttpUtils.getCookie(request, COVEO_COOKIE_NAME))
          .thenReturn(testCookies[1]);
      mockHttpUtils.when(() -> HttpUtils.forbiddenResponse(request, response, userService)).thenReturn(false);
      SearchTokenServlet servlet = new SearchTokenServlet();
      RequestPathInfo mockRequestInfo = mock(RequestPathInfo.class);
      lenient().when(request.getRequestPathInfo()).thenReturn(mockRequestInfo);
      lenient().when(mockRequestInfo.getResourcePath()).thenReturn("test/path");
      servlet.doGet(request, response);
      verify(response).setStatus(200);
    }
  }

  @Test
  public void testDoGetWithoutCookieInRequest() throws Exception {
    MockSlingHttpServletRequest request = mock(MockSlingHttpServletRequest.class);
    MockSlingHttpServletResponse response = mock(MockSlingHttpServletResponse.class);
    lenient().when(request.getCookies()).thenReturn(null);

    PrintWriter pr = mock(PrintWriter.class);
    lenient().when(response.getWriter()).thenReturn(pr);

    // Mock return from searchService
    when(searchApiConfigService.getTokenValidTime()).thenReturn(12000);
    when(searchApiConfigService.getSearchTokenApiKey()).thenReturn("mockSearchToken");
    when(searchApiConfigService.getUpcomingEventApiKey()).thenReturn("mockUpcomingEventAPIKey");
    when(searchApiConfigService.getRecommendationApiKey()).thenReturn("mockRecommendationAPIkey");
    when(searchApiConfigService.getOrgId()).thenReturn("mockOrgId");
    when(searchApiConfigService.getSearchTokenApi()).thenReturn("http://coveo/token/api");

    String userData =
        "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"email\":\"foo@workday.com\",\"contextInfo\":{\"isWorkmate\":\"false\"},\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
    when(drupalService.getUserData(anyString())).thenReturn(userData);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);

    StatusLine statusLine = new StatusLine() {
      @Override
      public ProtocolVersion getProtocolVersion() {
        return null;
      }

      @Override
      public int getStatusCode() {
        return 200;
      }

      @Override
      public String getReasonPhrase() {
        return null;
      }
    };

    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    HttpEntity entity = mock(HttpEntity.class);
    when(httpResponse.getEntity()).thenReturn(entity);

    InputStream input = getTestInputData();
    when(entity.getContent()).thenReturn(input);

    HashMap<String, String> testResult = new HashMap<>();
    testResult.put("token", "searchToken");

    ObjectMapper objectMapper = mock(ObjectMapper.class);
    searchTokenServlet.setObjectMapper(objectMapper);
    when(objectMapper.readValue(input, HashMap.class)).thenReturn(testResult);

    // Invoke your servlet
    try (MockedStatic<HttpUtils> mockHttpUtils = mockStatic(HttpUtils.class);
         MockedStatic<OurmUtils> mockOurmUtils = mockStatic(OurmUtils.class);
         MockedStatic<HttpClients> mockHttpClients = mockStatic(HttpClients.class)) {
      mockHttpUtils.when(() -> HttpUtils.getCookie(request, COVEO_COOKIE_NAME)).thenReturn(null);
      mockHttpUtils.when(() -> HttpUtils.forbiddenResponse(request, response, userService)).thenReturn(false);
      mockOurmUtils.when(() -> OurmUtils.getSalesForceId(any(), any()))
          .thenReturn(DEFAULT_SFID_MASTER);
      mockOurmUtils.when(() -> OurmUtils.getUserEmail(anyString(), any(), any()))
          .thenReturn("foo@workday.com");

      mockHttpClients.when(HttpClients::createDefault).thenReturn(httpClient);
      when(httpClient.execute(any())).thenReturn(httpResponse);
      RequestPathInfo mockRequestInfo = mock(RequestPathInfo.class);
      lenient().when(request.getRequestPathInfo()).thenReturn(mockRequestInfo);
      lenient().when(mockRequestInfo.getResourcePath()).thenReturn("test/path");

      searchTokenServlet.doGet(request, response);
      verify(response).setStatus(200);
    }
  }

  private InputStream getTestInputData() {
    JsonObject input = gson.fromJson("{\"token\":\"testSearchToken\"}", JsonObject.class);
    return new ByteArrayInputStream(input.toString().getBytes());
  }
}
