package com.workday.community.aem.core.services.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.OurmDrupalConfig;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.pojos.OurmUserList;
import com.workday.community.aem.core.services.OurmUserService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.OAuth1Util;

import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import static com.workday.community.aem.core.constants.RestApiConstants.AUTHORIZATION;

/**
 * The Class OurmUserServiceImpl.
 */
@Component(service = OurmUserService.class, property = {
        "service.pid=aem.core.services.ourmUsers"
}, configurationPid = "com.workday.community.aem.core.config.OurmDrupalConfig", configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = OurmDrupalConfig.class)
public class OurmUserServiceImpl implements OurmUserService {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OurmUserServiceImpl.class);

    /** The ourm drupal config. */
    private OurmDrupalConfig ourmDrupalConfig;

    /** The object mapper. */
    public transient ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sets the object mapper.
     *
     * @param objectMapper the new object mapper
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Activate.
     *
     * @param config the config
     */
    @Activate
    @Modified
    @Override
    public void activate(OurmDrupalConfig config) {
        this.ourmDrupalConfig = config;
    }

    /**
     * Search ourm user list.
     *
     * @param searchText the search text
     * @return the ourm user list
     * @throws OurmException the ourm exception
     */
    @Override
    public OurmUserList searchOurmUserList(String searchText) throws OurmException {

        String endpoint = this.ourmDrupalConfig.ourmDrupalRestRoot();
        String consumerKey = this.ourmDrupalConfig.ourmDrupalConsumerKey();
        String consumerSecret = this.ourmDrupalConfig.ourmDrupalConsumerSecret();
        String searchPath = this.ourmDrupalConfig.ourmDrupalUserSearchPath();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            String apiUrl = String.format("%s/%s", CommunityUtils.formUrl(endpoint, searchPath), searchText);
            HttpGet request = new HttpGet(apiUrl);

            String headerString = OAuth1Util.getHeader("GET", apiUrl, consumerKey, consumerSecret, new HashMap<>());
            request.addHeader(AUTHORIZATION, headerString);

            HttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                OurmUserList userList = objectMapper.readValue(response.getEntity().getContent(),
                        OurmUserList.class);
                return userList;
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new OurmException(
                    String.format("Error Occurred in DoGet Method in OurmUserServiceImpl : {}", e.getMessage()));
        }
        return new OurmUserList();
    }
}
