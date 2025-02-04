package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.workday.community.aem.core.config.LmsConfig;
import com.workday.community.aem.core.exceptions.LmsException;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.LmsService;
import com.workday.community.aem.core.utils.RestApiUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.annotation.Annotation;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * LmsServiceImplTest class.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class LmsServiceImplTest {

  private final LmsService service = new LmsServiceImpl();

  /**
   * Test config.
   */
  private final LmsConfig testConfig = new LmsConfig() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public String lmsUrl() {
      return "lmsUrl";
    }

    @Override
    public String lmsTokenPath() {
      return "lmsTokenPath";
    }

    @Override
    public String lmsCourseDetailPath() {
      return "lmsCourseDetailPath";
    }

    @Override
    public String lmsApiClientId() {
      return "lmsAPIClientId";
    }

    @Override
    public String lmsApiClientSecret() {
      return "lmsAPIClientSecret";
    }

    @Override
    public String lmsApiRefreshToken() {
      return "lmsAPIRefreshToken";
    }

    @Override
    public int lmsTokenCacheMax() {
      return 100;
    }

    @Override
    public long lmsTokenCacheTimeout() {
      return 1000;
    }

  };

  /**
   * Set up method for test run.
   */
  @BeforeEach
  public void setup() {
    service.activate(testConfig);
  }

  /**
   * Test method for getAPIToken method.
   *
   * @throws LmsException
   * @throws InterruptedException
   */
  @Test
  public void testGetAPIToken() throws LmsException, InterruptedException {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      // Case 1: with valid response
      ApiResponse response = new ApiResponse();

      String responseBody =
          "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"refresh_token\": \"refreshToken\"}";
      response.setResponseBody(responseBody);
      response.setResponseCode(HttpStatus.SC_OK);

      mocked.when(() -> RestApiUtil.doLmsTokenGet(anyString(), anyString(),
          anyString(), anyString())).thenReturn(response);

      String token = this.service.getApiToken();
      assertEquals("bearerToken", token);

      // Case 2: though response is null, returns from cache
      mocked.when(() -> RestApiUtil.doLmsTokenGet(anyString(), anyString(),
          anyString(), anyString())).thenReturn(null);
      token = this.service.getApiToken();
      assertEquals("bearerToken", token);
      Thread.sleep(1001);

      // Case 3: clear cache using sleep interval, with response as null
      mocked.when(() -> RestApiUtil.doLmsTokenGet(anyString(), anyString(),
          anyString(), anyString())).thenReturn(null);
      token = this.service.getApiToken();
      assertEquals("", token);
      Thread.sleep(1001);

      // Case 4: clear cache using sleep interval, response doesn't contain access
      // token
      responseBody = "{\"token_type\": \"Bearer\",\"refresh_token\": \"refreshToken\"}";
      token = this.service.getApiToken();
      assertEquals("", token);
    }
  }

  /**
   * Test method for getCourseDetail method.
   *
   * @throws LmsException
   */
  @Test
  public void testGetCourseDetail() throws LmsException {
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      // Case 1: with valid response
      ApiResponse response = new ApiResponse();
      String responseBody =
          "{\"Report_Entry\":[{\"accessControl\":\"authenticated\",\"library\":\"library\",\"groupedTitle\":\"groupedTitle\",\"languages\":\"languages\",\"roles\":\"roles\",\"productLines\":\"productLines\",\"description\":\"description\",\"durationRange\":\"durationRange\",\"deliveryOptions\":\"deliveryOptions\",\"creditsRange\":\"creditsRange\"}]}";
      response.setResponseBody(responseBody);
      response.setResponseCode(HttpStatus.SC_OK);
      mocked.when(() -> RestApiUtil.doLmsCourseDetailGet(anyString(), anyString()))
          .thenReturn(response);

      String courseDetail = this.service.getCourseDetail("groupedTitle");
      assertEquals(responseBody, courseDetail);

      // Case 2: with response without Report Entry element
      String detailResponse =
          "{\"accessControl\":\"authenticated\",\"library\":\"library\",\"groupedTitle\":\"groupedTitle\",\"languages\":\"languages\",\"roles\":\"roles\",\"productLines\":\"productLines\",\"description\":\"description\",\"durationRange\":\"durationRange\",\"deliveryOptions\":\"deliveryOptions\",\"creditsRange\":\"creditsRange\"}";
      response.setResponseBody(detailResponse);
      courseDetail = this.service.getCourseDetail("groupedTitle");
      assertEquals("", courseDetail);

      // Case 3: with response as null
      mocked.when(() -> RestApiUtil.doLmsCourseDetailGet(anyString(), anyString()))
          .thenReturn(null);
      courseDetail = this.service.getCourseDetail("groupedTitle");
      assertEquals("", courseDetail);
    }
  }
}
