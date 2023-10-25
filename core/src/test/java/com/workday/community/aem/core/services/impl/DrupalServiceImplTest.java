package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.utils.RestApiUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.annotation.Annotation;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DrupalServiceImplTest class.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class DrupalServiceImplTest {
  private final DrupalServiceImpl service = new DrupalServiceImpl();

  /**
   * AemContext
   */
  private final AemContext context = new AemContext();

  @Mock
  RunModeConfigService runModeConfigService;

  private CacheManagerServiceImpl cacheManagerService;

  /**
   * Test config.
   */
  private final DrupalConfig testConfig = new DrupalConfig() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public String drupalApiUrl() {
      return "drupalApiUrl";
    }

    @Override
    public String drupalTokenPath() {
      return "drupalTokenPath";
    }

    @Override
    public String drupalUserDataPath() {
      return "drupalUserDataPath";
    }

    @Override
    public String drupalApiClientId() {
      return "drupalApiClientId";
    }

    @Override
    public String drupalApiClientSecret() {
      return "drupalApiClientSecret";
    }

    @Override
    public int drupalTokenCacheMax() {
      return 100;
    }

    @Override
    public long drupalTokenCacheTimeout() {
      return 1000;
    }

    @Override
    public boolean enableCache() {
      return true;
    }

    @Override
    public String drupalUserSearchPath() {
      return "drupalUserSearchPath";
    }
  };

  /**
   * Set up method for test run.
   */
  @BeforeEach
  public void setup() {
    cacheManagerService = new CacheManagerServiceImpl();
    CacheConfig cacheConfig = TestUtil.getCacheConfig();
    cacheManagerService.activate(cacheConfig);

    service.setServiceCacheMgr(cacheManagerService);
    service.setRunModeConfigService(runModeConfigService);

    context.registerService(RunModeConfigService.class, runModeConfigService);
    context.registerService(cacheManagerService);

    ((DrupalServiceImpl) service).activate(testConfig);
  }

  /**
   * Test method for getAPIToken method.
   *
   * @throws DrupalException
   * @throws InterruptedException
   */
  @Test
  public void testGetAPIToken() throws DrupalException, InterruptedException {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      // Case 1: with valid response
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(response);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(response.getResponseBody()).thenReturn(responseBody);
      when(response.getResponseCode()).thenReturn(200);

      String token = this.service.getApiToken();
      assertEquals("bearerToken", token);

      // Case 2: though response is null, returns from cache
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(null);
      token = this.service.getApiToken();
      assertEquals("bearerToken", token);
      Thread.sleep(2000);

      // Case 3: clear cache using sleep interval, with response as null
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(null);
      token = this.service.getApiToken();
      assertEquals("", token);
      Thread.sleep(2000);

      // Case 4: clear cache using sleep interval, response doesn't contain access
      // token
      responseBody = "{\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      token = this.service.getApiToken();
      assertEquals("", token);
    }
  }

  /**
   * Test method for getUserData method.
   */
  @Test
  public void testGetUserData() {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);

      // Case 1: with valid response
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

      String userDataResponse =
          "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
      when(response.getResponseBody()).thenReturn(userDataResponse);
      when(response.getResponseCode()).thenReturn(200);

      String userData = this.service.getUserData("sfId");
      assertEquals(userDataResponse, userData);
      cacheManagerService.invalidateCache();

      // Case 2: with response as null
      mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(null);
      userData = this.service.getUserData("sfId");
      assertEquals("", userData);
    }
  }

  /**
   * Test method for getUserProfileImage method.
   */
  @Test
  public void testGetUserProfileImage() {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    HttpClientBuilder builder = mock(HttpClientBuilder.class);
    try (MockedStatic<HttpClients> MockedHttpClients = mockStatic(HttpClients.class);
         MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {

      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);

      MockedHttpClients.when(HttpClients::custom).thenReturn(builder);
      lenient().when(builder.build()).thenReturn(httpClient);
      assertEquals(this.service.getUserProfileImage("sfId"), StringUtils.EMPTY);
      cacheManagerService.invalidateCache();

      // Return from drupal api
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

      String userDataResponse =
          "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
      when(response.getResponseBody()).thenReturn(userDataResponse);
      when(response.getResponseCode()).thenReturn(200);

      String imageReturn = this.service.getUserProfileImage("sfId");
      assertEquals(imageReturn, "data:image/jpeg;base64,");

      // From cache
      userDataResponse =
          "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/png;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
      imageReturn = this.service.getUserProfileImage("sfId");
      assertEquals(imageReturn, "data:image/jpeg;base64,");
    }
  }

  @Test
  public void testGetUserProfileImageWithException() {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);
      mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString()))
          .thenThrow(new DrupalException());
      assertEquals(this.service.getUserProfileImage("sfId"), StringUtils.EMPTY);
    }
  }

  /**
   * Test method for getUserTimezone method.
   */
  @Test
  public void testGetUserTimezone() {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);

      // Return from drupal api
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

      String userDataResponse =
          "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
      when(response.getResponseBody()).thenReturn(userDataResponse);
      when(response.getResponseCode()).thenReturn(200);

      String timeZoneReturn = this.service.getUserTimezone("sfId");
      assertEquals(timeZoneReturn, "America/Los_Angeles");

      // From cache
      userDataResponse =
          "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/New York\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
      timeZoneReturn = this.service.getUserTimezone("sfId");
      assertEquals(timeZoneReturn, "America/Los_Angeles");
    }
  }

  /**
   * Test method for getAdobeDigitalData method.
   */
  @Test
  public void testGetAdobeDigitalData() {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);

      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

      String userDataResponse =
          "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"123\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
      when(response.getResponseBody()).thenReturn(userDataResponse);
      when(response.getResponseCode()).thenReturn(200);

      String pageTitle = "FAQ page";
      String contentType = "FAQ";
      String contactNumber = "123";
      String organizationName = "Workday";
      String adobeData = this.service.getAdobeDigitalData("sfId", pageTitle, contentType);
      assertTrue(adobeData.contains(pageTitle));
      assertTrue(adobeData.contains(contentType));
      assertTrue(adobeData.contains(contactNumber));
      assertTrue(adobeData.contains(organizationName));

      String adobeData1 = this.service.getAdobeDigitalData("sfId", pageTitle, contentType);
      assertEquals(adobeData, adobeData1);
    }
  }

  /**
   * Test method for getUserContext method.
   */
  @Test
  public void testGetUserContext() {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);

      // Return from drupal api
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

      String userDataResponse =
          "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"email\":\"foo@workday.com\",\"contextInfo\":{\"isWorkmate\":\"false\"},\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
      when(response.getResponseBody()).thenReturn(userDataResponse);
      when(response.getResponseCode()).thenReturn(200);

      String contextReturn = this.service.getUserContext(DEFAULT_SFID_MASTER).toString();
      assertEquals(contextReturn, "{\"isWorkmate\":\"false\"}");
    }
  }

  /**
   * Test method for searchOurmUserList.
   */
  @Test
  public void testSearchOurmUserList() throws DrupalException {
    String searchText = "fakeString";
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);

      String testUserContext =
          "{\"users\":[{\"sfId\":\"fakeSfId\",\"username\":\"fakeUserName\",\"firstName\":\"fake_first_name\",\"lastName\":\"fake_last_name\",\"email\":\"fakeEmail\",\"profileImageData\":\"fakeProfileData\"}]}";
      // Return from drupal api
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalUserSearchGet(anyString(), anyString())).thenReturn(response);
      when(response.getResponseBody()).thenReturn(testUserContext);
      when(response.getResponseCode()).thenReturn(200);

      JsonObject ret = this.service.searchOurmUserList(searchText);
      assertEquals(testUserContext, ret.toString());
    }
  }

  /**
   * Test method for searchOurmUserList with space in input parameter.
   */
  @Test
  public void testSearchOurmUserListWithSpace() throws DrupalException {
    String searchText = "fake String";
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      ApiResponse tokenResponse = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
          anyString())).thenReturn(tokenResponse);
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
      when(tokenResponse.getResponseBody()).thenReturn(responseBody);
      when(tokenResponse.getResponseCode()).thenReturn(200);

      String testUserContext =
          "{\"users\":[{\"sfId\":\"fakeSfId\",\"username\":\"fakeUserName\",\"firstName\":\"fake_first_name\",\"lastName\":\"fake_last_name\",\"email\":\"fakeEmail\",\"profileImageData\":\"fakeProfileData\"}]}";
      // Return from drupal api
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalUserSearchGet(anyString(), anyString())).thenReturn(response);
      when(response.getResponseBody()).thenReturn(testUserContext);
      when(response.getResponseCode()).thenReturn(200);

      JsonObject ret = this.service.searchOurmUserList(searchText);
      assertEquals(testUserContext, ret.toString());
    }
  }
}
