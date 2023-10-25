package com.workday.community.aem.core.services.impl;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.oltu.oauth2.common.OAuth.ContentType.JSON;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.HttpsUrlConnectionService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import java.util.HashMap;
import java.util.Map;
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

  /**
   * The service HttpsURLConnectionService.
   */
  @Mock
  HttpsUrlConnectionService restApiService;

  /**
   * The service CoveoIndexApiConfigService.
   */
  @Mock
  CoveoIndexApiConfigService coveoIndexApiConfigService;

  /**
   * The service CoveoSourceApiServiceImpl.
   */
  @Spy
  private CoveoSourceApiServiceImpl service;

  /**
   * Test generateSourceApiUri.
   */
  @Test
  public void testGenerateSourceApiUri() {
    service = this.registerService();
    String expected = coveoIndexApiConfigService.getSourceApiUri() +
        coveoIndexApiConfigService.getOrganizationId()
                + "/sources/" +
        coveoIndexApiConfigService.getSourceId();
    assertEquals(expected, service.generateSourceApiUri());
  }

  /**
   * Test generateHeader.
   */
  @Test
  public void testGenerateHeader() {
    service = this.registerService();
    Map<String, String> header = service.generateHeader();
      assertEquals(JSON, header.get(HttpConstants.HEADER_ACCEPT));
      assertEquals(JSON, header.get(CONTENT_TYPE));
      assertEquals(BEARER_TOKEN.token(coveoIndexApiConfigService.getCoveoApiKey()), header.get(AUTHORIZATION));
  }

    /**
     * Test getTotalIndexedNumbe failed.
     */
    @Test
    void testGetTotalIndexedNumbeFailed() {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 403);
        String responseString = "{\"error\": {\"message\": \"failed\"}}";
        response.put("response", responseString);
        doReturn(response).when(service).callApi();
        Assertions.assertEquals(-1, service.getTotalIndexedNumber());
    }

    /**
     * Test getTotalIndexedNumbe pass.
     */
    @Test
    void testGetTotalIndexedNumber() {
        Map<String, Object> response = new HashMap<>();
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
        restApiService = context.registerInjectActivateService(new HttpsUrlConnectionService());
        return context.registerInjectActivateService(new CoveoSourceApiServiceImpl());
    }
}
