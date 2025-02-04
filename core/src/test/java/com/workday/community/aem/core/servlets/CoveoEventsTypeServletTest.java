package com.workday.community.aem.core.servlets;

import static com.workday.community.aem.core.constants.GlobalConstants.CLOUD_CONFIG_NULL_VALUE;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.pojos.EventTypeValue;
import com.workday.community.aem.core.pojos.EventTypes;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.servlet.ServletException;
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

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoEventsTypeServletTest {
  private final AemContext context = new AemContext();

  @Mock
  SearchApiConfigService searchApiConfigService;

  @Mock
  DrupalService drupalService;

  @InjectMocks
  CoveoEventsTypeServlet coveoEventTypeServlet;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private UserService userService;

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
    String userData =
        "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"email\":\"foo@workday.com\",\"contextInfo\":{\"isWorkmate\":\"false\"},\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";

    try (MockedStatic<OurmUtils> ourmUtilsMock = mockStatic(OurmUtils.class);
         MockedStatic<HttpClients> httpClientsMockedStatic = mockStatic(HttpClients.class);
         MockedStatic<HttpUtils> httpUtilsMock = mockStatic(HttpUtils.class)) {
      httpUtilsMock.when(() -> HttpUtils.forbiddenResponse(request, response, userService)).thenReturn(false);
      ourmUtilsMock.when(() -> OurmUtils.getSalesForceId(any(), any())).thenReturn(DEFAULT_SFID_MASTER);
      ourmUtilsMock.when(() -> OurmUtils.getUserEmail(anyString(), any(), any())).thenReturn("test@workday.com");

      CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
      httpClientsMockedStatic.when(HttpClients::createDefault).thenReturn(httpClient);
      lenient().when(searchApiConfigService.getSearchTokenApi()).thenReturn("https://foo");
      lenient().when(drupalService.getUserData(anyString())).thenReturn(userData);
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
      lenient().when(searchApiConfigService.isDevMode()).thenReturn(true);
      coveoEventTypeServlet.setObjectMapper(this.objectMapper);
      try {
        coveoEventTypeServlet.doGet(request, response);
      } catch (ServletException | IOException exception) {
        // do nothing.
      }

      lenient().when(searchApiConfigService.isDevMode()).thenReturn(false);
      lenient().when(searchApiConfigService.getSearchTokenApiKey())
          .thenReturn(CLOUD_CONFIG_NULL_VALUE);
      try {
        coveoEventTypeServlet.doGet(request, response);
      } catch (ServletException | IOException exception) {
        // do nothing.
      }

      lenient().when(searchApiConfigService.getSearchTokenApiKey()).thenReturn("foo");
      lenient().when(searchApiConfigService.getUserIdType()).thenReturn("test user type");
      try {
        coveoEventTypeServlet.doGet(request, response);
      } catch (ServletException | IOException exception) {
        // do nothing.
      }
    }
  }
}
