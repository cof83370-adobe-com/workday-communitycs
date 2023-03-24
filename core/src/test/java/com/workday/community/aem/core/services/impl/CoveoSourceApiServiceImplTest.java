package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.apache.sling.api.servlets.HttpConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.services.HttpsURLConnectionService;
import com.workday.community.aem.core.constants.RestApiConstants;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;

import io.wcm.testing.mock.aem.junit5.AemContext;

/**
 * The Class CoveoSourceApiServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
public class CoveoSourceApiServiceImplTest {
 
    /** The service CoveoSourceApiServiceImpl. */
    @Spy 
    private CoveoSourceApiServiceImpl service;

    /** The service HttpsURLConnectionService. */
    @Mock
    HttpsURLConnectionService restApiService;


    /**
     * Test generateSourceApiUri.
     */
    @Test
    public void testGenerateSourceApiUri() {
        service = this.registerService();
        assertEquals("https://www.test.com/organizationId/sources/sourceId", service.generateSourceApiUri());
    }

    /**
     * Test generateHeader.
     */
    @Test
    public void testGenerateHeader() {
        service = this.registerService();
        HashMap<String, String> header = service.generateHeader();
        assertEquals(RestApiConstants.APPLICATION_SLASH_JSON, header.get(HttpConstants.HEADER_ACCEPT));
        assertEquals(RestApiConstants.APPLICATION_SLASH_JSON, header.get(RestApiConstants.CONTENT_TYPE));
        assertEquals(BEARER_TOKEN.token("apiKey"), header.get(RestApiConstants.AUTHORIZATION));
    }

    /**
     * Test getTotalIndexedNumbe failed.
     */
    @Test void testGetTotalIndexedNumbeFailed() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", 403);
        String responseString = "{\"error\": {\"message\": \"failed\"}}"; 
        response.put("response", responseString);
        doReturn(response).when(service).callApi();
        Assertions.assertEquals(-1, service.getTotalIndexedNumber());
    }

    /**
     * Test getTotalIndexedNumbe pass.
     */
    @Test void testGetTotalIndexedNumber() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.SC_OK);
        String responseString = "{\"information\": {\"numberOfDocuments\": \"20\"}}"; 
        response.put("response", responseString);
        doReturn(response).when(service).callApi();
        Assertions.assertEquals(20, service.getTotalIndexedNumber());
    }

    /**
	 * Helper method to register service.
	 *
	 * @return The CoveoSourceApiServiceImpl instance
	 */
    private CoveoSourceApiServiceImpl registerService() {
        AemContext context = new AemContext(); 
        HashMap<String, String> properties = new HashMap<>();
        properties.put("apiKey", "apiKey");
        properties.put("sourceApiUri", "https://www.test.com/");
        properties.put("organizationId", "organizationId");
        properties.put("sourceId", "sourceId");
        restApiService = context.registerInjectActivateService(new HttpsURLConnectionService());
        return context.registerInjectActivateService(new CoveoSourceApiServiceImpl(), properties);
    }
    
}
