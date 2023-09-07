package com.workday.community.aem.core.services.impl;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.utils.RestApiUtil;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;

/**
 * DrupalServiceImplTest class.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class DrupalServiceImplTest {
    private final DrupalService service = new DrupalServiceImpl();

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
    };

    /**
     * Set up method for test run.
     */
    @BeforeEach
    public void setup() {
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
            APIResponse response = mock(APIResponse.class);
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
            Thread.sleep(1001);

            // Case 3: clear cache using sleep interval, with response as null
            mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
                    anyString())).thenReturn(null);
            token = this.service.getApiToken();
            assertEquals("", token);
            Thread.sleep(1001);

            // Case 4: clear cache using sleep interval, response doesn't contain access
            // token
            responseBody = "{\"token_type\": \"Bearer\",\"expires_in\": 1799}";
            token = this.service.getApiToken();
            assertEquals("", token);
        }
    }

    /**
     * 
     * Test method for getUserData method.
     * 
     * @throws DrupalException
     */
    @Test
    public void testGetUserData() throws DrupalException {
        try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
            APIResponse tokenResponse = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
                    anyString())).thenReturn(tokenResponse);
            String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
            when(tokenResponse.getResponseBody()).thenReturn(responseBody);
            when(tokenResponse.getResponseCode()).thenReturn(200);

            // Case 1: with valid response
            APIResponse response = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

            String userDataResponse = "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
            when(response.getResponseBody()).thenReturn(userDataResponse);
            when(response.getResponseCode()).thenReturn(200);

            String userData = this.service.getUserData("sfId");
            assertEquals(userDataResponse, userData);

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
        try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
            APIResponse tokenResponse = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
                    anyString())).thenReturn(tokenResponse);
            String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
            when(tokenResponse.getResponseBody()).thenReturn(responseBody);
            when(tokenResponse.getResponseCode()).thenReturn(200);

            // Return from drupal api
            APIResponse response = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

            String userDataResponse = "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
            when(response.getResponseBody()).thenReturn(userDataResponse);
            when(response.getResponseCode()).thenReturn(200);

            String imageReturn = this.service.getUserProfileImage(DEFAULT_SFID_MASTER);
            assertEquals(imageReturn, "data:image/jpeg;base64,");
        }
    }

    /**
     * Test method for getUserTimezone method.
     */
    @Test
    public void testGetUserTimezone() {
        try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
            APIResponse tokenResponse = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
                    anyString())).thenReturn(tokenResponse);
            String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
            when(tokenResponse.getResponseBody()).thenReturn(responseBody);
            when(tokenResponse.getResponseCode()).thenReturn(200);

            // Return from drupal api
            APIResponse response = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

            String userDataResponse = "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
            when(response.getResponseBody()).thenReturn(userDataResponse);
            when(response.getResponseCode()).thenReturn(200);

            String imageReturn = this.service.getUserTimezone(DEFAULT_SFID_MASTER);
            assertEquals(imageReturn, "America/Los_Angeles");
        }
    }

    /**
     * Test method for getAdobeDigitalData method.
     */
    @Test
    public void testGetAdobeDigitalData() {
        try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
            APIResponse tokenResponse = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalTokenGet(anyString(), anyString(),
                    anyString())).thenReturn(tokenResponse);
            String responseBody = "{\"access_token\": \"bearerToken\",\"token_type\": \"Bearer\",\"expires_in\": 1799}";
            when(tokenResponse.getResponseBody()).thenReturn(responseBody);
            when(tokenResponse.getResponseCode()).thenReturn(200);

            APIResponse response = mock(APIResponse.class);
            mocked.when(() -> RestApiUtil.doDrupalUserDataGet(anyString(), anyString())).thenReturn(response);

            String userDataResponse = "{\"roles\":[\"authenticated\",\"internal_workmates\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"123\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
            when(response.getResponseBody()).thenReturn(userDataResponse);
            when(response.getResponseCode()).thenReturn(200);

            String pageTitle = "FAQ page";
            String contentType = "FAQ";
            String contactNumber = "123";
            String organizationName = "Workday";
            String adobeData = this.service.getAdobeDigitalData(DEFAULT_SFID_MASTER, pageTitle, contentType);
            assertTrue(adobeData.contains(pageTitle));
            assertTrue(adobeData.contains(contentType));
            assertTrue(adobeData.contains(contactNumber));
            assertTrue(adobeData.contains(organizationName));

            String adobeData1 = this.service.getAdobeDigitalData(DEFAULT_SFID_MASTER, pageTitle, contentType);
            assertEquals(adobeData, adobeData1);
        }
    }

}
