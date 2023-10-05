package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.HttpsURLConnectionService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import java.util.HashMap;
import org.apache.http.HttpStatus;
import org.apache.sling.api.servlets.HttpConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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

    /** The service CoveoIndexApiConfigService. */
    @Mock
    CoveoIndexApiConfigService coveoIndexApiConfigService;

    /**
     * Test generateSourceApiUri.
     */
    @Test
    public void testGenerateSourceApiUri() {
        service = this.registerService();
        String expected = coveoIndexApiConfigService.getSourceApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/sources/" + coveoIndexApiConfigService.getSourceId();
        assertEquals(expected, service.generateSourceApiUri());
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
        assertEquals(BEARER_TOKEN.token(coveoIndexApiConfigService.getCoveoApiKey()), header.get(RestApiConstants.AUTHORIZATION));
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
        coveoIndexApiConfigService = context.registerInjectActivateService(new CoveoIndexApiConfigService());
        restApiService = context.registerInjectActivateService(new HttpsURLConnectionService());
        return context.registerInjectActivateService(new CoveoSourceApiServiceImpl());
    }

}
