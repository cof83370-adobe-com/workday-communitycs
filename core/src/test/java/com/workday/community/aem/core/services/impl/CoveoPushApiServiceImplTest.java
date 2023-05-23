package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.HttpsURLConnectionService;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class CoveoPushApiServiceImplTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoPushApiServiceImplTest {

    /** The service CoveoPushApiServiceImpl. */
    @InjectMocks
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
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/sources/" + coveoIndexApiConfigService.getSourceId() + "/documents/batch?fileId=file1";
        assertEquals(expected, service.generateBatchUploadUri("file1"));
    }

    /**
     * Test generateContainerUri.
     */
    @Test
    public void testGenerateContainerUri() {
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/files";
        assertEquals(expected, service.generateContainerUri());
    }

    /**
     * Test DeleteAllItemsUri.
     */
    @Test
    public void testGenerateDeleteAllItemsUri() {
        String uri = service.generateDeleteAllItemsUri();
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/sources/" + coveoIndexApiConfigService.getSourceId() + "/documents/olderthan?orderingId=";
        assertTrue(uri.contains(expected));
    }

    /**
     * Test generateDeleteSingleItemUri.
     */
    @Test
    public void testGenerateDeleteSingleItemUri() {
        String expected = coveoIndexApiConfigService.getPushApiUri() + coveoIndexApiConfigService.getOrganizationId() + "/sources/" + coveoIndexApiConfigService.getSourceId() + "/documents?deleteChildren=false&documentId=item1";
        assertEquals(expected, service.generateDeleteSingleItemUri("item1"));
    }

    /**
     * Test callDeleteAllItemsUri.
     */
    @Test 
    public void testCallDeleteAllItemsUri() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.SC_FORBIDDEN);
        String responseString = "{\"error\": {\"message\": \"fail\"}}"; 
        response.put("response", responseString);
        lenient().when(service.callApi(any(), any(), any(), any())).thenReturn(response);
        assertDoesNotThrow(() -> service.callDeleteAllItemsUri());
    }

    /**
     * Test callDeleteSingleItemUri.
     */
    @Test 
    public void testCallDeleteSingleItemUri() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.SC_FORBIDDEN);
        String responseString = "{\"error\": {\"message\": \"fail\"}}"; 
        response.put("response", responseString);
        lenient().when(service.callApi(any(), any(), any(), any())).thenReturn(response);
        assertDoesNotThrow(() -> service.callDeleteSingleItemUri("item1"));
    }

    /**
     * Test callApi.
     */
    @Test
    public void testCallApi() {
        String uri = "uri";
        HashMap<String, String> header = new HashMap<>();
        String httpMethod = "get";
        String payload = "";
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.SC_OK);
        response.put("response", "successed");
        lenient().when(restApiService.send(uri, header, httpMethod, payload)).thenReturn(response);
        HashMap<String, Object> expected = service.callApi(uri, header, httpMethod, payload);
        assertEquals(HttpStatus.SC_OK, expected.get("statusCode"));
        assertEquals("successed", expected.get("response"));
    }

    /**
     * Test callBatchUploadUri.
     */
    @Test
    public void testCallBatchUploadUri() {
        String fileId = "fileId";
        String uri = service.generateBatchUploadUri(fileId);
        HashMap<String, Object> response = new HashMap<>();
        response.put("statusCode", HttpStatus.SC_OK);
        response.put("response", "successed");
        String apiKey = "apiKey";
        lenient().when(coveoIndexApiConfigService.getCoveoApiKey()).thenReturn(apiKey);
        service.activate();
        HashMap<String, String> header = new HashMap<String, String>();
        header.put(RestApiConstants.CONTENT_TYPE, RestApiConstants.APPLICATION_SLASH_JSON);
        header.put(RestApiConstants.AUTHORIZATION, "Bearer apiKey");
        lenient().when(service.callApi(uri, header, "PUT", "")).thenReturn(response);
        HashMap<String, Object> expected = service.callBatchUploadUri(fileId);
        assertEquals(HttpStatus.SC_OK, expected.get("statusCode"));
        assertEquals("successed", expected.get("response"));
    }

    /**
     * Test IndexItems successed.
     */
    @Test 
    public void testIndexItemsSuccessed() {
        HashMap<String, Object> createContainerResponse = new HashMap<String, Object>();
        createContainerResponse.put("statusCode", HttpStatus.SC_CREATED);
        String response = "{\"fileId\": \"fileId\",\"requiredHeaders\": {\"additionalProp1\": \"string\"},\"uploadUri\": \"uploadUri\"}"; 
        createContainerResponse.put("response", response);
        lenient().when(service.callCreateContainerUri()).thenReturn(createContainerResponse);

        HashMap<String, Object> uploadFileResponse = new HashMap<String, Object>();
        uploadFileResponse.put("statusCode", HttpStatus.SC_OK);
        uploadFileResponse.put("response", "upload file successed");
        String uploadUri = "uploadUri";
        HashMap<String, String> uploadFileHeader = new HashMap<>();
        uploadFileHeader.put("additionalProp1", "string");
        List<Object> payload = new ArrayList<>();
        lenient().when(service.callUploadFileUri(uploadUri, uploadFileHeader, payload)).thenReturn(uploadFileResponse);

        HashMap<String, Object> batchUploadResponse = new HashMap<String, Object>();
        batchUploadResponse.put("statusCode", HttpStatus.SC_ACCEPTED);
        batchUploadResponse.put("response", "batch upload successed");
        lenient().when(service.callBatchUploadUri("fileId")).thenReturn(batchUploadResponse);
        assertEquals(202, service.indexItems(payload));
    }

    /**
     * Test IndexItems failed.
     */
    @Test 
    public void testIndexItemsFailed() {
        List<Object> payload = new ArrayList<>();

        // Create container call failed.
        HashMap<String, Object> createContainerResponse = new HashMap<String, Object>();
        createContainerResponse.put("statusCode", HttpStatus.SC_FORBIDDEN);
        createContainerResponse.put("response", "create container failed");
        lenient().when(service.callCreateContainerUri()).thenReturn(createContainerResponse);
        assertEquals(0, service.indexItems(payload));

        // Upload file call failed with status code 413.
        HashMap<String, Object> createContainerPassResponse = new HashMap<String, Object>();
        createContainerPassResponse.put("statusCode", HttpStatus.SC_CREATED);
        String response = "{\"fileId\": \"fileId\",\"requiredHeaders\": {\"additionalProp1\": \"string\"},\"uploadUri\": \"uploadUri\"}"; 
        createContainerPassResponse.put("response", response);
        lenient().when(service.callCreateContainerUri()).thenReturn(createContainerPassResponse);

        HashMap<String, Object> uploadFileFailResponse = new HashMap<String, Object>();
        uploadFileFailResponse.put("statusCode", HttpStatus.SC_REQUEST_TOO_LONG);
        uploadFileFailResponse.put("response", "upload file failed");
        String uploadUri = "uploadUri";
        HashMap<String, String> uploadFileHeader = new HashMap<>();
        uploadFileHeader.put("additionalProp1", "string");
        lenient().when(service.callUploadFileUri(uploadUri, uploadFileHeader, payload)).thenReturn(uploadFileFailResponse);
        assertEquals(-1, service.indexItems(payload));

        // Upload file call failed with other status code.
        HashMap<String, Object> uploadFileFailResponse2 = new HashMap<String, Object>();
        uploadFileFailResponse2.put("statusCode", HttpStatus.SC_BAD_GATEWAY);
        uploadFileFailResponse2.put("response", "upload file failed");
        lenient().when(service.callUploadFileUri(uploadUri, uploadFileHeader, payload)).thenReturn(uploadFileFailResponse2);
        assertEquals(0, service.indexItems(payload));

        // Batch upload failed.
        HashMap<String, Object> uploadFilePassResponse = new HashMap<String, Object>();
        uploadFilePassResponse.put("statusCode", HttpStatus.SC_OK);
        uploadFilePassResponse.put("response", "upload file successed");
        lenient().when(service.callUploadFileUri(uploadUri, uploadFileHeader, payload)).thenReturn(uploadFilePassResponse);

        HashMap<String, Object> batchUploadResponse = new HashMap<String, Object>();
        batchUploadResponse.put("statusCode", HttpStatus.SC_REQUEST_TOO_LONG);
        batchUploadResponse.put("response", "batch upload fail");
        lenient().when(service.callBatchUploadUri("fileId")).thenReturn(batchUploadResponse);
        assertEquals(0, service.indexItems(payload));
    }

    /**
     * Test transformCreateContainerResponse.
     * @throws IOException
     * @throws JsonParseException
     */
    @Test 
    public void testTransformCreateContainerResponse() throws JsonParseException, IOException {
        String response = "{\"fileId\": \"fileId\",\"requiredHeaders\": {\"additionalProp1\": \"string\"},\"uploadUri\": \"uploadUri\"}"; 
        HashMap<String, Object> result = service.transformCreateContainerResponse(response);
        assertEquals("fileId", result.get("fileId"));
        assertEquals("uploadUri", result.get("uploadUri"));
        HashMap<String, String> header = (HashMap<String, String>) result.get("requiredHeaders");
        assertEquals("string", header.get("additionalProp1"));
    }

    /**
     * Test transformPayload.
     * @throws JsonProcessingException
     */
    @Test 
    public void testTransformPayload() throws JsonProcessingException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("title", "Sample");
        ArrayList<Object> payload = new ArrayList<Object>();
        payload.add(map);
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("addOrUpdate", payload);
        String expected = "{\"addOrUpdate\":[{\"title\":\"Sample\"}]}";
        assertEquals(expected, service.transformPayload(payload));
    }
}
