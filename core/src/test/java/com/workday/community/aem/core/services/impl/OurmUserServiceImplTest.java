package com.workday.community.aem.core.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.OurmDrupalConfig;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.pojos.OurmUser;
import com.workday.community.aem.core.pojos.OurmUserList;
import com.workday.community.aem.core.services.OurmUserService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * The Class OurmUsersApiConfigServiceImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class OurmUserServiceImplTest {
    private final AemContext context = new AemContext();


  /** The ourmUsers api service. */
  private final OurmUserService ourmUserService = new OurmUserServiceImpl();

  @Mock
  private transient ObjectMapper objectMapper;

  /** The ourmUsers config. */
  private final OurmDrupalConfig ourmUsersConfig = new OurmDrupalConfig() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return Annotation.class;
    }

    @Override
    public String ourmDrupalLookupApiEndpoint() {
      return "https://den.community-workday.com";
    }

    @Override
    public String ourmDrupalConsumerKey() {
      return "mockConsumerKey";
    }

    @Override
    public String ourmDrupalConsumerSecret() {
      return "mockConsumerSecret";
    }
  };

  /**
   * Setup.
   */
  @BeforeEach
  public void setup() {
    context.registerService(objectMapper);
    ((OurmUserServiceImpl) ourmUserService).activate(ourmUsersConfig);
    ((OurmUserServiceImpl) ourmUserService).setObjectMapper(objectMapper);
  }

  /**
   * Test all apis.
   *
   * @throws OurmException the ourm exception
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  @Test
  public void testAllApis() throws OurmException, IOException {
    String searchText = "dav";
    try (MockedStatic<HttpClients> httpClientsMockedStatic = mockStatic(HttpClients.class)) {
      CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
      httpClientsMockedStatic.when(HttpClients::createDefault).thenReturn(httpClient);
      CloseableHttpResponse response = mock(CloseableHttpResponse.class);
      StatusLine statusLine = mock(StatusLine.class);
      HttpEntity entity = mock(HttpEntity.class);
      InputStream inputStream = mock(InputStream.class);
      lenient().when(httpClient.execute(any())).thenReturn(response);
      lenient().when(response.getStatusLine()).thenReturn(statusLine);
      lenient().when(response.getEntity()).thenReturn(entity);
      lenient().when(response.getEntity().getContent()).thenReturn(inputStream);
      OurmUserList ourmUserList = new OurmUserList();
      String profileImageData = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMi";
      ourmUserList.getUsers().add(new OurmUser(profileImageData, "adavis36", "fake_first_name", "fake_last_name",
          "aaron.davis@workday.com", "0031B00002kka6hQAA"));

      lenient().when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
      
      lenient().when(objectMapper.readValue(inputStream, OurmUserList.class)).thenReturn(ourmUserList);

      assertNotNull(ourmUserService.searchOurmUserList(searchText));
    }
  }
}