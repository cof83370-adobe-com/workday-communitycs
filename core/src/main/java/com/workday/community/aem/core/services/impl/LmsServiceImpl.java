package com.workday.community.aem.core.services.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.LmsConfig;
import com.workday.community.aem.core.constants.LmsConstants;
import com.workday.community.aem.core.exceptions.LmsException;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import com.workday.community.aem.core.services.LmsService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.RestApiUtil;

/**
 * The OSGi service implementation for Lms API.
 */
@Component(service = LmsService.class, property = {
        "service.pid=aem.core.services.lms"
}, configurationPid = "com.workday.community.aem.core.config.LmsConfig", immediate = true)
@Designate(ocd = LmsConfig.class)
public class LmsServiceImpl implements LmsService {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LmsServiceImpl.class);

    /** The snap Config. */
    private LmsConfig config;

    /** The gson service. */
    private final Gson gson = new Gson();

    /**
     * Activates the Lms Service class.
     */
    @Activate
    @Modified
    @Override
    public void activate(LmsConfig config) {
        this.config = config;
        LOGGER.debug("LmsService is activated.");
    }

    /**
     * Gets the Lms API Bearer Token required for course list and course detail
     * APIs.
     */
    @Override
    public String getApiToken() throws LmsException {
        String lmsUrl = config.lmsUrl(), tokenPath = config.lmsTokenPath(),
                clientId = config.lmsAPIClientId(), clientSecret = config.lmsAPIClientSecret(),
                refreshToken = config.lmsAPIRefreshToken();

        if (StringUtils.isEmpty(lmsUrl) || StringUtils.isEmpty(tokenPath) ||
                StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret) ||
                StringUtils.isEmpty(refreshToken)) {
            // No Lms configuration provided, just return the default one.
            LOGGER.debug(String.format("There is no value " +
                    "for one or multiple configuration parameters: " +
                    "lmsUrl=%s;tokenPath=%s;clientId=%s;clientSecret=%s;refreshToken=%s",
                    lmsUrl, tokenPath, clientId, clientSecret, refreshToken));
            return StringUtils.EMPTY;
        }

        try {
            String url = CommunityUtils.formUrl(lmsUrl, tokenPath);

            // Execute the request.
            APIResponse lmsResponse = RestApiUtil.doLmsTokenGet(url, clientId, clientSecret, refreshToken);
            if (lmsResponse == null || StringUtils.isEmpty(lmsResponse.getResponseBody())
                    || lmsResponse.getResponseCode() != HttpStatus.SC_OK) {
                LOGGER.error("Lms API token response is empty.");
                return StringUtils.EMPTY;
            }

            // Gson object for json handling of token response.
            JsonObject tokenResponse = gson.fromJson(lmsResponse.getResponseBody(), JsonObject.class);
            if (tokenResponse.get("access_token") == null || tokenResponse.get("access_token").isJsonNull()) {
                LOGGER.error("Lms API token is empty.");
                return StringUtils.EMPTY;
            }
            return tokenResponse.get("access_token").getAsString();
        } catch (LmsException | JsonSyntaxException e) {
            LOGGER.error("Error in getAPIToken method call :: {}", e.getMessage());
            throw new LmsException(
                    "There is an error while fetching the course detail token. Please contact Community Admin.");
        }
    }

    /**
     * Makes Lms API call and fetches the course detail data of the given course.
     */
    @Override
    public String getCourseDetail(String courseTitle) throws LmsException {
        try {
            if (StringUtils.isNotBlank(courseTitle)) {
                String lmsUrl = config.lmsUrl(), courseDetailPath = config.lmsCourseDetailPath();
                // Get the bearer token needed for course detail API call.
                String bearerToken = getApiToken();

                // Frame the request URL.
                String url = CommunityUtils.formUrl(lmsUrl, courseDetailPath);

                // Encode title and format the URL.
                url = String.format(url, URLEncoder.encode(courseTitle, StandardCharsets.UTF_8)
                        .replace(LmsConstants.PLUS, LmsConstants.ENCODED_SPACE));

                // Execute the request.
                APIResponse lmsResponse = RestApiUtil.doLmsCourseDetailGet(url, bearerToken);
                if (lmsResponse == null || StringUtils.isEmpty(lmsResponse.getResponseBody())
                        || lmsResponse.getResponseCode() != HttpStatus.SC_OK) {
                    LOGGER.error("Lms API course detail response is empty.");
                    return StringUtils.EMPTY;
                }

                // Gson object for json handling.
                JsonObject response = gson.fromJson(lmsResponse.getResponseBody(), JsonObject.class);
                if (response.get(LmsConstants.REPORT_ENTRY_KEY) != null
                        && !response.get(LmsConstants.REPORT_ENTRY_KEY).isJsonNull()) {
                    return gson.toJson(response);
                }
            }
            return StringUtils.EMPTY;
        } catch (LmsException | JsonSyntaxException e) {
            LOGGER.error("Error in getCourseDetail method call :: {}", e.getMessage());
            throw new LmsException(
                    "There is an error while fetching the course detail. Please contact Community Admin.");
        }
    }

}
