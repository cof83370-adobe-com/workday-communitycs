package com.workday.community.aem.core.services.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.OurmDrupalConfig;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.services.OurmUserService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.OAuth1Util;
import com.workday.community.aem.core.utils.RestApiUtil;

import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

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

    /** The gson service. */
    private final Gson gson = new Gson();

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
     * @return the json object
     * @throws OurmException the ourm exception
     */
    @Override
    public JsonObject searchOurmUserList(String searchText) throws OurmException {

        String endpoint = this.ourmDrupalConfig.ourmDrupalRestRoot();
        String consumerKey = this.ourmDrupalConfig.ourmDrupalConsumerKey();
        String consumerSecret = this.ourmDrupalConfig.ourmDrupalConsumerSecret();
        String searchPath = this.ourmDrupalConfig.ourmDrupalUserSearchPath();
        if (StringUtils.isNotBlank(endpoint) && StringUtils.isNotBlank(consumerKey)
                && StringUtils.isNotBlank(consumerSecret) && StringUtils.isNotBlank(searchPath)) {
            try {

                String apiUrl = String.format("%s/%s", CommunityUtils.formUrl(endpoint, URLEncoder.encode(searchText, StandardCharsets.UTF_8)), searchText);
                String headerString = OAuth1Util.getHeader("GET", apiUrl, consumerKey, consumerSecret, new HashMap<>());
                LOGGER.info("OurmUserServiceImpl::searchOurmUserList - apiUrl {}", apiUrl);

                // Execute the request.
                String jsonResponse = RestApiUtil.doOURMGet(apiUrl, headerString);
                return gson.fromJson(jsonResponse, JsonObject.class);

            } catch (SnapException | InvalidKeyException | NoSuchAlgorithmException e) {
                String errorMessage = e.getMessage();
                LOGGER.error("Error Occurred in searchOurmUserList Method in OurmUserServiceImpl %s", errorMessage);
                throw new OurmException(
                        String.format("Error Occurred in searchOurmUserList Method in OurmUserServiceImpl : %s", errorMessage));
            }
        }
        return new JsonObject();
    }
}