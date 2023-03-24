package com.workday.community.aem.core.services.impl;

import java.io.IOException;
import java.util.HashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.CoveoIndexApiConfig;
import com.workday.community.aem.core.services.HttpsURLConnectionService;
import com.workday.community.aem.core.services.CoveoSourceApiService;
import com.workday.community.aem.core.constants.RestApiConstants;
import static com.workday.community.aem.core.constants.RestApiConstants.BEARER_TOKEN;

/**
 * The Class CoveoSourceApiServiceImpl.
 */
@Component(
    service = CoveoSourceApiService.class,
    immediate = true
)
@Designate(ocd = CoveoIndexApiConfig.class)
public class CoveoSourceApiServiceImpl implements CoveoSourceApiService {

    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The source api uri. */
    private String sourceApiUri;
    
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
        this.sourceApiUri = coveoIndexApiConfig.sourceApiUri();
        this.organizationId = coveoIndexApiConfig.organizationId();
        this.apiKey = coveoIndexApiConfig.apiKey();
        this.sourceId = coveoIndexApiConfig.sourceId();
    }


    @Override
    public String generateSourceApiUri() {
        return this.sourceApiUri + this.organizationId + "/sources/" + this.sourceId;
    }

    /**
	 * Generate the api header.
	 *
	 * @return The api header
	 */
    protected HashMap<String, String> generateHeader() {
        HashMap<String, String> header = new HashMap<String, String>();
        header.put(RestApiConstants.CONTENT_TYPE, RestApiConstants.APPLICATION_SLASH_JSON);
        header.put(RestApiConstants.ACCEPT, RestApiConstants.APPLICATION_SLASH_JSON);
        header.put(RestApiConstants.AUTHORIZATION, BEARER_TOKEN.token(this.apiKey));
        return header;
    }

    @Override
    public HashMap<String, Object> callApi() {
        return restApiService.send(this.generateSourceApiUri(), generateHeader(), "GET", ""); 
    }

    @Override
    public long getTotalIndexedNumber() {
        // Coveo reference https://docs.coveo.com/en/65/index-content/get-detailed-information-about-a-source.
        long totalNumberOfIndexedItems = -1;
        HashMap<String, Object> response = this.callApi();
        if ((Integer) response.get("statusCode") == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();
            try {
                JsonParser jsonParser = factory.createParser(response.get("response").toString());
                JsonNode node = mapper.readTree(jsonParser);
                JsonNode innerNode = node.get("information");                
                JsonNode numberField = innerNode.get("numberOfDocuments");
                totalNumberOfIndexedItems = numberField.asLong();
            }
            catch (IOException e) {
                logger.error("Parse coveo source api call response failed: {}", e.getMessage());
                return totalNumberOfIndexedItems;
            }
        }
        else {
            logger.error("Get number of indexed pages from coveo failed with status code {}: {}", response.get("statusCode"), response.get("response").toString());
        }
        return totalNumberOfIndexedItems;
    }
}
