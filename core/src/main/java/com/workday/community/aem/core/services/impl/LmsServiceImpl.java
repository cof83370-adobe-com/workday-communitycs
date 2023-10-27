package com.workday.community.aem.core.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.LmsConfig;
import com.workday.community.aem.core.constants.LmsConstants;
import com.workday.community.aem.core.exceptions.LmsException;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.LmsService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.cache.LruCacheWithTimeout;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The OSGi service implementation for Lms API.
 */
@Slf4j
@Component(service = LmsService.class, property = {
    "service.pid=aem.core.services.lms"
}, configurationPid = "com.workday.community.aem.core.config.LmsConfig", immediate = true)
@Designate(ocd = LmsConfig.class)
public class LmsServiceImpl implements LmsService {

  /**
   * The gson service.
   */
  private final Gson gson = new Gson();

  /**
   * The snap Config.
   */
  private LmsConfig config;

  /**
   * LRU Cache for storing token value.
   */
  private LruCacheWithTimeout<String, String> lmsCache;

  /**
   * {@inheritDoc}
   */
  @Activate
  @Modified
  @Override
  public void activate(LmsConfig config) {
    this.config = config;
    this.lmsCache =
        new LruCacheWithTimeout<>(config.lmsTokenCacheMax(), config.lmsTokenCacheTimeout());
    log.debug("LmsService is activated.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getApiToken() throws LmsException {
    String cachedResult = lmsCache.get(LmsConstants.TOKEN_CACHE_KEY);
    if (StringUtils.isNotBlank(cachedResult)) {
      return cachedResult;
    }
    String lmsUrl = config.lmsUrl();
    String tokenPath = config.lmsTokenPath();
    String clientId = config.lmsApiClientId();
    String clientSecret = config.lmsApiClientSecret();
    String refreshToken = config.lmsApiRefreshToken();

    if (StringUtils.isEmpty(lmsUrl) || StringUtils.isEmpty(tokenPath)
        || StringUtils.isEmpty(clientId)
        || StringUtils.isEmpty(clientSecret)
        || StringUtils.isEmpty(refreshToken)) {
      // No Lms configuration provided, just return the default one.
      log.debug("There is no value for one or multiple configuration parameters: "
              + "lmsUrl={};tokenPath={};clientId={};clientSecret={};refreshToken={}",
          lmsUrl, tokenPath, clientId, clientSecret, refreshToken);
      return StringUtils.EMPTY;
    }

    try {
      String url = CommunityUtils.formUrl(lmsUrl, tokenPath);

      // Execute the request.
      ApiResponse lmsResponse =
          RestApiUtil.doLmsTokenGet(url, clientId, clientSecret, refreshToken);
      if (lmsResponse == null || StringUtils.isEmpty(lmsResponse.getResponseBody())
          || lmsResponse.getResponseCode() != HttpStatus.SC_OK) {
        log.error("Lms API token response is empty.");
        return StringUtils.EMPTY;
      }

      // Gson object for json handling of token response.
      JsonObject tokenResponse = gson.fromJson(lmsResponse.getResponseBody(), JsonObject.class);
      if (tokenResponse.get("access_token") == null
          || tokenResponse.get("access_token").isJsonNull()) {
        log.error("Lms API token is empty.");
        return StringUtils.EMPTY;
      }

      // Update the cache with the bearer token.
      String bearerToken = tokenResponse.get("access_token").getAsString();
      lmsCache.put(LmsConstants.TOKEN_CACHE_KEY, bearerToken);
      return bearerToken;
    } catch (LmsException | JsonSyntaxException e) {
      throw new LmsException("getApiToken call failed in LmsServiceImpl. Error: %s", e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCourseDetail(String courseTitle) throws LmsException {
    try {
      if (StringUtils.isNotBlank(courseTitle)) {
        String lmsUrl = config.lmsUrl();
        String courseDetailPath = config.lmsCourseDetailPath();
        // Get the bearer token needed for course detail API call.
        String bearerToken = getApiToken();

        // Frame the request URL.
        String url = CommunityUtils.formUrl(lmsUrl, courseDetailPath);

        // Encode title and format the URL.
        url = String.format(url, URLEncoder.encode(courseTitle, StandardCharsets.UTF_8)
            .replace(LmsConstants.PLUS, LmsConstants.ENCODED_SPACE));

        // Execute the request.
        ApiResponse lmsResponse = RestApiUtil.doLmsCourseDetailGet(url, bearerToken);
        if (lmsResponse == null || StringUtils.isEmpty(lmsResponse.getResponseBody())
            || lmsResponse.getResponseCode() != HttpStatus.SC_OK) {
          log.error("Lms API course detail response is empty.");
          return StringUtils.EMPTY;
        }

        // Gson object for json handling.
        JsonObject response = gson.fromJson(lmsResponse.getResponseBody(), JsonObject.class);
        if (response.get(LmsConstants.REPORT_ENTRY_KEY) != null
            && !response.get(LmsConstants.REPORT_ENTRY_KEY).isJsonNull()
            && !response.getAsJsonArray(LmsConstants.REPORT_ENTRY_KEY).isEmpty()) {
          return gson.toJson(response);
        }
      }
      return StringUtils.EMPTY;
    } catch (LmsException | JsonSyntaxException e) {
      throw new LmsException(
          "There is an error while fetching the course detail. Please contact Community Admin. Msg: %s",
          e.getMessage());
    }
  }

}
