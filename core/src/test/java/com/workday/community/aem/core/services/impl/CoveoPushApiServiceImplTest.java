package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.HttpsURLConnectionService;

import io.wcm.testing.mock.aem.junit5.AemContext;

/**
 * The Class CoveoPushApiServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
public class CoveoPushApiServiceImplTest {

    /** The service CoveoPushApiServiceImpl. */
    @Spy 
    private CoveoPushApiServiceImpl service;

    /** The service HttpsURLConnectionService. */
    @Mock
    HttpsURLConnectionService restApiService;

    /** The service CoveoIndexApiConfigService. */
    @Mock
    CoveoIndexApiConfigService coveoIndexApiConfigService;

    /**
     * Test generateBatchUploadUri.
     */
    @Test
    public void testGenerateBatchUploadUri() {
        service = this.registerService();
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/sources/" + coveoIndexApiConfigService.getSourceId() + "/documents/batch?fileId=file1";
        assertEquals(expected, service.generateBatchUploadUri("file1"));
    }

    /**
     * Test generateContainerUri.
     */
    @Test
    public void testGenerateContainerUri() {
        service = this.registerService();
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/files";
        assertEquals(expected, service.generateContainerUri());
    }

    /**
     * Test DeleteAllItemsUri.
     */
    @Test
    public void testGenerateDeleteAllItemsUri() {
        service = this.registerService();
        String uri = service.generateDeleteAllItemsUri();
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/sources/" + coveoIndexApiConfigService.getSourceId() + "/documents/olderthan?orderingId=";
        assertTrue(uri.contains(expected));
    }

    /**
     * Test generateDeleteSingleItemUri.
     */
    @Test
    public void testGenerateDeleteSingleItemUri() {
        service = this.registerService();
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/sources/" + coveoIndexApiConfigService.getSourceId() + "/documents?deleteChildren=false&documentId=item1";
        assertEquals(expected, service.generateDeleteSingleItemUri("item1"));
    }

    /**
     * Test callDeleteAllItemsUri.
     */
    @Test void testCallDeleteAllItemsUri() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.SC_ACCEPTED);
        String responseString = "{\"success\": {\"message\": \"ok\"}}"; 
        response.put("response", responseString);
        doReturn(response).when(service).callApi(any(), any(), any(), any());
        assertDoesNotThrow(() -> service.callDeleteAllItemsUri());
    }

    /**
     * Test callDeleteSingleItemUri.
     */
    @Test void testCallDeleteSingleItemUri() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.SC_FORBIDDEN);
        String responseString = "{\"error\": {\"message\": \"fail\"}}"; 
        response.put("response", responseString);
        doReturn(response).when(service).callApi(any(), any(), any(), any());
        assertDoesNotThrow(() -> service.callDeleteSingleItemUri("item1"));
    }

    /**
     * Test IndexItems successed.
     */
    @Test void testIndexItemsSuccessed() {
        HashMap<String, Object> createContainerResponse = new HashMap<String, Object>();
        createContainerResponse.put("statusCode", HttpStatus.SC_CREATED);
        String response = "{\"fileId\": \"fileId\",\"requiredHeaders\": {\"additionalProp1\": \"string\"},\"uploadUri\": \"uploadUri\"}"; 
        createContainerResponse.put("response", response);
        doReturn(createContainerResponse).when(service).callCreateContainerUri();

        HashMap<String, Object> uploadFileResponse = new HashMap<String, Object>();
        uploadFileResponse.put("statusCode", HttpStatus.SC_OK);
        uploadFileResponse.put("response", "upload file successed");
        doReturn(uploadFileResponse).when(service).callUploadFileUri(any(), any(), any());

        HashMap<String, Object> batchUploadResponse = new HashMap<String, Object>();
        batchUploadResponse.put("statusCode", HttpStatus.SC_ACCEPTED);
        batchUploadResponse.put("response", "batch upload successed");
        doReturn(batchUploadResponse).when(service).callBatchUploadUri(any());
        assertEquals(202, service.indexItems(any()));
    }

    /**
     * Test IndexItems failed.
     */
    @Test void testIndexItemsFailed() {
        HashMap<String, Object> createContainerResponse = new HashMap<String, Object>();
        createContainerResponse.put("statusCode", HttpStatus.SC_FORBIDDEN);
        createContainerResponse.put("response", "create container failed");
        doReturn(createContainerResponse).when(service).callCreateContainerUri();
        assertEquals(0, service.indexItems(any()));
    }

    /**
     * Test transformCreateContainerResponse.
     */
    @Test void testTransformCreateContainerResponse() {
        service = this.registerService();
        String response = "{\"fileId\": \"fileId\",\"requiredHeaders\": {\"additionalProp1\": \"string\"},\"uploadUri\": \"uploadUri\"}"; 
        HashMap<String, Object> result = service.transformCreateContainerResponse(response);
        assertEquals("fileId", result.get("fileId"));
        assertEquals("uploadUri", result.get("uploadUri"));
        HashMap<String, String> header = (HashMap<String, String>) result.get("requiredHeaders");
        assertEquals("string", header.get("additionalProp1"));
    }

    /**
     * Test transformPayload.
     */
    @Test void testTransformPayload() {
        service = this.registerService();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("title", "Sample");
        ArrayList<Object> payload = new ArrayList<Object>();
        payload.add(map);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("addOrUpdate", payload);
        String expected = "{\"addOrUpdate\":[{\"title\":\"Sample\"}]}";
        assertEquals(expected, service.transformPayload(payload));
    }

    /**
	 * Helper method to register service.
	 *
	 * @return The CoveoPushApiServiceImpl instance.
	 */
    private CoveoPushApiServiceImpl registerService() {
        AemContext context = new AemContext(); 
        restApiService = context.registerInjectActivateService(new HttpsURLConnectionService());
        coveoIndexApiConfigService = context.registerInjectActivateService(new CoveoIndexApiConfigService());
        return context.registerInjectActivateService(new CoveoPushApiServiceImpl());
    }
    
}
