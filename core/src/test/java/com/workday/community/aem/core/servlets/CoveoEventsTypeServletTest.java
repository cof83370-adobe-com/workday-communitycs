package com.workday.community.aem.core.servlets;

import static com.workday.community.aem.core.constants.GlobalConstants.CLOUD_CONFIG_NULL_VALUE;
import static com.workday.community.aem.core.constants.SearchConstants.EMAIL_NAME;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.pojos.EventTypes;
import com.workday.community.aem.core.pojos.EventTypeValue;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.OurmUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import javax.servlet.ServletException;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class CoveoEventsTypeServletTest {
  private final AemContext context = new AemContext();

  @Mock
  SearchApiConfigService searchApiConfigService;

  @Mock
  SnapService snapService;

  @Mock
  private transient ObjectMapper objectMapper;

  @InjectMocks
  CoveoEventsTypeServlet coveoEventTypeServlet;

  @BeforeEach
  public void setup() {
    context.registerService(objectMapper);
  }

  @Test
  public void testDoGet() throws IOException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
    ResourceResolver resourceResolver = mock(ResourceResolver.class);
    lenient().when(request.getResourceResolver()).thenReturn(resourceResolver);
    lenient().when(searchApiConfigService.isDevMode()).thenReturn(true);
    JsonObject userObject = new JsonObject();
    userObject.addProperty(EMAIL_NAME, "test@workday.com");

    try (MockedStatic<OurmUtils> ourmUtilsMock = mockStatic(OurmUtils.class);
    MockedStatic<HttpClients> httpClientsMockedStatic = mockStatic(HttpClients.class)){
      ourmUtilsMock.when(()-> OurmUtils.getSalesForceId(any())).thenReturn(DEFAULT_SFID_MASTER);
      ourmUtilsMock.when(()-> OurmUtils.getUserEmail(anyString(), any(), any())).thenReturn("test@workday.com");

      CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
      httpClientsMockedStatic.when(HttpClients::createDefault).thenReturn(httpClient);
      lenient().when(searchApiConfigService.getSearchTokenAPI()).thenReturn("https://foo");
      lenient().when(snapService.getUserContext(anyString())).thenReturn(userObject);
      CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
      StatusLine statusLine = mock(StatusLine.class);
      HttpEntity entity = mock(HttpEntity.class);
      InputStream inputStream = mock(InputStream.class);
      lenient().when(httpClient.execute(any())).thenReturn(httpResponse);
      lenient().when(httpResponse.getStatusLine()).thenReturn(statusLine);
      lenient().when(httpResponse.getEntity()).thenReturn(entity);
      lenient().when(entity.getContent()).thenReturn(inputStream);

      lenient().when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
      HashMap<String, String> result = new HashMap<>();
      result.put("token", "testToken");
      EventTypes eventTypes = new EventTypes();
      eventTypes.getValues().add(new EventTypeValue("test", "test"));
      lenient().when(objectMapper.readValue(inputStream, HashMap.class)).thenReturn(result);
      lenient().when(objectMapper.readValue(inputStream, EventTypes.class)).thenReturn(eventTypes);

      coveoEventTypeServlet.setObjectMapper(this.objectMapper);
      try {
        coveoEventTypeServlet.doGet(request, response);
      } catch (ServletException | IOException exception){
        // do nothing.
      }

      lenient().when(searchApiConfigService.getSearchTokenAPIKey()).thenReturn(CLOUD_CONFIG_NULL_VALUE);
      try {
        coveoEventTypeServlet.doGet(request, response);
      } catch (ServletException | IOException exception){
        // do nothing.
      }

      lenient().when(searchApiConfigService.getSearchTokenAPIKey()).thenReturn("foo");
      lenient().when(searchApiConfigService.getUserIdType()).thenReturn("test user type");
      try {
        coveoEventTypeServlet.doGet(request, response);
      } catch (ServletException | IOException exception){
        // do nothing.
      }
    }
  }
}