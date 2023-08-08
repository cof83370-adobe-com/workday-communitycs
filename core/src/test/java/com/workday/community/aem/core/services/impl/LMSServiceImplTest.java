package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.config.LMSConfig;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import com.workday.community.aem.core.services.LMSService;
import com.workday.community.aem.core.utils.RestApiUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * LMSServiceImplTest class.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class LMSServiceImplTest {
    private final LMSService service = new LMSServiceImpl();

    /**
     * Test config.
     */
    private final LMSConfig testConfig = new LMSConfig() {

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
        public String lmsAPIClientId() {
            return "lmsAPIClientId";
        }

        @Override
        public String lmsAPIClientSecret() {
            return "lmsAPIClientSecret";
        }

        @Override
        public String lmsAPIRefreshToken() {
            return "lmsAPIRefreshToken";
        }

    };

    /**
     * Set up method for test run.
     */
    @BeforeEach
    public void setup() {
        ((LMSServiceImpl) service).activate(testConfig);
    }

    /**
     * Test method for getLMSAPIToken method.
     */
    @Test
    public void testGetLMSAPIToken() {
        try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
            // Case 1: with valid response
            APIResponse response = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doLMSTokenPost(anyString(), anyString(),
                    anyString(), anyString())).thenReturn(response);
            String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"refresh_token\": \"refreshToken\"}";
            when(response.getResponseBody()).thenReturn(responseBody);
            when(response.getResponseCode()).thenReturn(200);

            String token = this.service.getLMSAPIToken();
            assertEquals("bearerToken", token);

            // Case 2: with response as null
            mocked.when(() -> RestApiUtil.doLMSTokenPost(anyString(), anyString(),
                    anyString(), anyString())).thenReturn(null);
            token = this.service.getLMSAPIToken();
            assertEquals("", token);

            // Case 3: response doesn't contain access token
            responseBody = "{\"token_type\": \"Bearer\",\"refresh_token\": \"refreshToken\"}";
            token = this.service.getLMSAPIToken();
            assertEquals("", token);
        }
    }

    /**
     * Test method for getCourseDetail method.
     */
    @Test
    public void testGetCourseDetail() {
        try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
            // Case 1: with valid response
            APIResponse response = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doLMSCourseDetailGet(anyString(), anyString())).thenReturn(response);
            String responseBody = "{\"Report_Entry\":[{\"accessControl\":\"authenticated\",\"library\":\"library\",\"groupedTitle\":\"groupedTitle\",\"languages\":\"languages\",\"roles\":\"roles\",\"productLines\":\"productLines\",\"description\":\"description\",\"durationRange\":\"durationRange\",\"deliveryOptions\":\"deliveryOptions\",\"creditsRange\":\"creditsRange\"}]}";
            String detailResponse = "{\"accessControl\":\"authenticated\",\"library\":\"library\",\"groupedTitle\":\"groupedTitle\",\"languages\":\"languages\",\"roles\":\"roles\",\"productLines\":\"productLines\",\"description\":\"description\",\"durationRange\":\"durationRange\",\"deliveryOptions\":\"deliveryOptions\",\"creditsRange\":\"creditsRange\"}";
            when(response.getResponseBody()).thenReturn(responseBody);
            when(response.getResponseCode()).thenReturn(200);

            String courseDetail = this.service.getCourseDetail("groupedTitle");
            assertEquals(detailResponse, courseDetail);

            // Case 2: with response without Report Entry element
            when(response.getResponseBody()).thenReturn(detailResponse);
            courseDetail = this.service.getCourseDetail("groupedTitle");
            assertEquals("", courseDetail);

            // Case 3: with response as null
            mocked.when(() -> RestApiUtil.doLMSCourseDetailGet(anyString(), anyString())).thenReturn(null);
            courseDetail = this.service.getCourseDetail("groupedTitle");
            assertEquals("", courseDetail);
        }
    }
}
