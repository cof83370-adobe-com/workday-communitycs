package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.HttpsUrlConnectionService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class CoveoPushApiServiceImpl.
 */
@Slf4j
@Component(service = CoveoPushApiService.class)
public class CoveoPushApiServiceImpl implements CoveoPushApiService {

  /**
   * The push api uri.
   */
  private String pushApiUri;

  /**
   * The organization id.
   */
  private String organizationId;

  /**
   * The api key.
   */
  private String apiKey;

  /**
   * The source id.
   */
  private String sourceId;

  /**
   * The HttpsURLConnectionService.
   */
  @Reference
  private HttpsUrlConnectionService restApiService;

  /**
   * The CoveoIndexApiConfigService.
   */
  @Reference
  private CoveoIndexApiConfigService coveoIndexApiConfigService;

  @Activate
  @Modified
  protected void activate() {
    this.pushApiUri = coveoIndexApiConfigService.getPushApiUri();
    this.organizationId = coveoIndexApiConfigService.getOrganizationId();
    this.apiKey = coveoIndexApiConfigService.getCoveoApiKey();
    this.sourceId = coveoIndexApiConfigService.getSourceId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String generateBatchUploadUri(String fileId) {
    return this.pushApiUri + this.organizationId + "/sources/" + this.sourceId
        + "/documents/batch?fileId=" + fileId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String generateContainerUri() {
    return this.pushApiUri + this.organizationId + "/files";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String generateDeleteAllItemsUri() {
    String time = Long.toString(System.currentTimeMillis());
    return this.pushApiUri + this.organizationId + "/sources/" + this.sourceId
        + "/documents/olderthan?orderingId=" + time + "&queueDelay=15";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String generateDeleteSingleItemUri(String documentId) {
    return this.pushApiUri + this.organizationId + "/sources/" + this.sourceId
        + "/documents?deleteChildren=false&documentId=" + documentId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> callApi(String uri, Map<String, String> header,
                                     String httpMethod, String payload) {
    return restApiService.send(uri, header, httpMethod, payload);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> callBatchUploadUri(String fileId) {
    Map<String, String> header = new HashMap<>();
    header.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    header.put(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
    return callApi(generateBatchUploadUri(fileId), header,
        org.apache.sling.api.servlets.HttpConstants.METHOD_PUT, "");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> callCreateContainerUri() {
    Map<String, String> containerHeader = new HashMap<>();
    containerHeader.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    containerHeader.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    containerHeader.put(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
    return callApi(generateContainerUri(), containerHeader,
        org.apache.sling.api.servlets.HttpConstants.METHOD_POST, "");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer callDeleteAllItemsUri() {
    Map<String, String> header = new HashMap<>();
    header.put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    header.put(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
    Map<String, Object> response = callApi(generateDeleteAllItemsUri(), header,
        org.apache.sling.api.servlets.HttpConstants.METHOD_DELETE, "");
    if ((Integer) response.get("statusCode") != HttpStatus.SC_ACCEPTED) {
      log.error("Deleting all items from coveo failed with status code {}: {}.",
          response.get("statusCode"), response.get("response"));
    }
    return (Integer) response.get("statusCode");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer callDeleteSingleItemUri(String documentId) {
    Map<String, String> header = new HashMap<>();
    header.put(HttpHeaders.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
    Map<String, Object> response = callApi(generateDeleteSingleItemUri(documentId), header,
        org.apache.sling.api.servlets.HttpConstants.METHOD_DELETE, "");
    if ((Integer) response.get("statusCode") != HttpStatus.SC_ACCEPTED) {
      log.error("Deleting single item {} from coveo failed with status code {}: {}.", documentId,
          response.get("statusCode"), response.get("response"));
    }
    return (Integer) response.get("statusCode");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> callUploadFileUri(String uploadUri,
                                                   Map<String, String> uploadFileHeader,
                                                   List<Object> payload) {
    return callApi(uploadUri, uploadFileHeader,
        org.apache.sling.api.servlets.HttpConstants.METHOD_PUT, transformPayload(payload));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer indexItems(List<Object> payload) {
    Integer apiStatusCode = 0;
    Map<String, Object> createContainerResponse = callCreateContainerUri();
    if ((Integer) createContainerResponse.get("statusCode") == HttpStatus.SC_CREATED) {
      Map<String, Object> fileInfo =
          transformCreateContainerResponse(createContainerResponse.get("response").toString());
      String uploadUri = fileInfo.get("uploadUri").toString();
      Map<String, String> uploadFileHeader =
          (HashMap<String, String>) fileInfo.get("requiredHeaders");
      String fileId = fileInfo.get("fileId").toString();
      Map<String, Object> uploadFileResponse =
          callUploadFileUri(uploadUri, uploadFileHeader, payload);
      if ((Integer) uploadFileResponse.get("statusCode") == HttpStatus.SC_OK) {
        Map<String, Object> batchUploadResponse = callBatchUploadUri(fileId);
        if ((Integer) batchUploadResponse.get("statusCode") == HttpStatus.SC_ACCEPTED) {
          return HttpStatus.SC_ACCEPTED;
        } else {
          log.error("Triggering batch ingestion failed with status code {}: {}.",
              batchUploadResponse.get("statusCode"), batchUploadResponse.get("response"));
        }
      } else if ((Integer) uploadFileResponse.get("statusCode") == HttpStatus.SC_REQUEST_TOO_LONG) {
        // Split payload.
        int chunckStatusCode = -1;
        List<List<Object>> chunks = ListUtils.partition(payload, payload.size() / 2 + 1);
        for (List<Object> chunk : chunks) {
          Integer code = this.indexItems(chunk);
          if (code != HttpStatus.SC_ACCEPTED || chunckStatusCode == -1) {
            chunckStatusCode = code;
          }
        }
        return chunckStatusCode;
      } else {
        log.error("Uploading batch file to file container failed with status code {}: {}.",
            uploadFileResponse.get("statusCode"), uploadFileResponse.get("response"));
      }
    } else {
      log.error("Creating push container failed with status code {}: {}.",
          createContainerResponse.get("statusCode"), createContainerResponse.get("response"));
    }

    return apiStatusCode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String transformPayload(List<Object> payload) {
    Map<String, Object> data = new HashMap<>();
    data.put("addOrUpdate", payload);
    ObjectMapper mapperObj = new ObjectMapper();
    String transformedPayload = "";
    try {
      transformedPayload = mapperObj.writeValueAsString(data);
    } catch (IOException e) {
      log.error("Transform payload failed: {}.", e.getMessage());
      return transformedPayload;
    }
    return transformedPayload;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> transformCreateContainerResponse(String response) {
    Map<String, Object> transformedResponse = new HashMap<>();

    ObjectMapper mapper = new ObjectMapper();
    JsonFactory factory = mapper.getFactory();
    String requiredHeaders;
    String fileId;
    String uploadUri;
    try {
      JsonParser jsonParser = factory.createParser(response);
      JsonNode node = mapper.readTree(jsonParser);
      fileId = node.get("fileId").asText();
      uploadUri = node.get("uploadUri").asText();
      requiredHeaders = node.get("requiredHeaders").toPrettyString();
    } catch (IOException e) {
      log.error("Parse create container response failed: {}", e.getMessage());
      return transformedResponse;
    }

    Map<String, String> header;
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      header = objectMapper.readValue(requiredHeaders, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      log.error("Generate requiredheader array failed: {}", e.getMessage());
      return transformedResponse;
    }
    transformedResponse.put("fileId", fileId);
    transformedResponse.put("uploadUri", uploadUri);
    transformedResponse.put("requiredHeaders", header);
    return transformedResponse;
  }

}
