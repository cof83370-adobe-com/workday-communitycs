package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.dto.AemContentDto;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.utils.RestApiUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.annotation.Annotation;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DrupalServiceImplTest class.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class DrupalServiceImplTest {

  /**
   * The path to the Community content page.
   */
  static final String COMMUNITY_EVENT_PAGE_PATH = "/content/workday-community/en-us/event1/event2";

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
    public String drupalUserLookupClientId() {
      return "drupalUserLookupClientId";
    }

    @Override
    public String drupalUserLookupClientSecret() {
      return "drupalUserLookupClientSecret";
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

    @Override
    public String drupalInstanceDomain() {
      return "http://test-link.com";
    }

    @Override
    public String drupalCsrfTokenPath() {
      return "drupalCsrfTokenPath";
    }

    @Override
    public boolean contentSyncEnabled() {
      return false;
    }

    @Override
    public String drupalAemContentEntityPath() {
      return "drupalAemContentEntityPath";
    }

    @Override
    public String drupalAemContentDeleteEntityPath() {
      return "drupalAemContentDeleteEntityPath";
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
    MockitoAnnotations.openMocks(this);
    service.activate(testConfig);
  }

  /**
   * Test method for getAPIToken method.
   *
   * @throws DrupalException      DrupalException
   * @throws InterruptedException InterruptedException
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
      token = this.service.getApiToken();
      assertEquals("", token);
    }
  }

  /**
   * Test method for getAPIToken method.
   *
   * @throws DrupalException      DrupalException
   * @throws InterruptedException InterruptedException
   */
  @Test
  public void testCsrfToken() throws DrupalException, InterruptedException {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      // Case 1: with valid response
      ApiResponse response = mock(ApiResponse.class);
      mocked.when(() -> RestApiUtil.doDrupalCsrfTokenGet(anyString())).thenReturn(response);
      String responseBody = "testCsrfToken";
      when(response.getResponseBody()).thenReturn(responseBody);
      when(response.getResponseCode()).thenReturn(200);

      String token = this.service.getCsrfToken();
      assertEquals("testCsrfToken", token);

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
          .thenThrow(new DrupalException("test failure"));
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
      assertEquals("{\"isWorkmate\":\"false\"}", contextReturn);
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

  @Test
  public void testCreateOrUpdateEntity_Success() throws DrupalException {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      AemContentDto aemContentDto = new AemContentDto();
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";

      ApiResponse response1 = mock(ApiResponse.class);
      response1.setResponseBody(responseBody);
      response1.setResponseCode(HttpStatus.SC_CREATED);

      ApiResponse response2 = mock(ApiResponse.class);
      response2.setResponseBody("CsrfToken");
      response2.setResponseCode(HttpStatus.SC_CREATED);

      ApiResponse response3 = mock(ApiResponse.class);
      response2.setResponseBody("Entity Created");
      response2.setResponseCode(HttpStatus.SC_CREATED);

      when(RestApiUtil.doDrupalTokenGet(anyString(),  anyString(), anyString())).thenReturn(response1);
      when(RestApiUtil.doDrupalCsrfTokenGet(anyString())).thenReturn(response3);
      when(RestApiUtil.doDrupalCreateOrUpdateEntity(anyString(),any(), anyString(), anyString())).thenReturn(response3);

      ApiResponse result = this.service.createOrUpdateEntity(aemContentDto);

      //assertNotNull(result);
      //assertEquals(HttpStatus.SC_OK, result.getResponseCode());
      //assertEquals("Entity Created", result.getResponseBody());
    }
  }

  @Test
  public void testDeleteEntity_Success() throws DrupalException {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      AemContentDto aemContentDto = new AemContentDto();
      String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";

      ApiResponse response1 = mock(ApiResponse.class);
      response1.setResponseBody(responseBody);
      response1.setResponseCode(HttpStatus.SC_CREATED);

      ApiResponse response2 = mock(ApiResponse.class);
      response2.setResponseBody("CsrfToken");
      response2.setResponseCode(HttpStatus.SC_CREATED);

      ApiResponse response3 = mock(ApiResponse.class);
      response2.setResponseBody("Entity Created");
      response2.setResponseCode(HttpStatus.SC_CREATED);

      when(RestApiUtil.doDrupalTokenGet(anyString(),  anyString(), anyString())).thenReturn(response1);
      when(RestApiUtil.doDrupalCsrfTokenGet(anyString())).thenReturn(response3);
      when(RestApiUtil.doDrupalDeleteEntity(anyString(),any(), anyString(), anyString())).thenReturn(response3);

      ApiResponse result = this.service.deleteEntity(COMMUNITY_EVENT_PAGE_PATH);

      //assertNotNull(result);
      //assertEquals(HttpStatus.SC_OK, result.getResponseCode());
      //assertEquals("Entity Created", result.getResponseBody());
    }
  }
}
