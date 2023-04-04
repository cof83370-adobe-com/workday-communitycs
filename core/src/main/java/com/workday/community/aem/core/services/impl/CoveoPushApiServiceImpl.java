package com.workday.community.aem.core.services.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.CoveoIndexApiConfig;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.HttpsURLConnectionService;
import com.workday.community.aem.core.constants.RestApiConstants;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;

/**
 * The Class CoveoPushApiServiceImpl.
 */
@Component(
    service = CoveoPushApiService.class,
    immediate = true
)
@Designate(ocd = CoveoIndexApiConfig.class)
public class CoveoPushApiServiceImpl implements CoveoPushApiService {

    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The push api uri. */
    private String pushApiUri;
    
    /** The organization id. */
    private String organizationId;

    /** The api key. */
    private String apiKey;

    /** The source id. */
    private String sourceId;

    /** The HttpsURLConnectionService. */
    @Reference
    private HttpsURLConnectionService restApiService;

    @Activate
    @Modified
    protected void activate(CoveoIndexApiConfig coveoIndexApiConfig){
        this.pushApiUri = coveoIndexApiConfig.pushApiUri();
        this.organizationId = coveoIndexApiConfig.organizationId();
        this.apiKey = coveoIndexApiConfig.coveoApiKey();
        this.sourceId = coveoIndexApiConfig.sourceId();
    }

    @Override
    public String generateBatchUploadUri(String fileId) {
        return this.pushApiUri + this.organizationId + "/sources/" + this.sourceId + "/documents/batch?fileId=" + fileId;
    }

    @Override
    public String generateContainerUri() {
        return this.pushApiUri + this.organizationId + "/files";
    }

    @Override
    public String generateDeleteAllItemsUri() {
        String time = Long.toString(System.currentTimeMillis());
        return this.pushApiUri + this.organizationId + "/sources/" + this.sourceId + "/documents/olderthan?orderingId=" + time + "&queueDelay=15";
    }

    @Override
    public String generateDeleteSingleItemUri(String documentId) {
        return this.pushApiUri + this.organizationId + "/sources/" + this.sourceId + "/documents?deleteChildren=false&documentId=" + documentId;
    }

    @Override
    public HashMap<String, Object> callApi(String uri, HashMap<String, String> header, String httpMethod, String payload) {
        return restApiService.send(uri, header, httpMethod, payload); 
    }

    @Override
    public HashMap<String, Object> callBatchUploadUri(String fileId) {
        HashMap<String, String> header = new HashMap<String, String>();
        header.put(RestApiConstants.CONTENT_TYPE, RestApiConstants.APPLICATION_SLASH_JSON);
        header.put(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
        return callApi(generateBatchUploadUri(fileId), header, "PUT", "");
    }

    @Override
    public HashMap<String, Object> callCreateContainerUri() {
        HashMap<String, String> containerHeader = new HashMap<String, String>();
        containerHeader.put(RestApiConstants.CONTENT_TYPE, RestApiConstants.APPLICATION_SLASH_JSON);
        containerHeader.put(HttpConstants.HEADER_ACCEPT, RestApiConstants.APPLICATION_SLASH_JSON);
        containerHeader.put(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
        HashMap<String, Object> createContainerResponse = callApi(generateContainerUri(), containerHeader, "POST", "");
        return createContainerResponse;
    }

    @Override
    public Integer callDeleteAllItemsUri() {
        // Coveo reference https://docs.coveo.com/en/131/index-content/deleting-old-items-in-a-push-source.
        HashMap<String, String> header = new HashMap<String, String>();
        header.put(HttpConstants.HEADER_ACCEPT, RestApiConstants.APPLICATION_SLASH_JSON);
        header.put(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
        HashMap<String, Object> response = callApi(generateDeleteAllItemsUri(), header, "DELETE", "");
        if ((Integer) response.get("statusCode") != HttpStatus.SC_ACCEPTED) {
            logger.error("Deleting all items from coveo failed with status code {}: {}.", response.get("statusCode"), response.get("response").toString());
        }
        return (Integer) response.get("statusCode");
    }

    @Override
    public Integer callDeleteSingleItemUri(String documentId) {
        // Coveo reference https://docs.coveo.com/en/171/index-content/deleting-an-item-and-optionally-its-children-in-a-push-source.
        HashMap<String, String> header = new HashMap<String, String>();
        header.put(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
        HashMap<String, Object> response = callApi(generateDeleteSingleItemUri(documentId), header, "DELETE", "");
        if ((Integer) response.get("statusCode") != HttpStatus.SC_ACCEPTED) {
            logger.error("Deleting single item {} from coveo failed with status code {}: {}.", documentId, response.get("statusCode"), response.get("response").toString());
        }
        return (Integer) response.get("statusCode");
    }

    @Override
    public HashMap<String, Object> callUploadFileUri(String uploadUri, HashMap<String, String> uploadFileHeader, List<Object> payload) {
        return callApi(uploadUri, uploadFileHeader, "PUT", transformPayload(payload));
    }

    @Override
    public Integer indexItems(List<Object> payload) {
        // Coveo reference https://docs.coveo.com/en/90/index-content/manage-batches-of-items-in-a-push-source.
        Integer apiStatusCode = 0;
        HashMap<String, Object> createContainerResponse = callCreateContainerUri();
        if ((Integer) createContainerResponse.get("statusCode") == HttpStatus.SC_CREATED) {
            HashMap<String, Object> fileInfo = transformCreateContainerResponse(createContainerResponse.get("response").toString());
            String uploadUri = fileInfo.get("uploadUri").toString();
            HashMap<String, String> uploadFileHeader = (HashMap<String, String>) fileInfo.get("requiredHeaders");
            String fileId = fileInfo.get("fileId").toString();
            HashMap<String, Object> uploadFileResponse = callUploadFileUri(uploadUri, uploadFileHeader, payload);
            if ((Integer) uploadFileResponse.get("statusCode") == HttpStatus.SC_OK) {
                HashMap<String, Object> batchUploadResponse = callBatchUploadUri(fileId);
                if ((Integer) batchUploadResponse.get("statusCode") == HttpStatus.SC_ACCEPTED) {
                    return apiStatusCode = HttpStatus.SC_ACCEPTED;
                }
                else {
                    logger.error("Triggering batch ingestion failed with status code {}: {}.", batchUploadResponse.get("statusCode"), batchUploadResponse.get("response").toString()); 
                }
            }
            else if ((Integer) uploadFileResponse.get("statusCode") == HttpStatus.SC_REQUEST_TOO_LONG) {
                // Split payload.
                Integer chunckStatusCode = -1;
                List<List<Object>> chunks = ListUtils.partition(payload, payload.size() / 2 + 1);
                Iterator<List<Object>> it = chunks.iterator();
                while (it.hasNext()) {
                    Integer code = this.indexItems(it.next());
                    if (code != HttpStatus.SC_ACCEPTED || chunckStatusCode == -1) {
                        chunckStatusCode = code;
                    }  
                }
                return chunckStatusCode;
            }
            else {
                logger.error("Uploading batch file to file container failed with status code {}: {}.", uploadFileResponse.get("statusCode"), uploadFileResponse.get("response").toString());
            }
        }
        else {
            logger.error("Creating push container failed with status code {}: {}.", createContainerResponse.get("statusCode"), createContainerResponse.get("response").toString());
        }

        return apiStatusCode;
    }

    @Override
    public String transformPayload(List<Object> payload) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("addOrUpdate", payload);
        ObjectMapper mapperObj = new ObjectMapper();
        String transformedPayload = "";
        try {
            transformedPayload = mapperObj.writeValueAsString(data);
        } 
        catch (IOException e) {
            logger.error("Transform payload failed: {}.", e.getMessage());
            return transformedPayload;
        }
        return transformedPayload;
    }

    @Override
    public HashMap<String, Object> transformCreateContainerResponse(String response) {
        HashMap<String, Object> transformedResponse = new HashMap<String, Object>();

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        String requiredHeaders = "";
        String fileId = "";
        String uploadUri = "";
        try {
            JsonParser jsonParser = factory.createParser(response);
            JsonNode node = mapper.readTree(jsonParser);
            fileId = node.get("fileId").asText();
            uploadUri = node.get("uploadUri").asText();
            requiredHeaders = node.get("requiredHeaders").toPrettyString();
        }
        catch (Exception e) {
            logger.error("Parse create container reponse failed: {}", e.getMessage());
            return transformedResponse;
        }

        HashMap<String, String> header = new HashMap<String, String>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            header = objectMapper.readValue(requiredHeaders, new TypeReference<HashMap<String, String>>() {});
        } 
        catch (Exception e) {
            logger.error("Generate requiredheader array failed: {}", e.getMessage());
            return transformedResponse;
        }
        transformedResponse.put("fileId", fileId);
        transformedResponse.put("uploadUri", uploadUri);
        transformedResponse.put("requiredHeaders", header);
        return transformedResponse;
    }
    
}
