package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.oltu.oauth2.common.OAuth.ContentType.JSON;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.constants.RestApiConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.CoveoSourceApiService;
import com.workday.community.aem.core.services.HttpsUrlConnectionService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.servlets.HttpConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class CoveoSourceApiServiceImpl.
 */
@Slf4j
@Component(service = CoveoSourceApiService.class, immediate = true)
public class CoveoSourceApiServiceImpl implements CoveoSourceApiService {

  /**
   * The source api uri.
   */ private String sourceApiUri;

    /*** The organization id.
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
    this.sourceApiUri = coveoIndexApiConfigService.getSourceApiUri();
    this.organizationId = coveoIndexApiConfigService.getOrganizationId();
    this.apiKey = coveoIndexApiConfigService.getCoveoApiKey();
    this.sourceId = coveoIndexApiConfigService.getSourceId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String generateSourceApiUri() {
    return this.sourceApiUri + this.organizationId + "/sources/" + this.sourceId;
  }

  /**
   * Generate the api header.
   *
   * @return The api header
   */
  protected Map<String, String> generateHeader() {
    Map<String, String> header = new HashMap<>();
    header.put(CONTENT_TYPE, JSON);
    header.put(HttpConstants.HEADER_ACCEPT, JSON);
    header.put(AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
    return header;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> callApi() {
    return restApiService.send(this.generateSourceApiUri(), generateHeader(), HttpConstants.METHOD_GET, "");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getTotalIndexedNumber() {
    long totalNumberOfIndexedItems = -1;
    Map<String, Object> response = this.callApi();
    if ((Integer) response.get("statusCode") == HttpStatus.SC_OK) {
      ObjectMapper mapper = new ObjectMapper();
      JsonFactory factory = mapper.getFactory();
      try {
        JsonParser jsonParser = factory.createParser(response.get("response").toString());
        JsonNode node = mapper.readTree(jsonParser);
        JsonNode innerNode = node.get("information");
        JsonNode numberField = innerNode.get("numberOfDocuments");
        totalNumberOfIndexedItems = numberField.asLong();
      } catch (IOException e) {
        log.error("Parse coveo source api call response failed: {}", e.getMessage());
        return totalNumberOfIndexedItems;
      }
    } else {
      log.error("Get number of indexed pages from coveo failed with status code {}: {}",
          response.get("statusCode"), response.get("response"));
    }

    return totalNumberOfIndexedItems;
  }

}
