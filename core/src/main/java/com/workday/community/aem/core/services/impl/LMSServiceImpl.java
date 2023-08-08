package com.workday.community.aem.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.LMSConfig;
import com.workday.community.aem.core.exceptions.RestAPIException;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import com.workday.community.aem.core.services.LMSService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.RestApiUtil;

/**
 * The OSGi service implementation for LMS API.
 */
@Component(service = LMSService.class, property = {
        "service.pid=aem.core.services.lms"
}, configurationPid = "com.workday.community.aem.core.config.LMSConfig", immediate = true)
@Designate(ocd = LMSConfig.class)
public class LMSServiceImpl implements LMSService {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(LMSServiceImpl.class);

    /** The snap Config. */
    private LMSConfig config;

    /** The gson service. */
    private final Gson gson = new Gson();

    /**
     * Activates the LMS Service class.
     */
    @Activate
    @Modified
    @Override
    public void activate(LMSConfig config) {
        this.config = config;
        logger.info("LMSService is activated.");
    }

    /**
     * Fetches the training catalog data.
     */
    @Override
    public String getTrainingCatalogData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTrainingCatalogData'");
    }

    /**
     * Gets the LMS API Bearer Token required for course list and course detail
     * APIs.
     */
    @Override
    public String getLMSAPIToken() {
        String lmsUrl = config.lmsUrl(), tokenPath = config.lmsTokenPath(),
                clientId = config.lmsAPIClientId(), clientSecret = config.lmsAPIClientSecret(),
                refreshToken = config.lmsAPIRefreshToken();

        if (StringUtils.isEmpty(lmsUrl) || StringUtils.isEmpty(tokenPath) ||
                StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret) ||
                StringUtils.isEmpty(refreshToken)) {
            // No LMS configuration provided, just return the default one.
            logger.debug(String.format("There is no value " +
                    "for one or multiple configuration parameters: " +
                    "lmsUrl=%s;tokenPath=%s;clientId=%s;clientSecret=%s;refreshToken=%s",
                    lmsUrl, tokenPath, clientId, clientSecret, refreshToken));
            return StringUtils.EMPTY;
        }

        try {
            String url = CommunityUtils.formUrl(lmsUrl, tokenPath);

            // Execute the request.
            APIResponse lmsResponse = RestApiUtil.doLMSTokenPost(url, clientId, clientSecret, refreshToken);
            if (lmsResponse == null || StringUtils.isEmpty(lmsResponse.getResponseBody())
                    || lmsResponse.getResponseCode() != HttpStatus.SC_OK) {
                logger.error("LMS API token response is empty.");
                return StringUtils.EMPTY;
            }

            // Gson object for json handling of token response.
            JsonObject tokenResponse = gson.fromJson(lmsResponse.getResponseBody(), JsonObject.class);
            if (tokenResponse.get("access_token") == null || tokenResponse.get("access_token").isJsonNull()) {
                logger.error("LMS API token is empty.");
                return StringUtils.EMPTY;
            }
            return tokenResponse.get("access_token").getAsString();
        } catch (RestAPIException | JsonSyntaxException e) {
            logger.error("Error in getLMSAPIToken method call :: {}", e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    /**
     * Makes LMS API call and fetches the course detail data of the given course.
     */
    @Override
    public String getCourseDetail(String courseTitle) {
        if (StringUtils.isNotBlank(courseTitle)) {
            String lmsUrl = config.lmsUrl(), courseDetailPath = config.lmsCourseDetailPath();

            try {
                // Get the bearer token needed for course detail API call.
                String bearerToken = getLMSAPIToken();

                // Frame the request URL.
                String url = CommunityUtils.formUrl(lmsUrl, courseDetailPath);
                url = String.format(url, courseTitle);

                // Execute the request.
                APIResponse lmsResponse = RestApiUtil.doLMSCourseDetailGet(url, bearerToken);
                if (lmsResponse == null || StringUtils.isEmpty(lmsResponse.getResponseBody())
                        || lmsResponse.getResponseCode() != HttpStatus.SC_OK) {
                    logger.error("LMS API course detail response is empty.");
                    return StringUtils.EMPTY;
                }

                // Gson object for json handling.
                JsonObject response = gson.fromJson(lmsResponse.getResponseBody(), JsonObject.class);
                if (response.get("Report_Entry") != null && !response.get("Report_Entry").isJsonNull()) {
                    JsonArray detailArray = response.get("Report_Entry").getAsJsonArray();
                    if (detailArray.size() > 0) {
                        return gson.toJson(detailArray.get(0));
                    }
                }

                return StringUtils.EMPTY;

            } catch (RestAPIException | JsonSyntaxException e) {
                logger.error("Error in getCourseDetail method call :: {}", e.getMessage());
            }
        }
        return StringUtils.EMPTY;
    }

}
