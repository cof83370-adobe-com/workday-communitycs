package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.SearchService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import static com.workday.community.aem.core.constants.GlobalConstants.HttpConstants.COVEO_COOKIE_NAME;
import static com.workday.community.aem.core.constants.GlobalConstants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SearchTokenServletTest {

  @Mock
  SearchService searchService;

  @Mock
  SnapService snapService;

  @Mock
  HttpClient httpClient;

  @InjectMocks
  SearchTokenServlet searchTokenServlet;

  private final Gson gson = new Gson();

  @Test
  public void testDoGetWithExistingCookieInRequest() throws Exception {

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
      mockHttpUtils.when(() -> HttpUtils.getCookie(request, COVEO_COOKIE_NAME)).thenReturn(testCookies[1]);
      SearchTokenServlet servlet = new SearchTokenServlet();
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
    when(searchService.getTokenValidTime()).thenReturn(12000);
    when(searchService.getSearchTokenAPIKey()).thenReturn("mockSearchToken");
    when(searchService.getUpcomingEventAPIKey()).thenReturn("mockUpcomingEventAPIKey");
    when(searchService.getRecommendationAPIKey()).thenReturn("mockRecommendationAPIkey");
    when(searchService.getOrgId()).thenReturn("mockOrgId");
    when(searchService.getSearchTokenAPI()).thenReturn("http://coveo/token/api");

    JsonObject testUserContext = gson.fromJson("{\"email\":\"foo@workday.com\"}", JsonObject.class);
    when(snapService.getUserContext(anyString())).thenReturn(testUserContext);
    HttpResponse httpResponse = mock(HttpResponse.class);
    when(httpClient.execute(any())).thenReturn(httpResponse);
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
         MockedStatic<OurmUtils> mockOurmUtils = mockStatic(OurmUtils.class)) {
      mockHttpUtils.when(() -> HttpUtils.getCookie(request, COVEO_COOKIE_NAME)).thenReturn(null);
      mockOurmUtils.when(() -> OurmUtils.getSalesForceId(any())).thenReturn(DEFAULT_SFID_MASTER);
      searchTokenServlet.doGet(request, response);
      verify(response).setStatus(200);
    }
  }

  private InputStream getTestInputData() {
    JsonObject input = gson.fromJson("{\"token\":\"testSearchToken\"}", JsonObject.class);
    return new ByteArrayInputStream(input.toString().getBytes());
  }
}
