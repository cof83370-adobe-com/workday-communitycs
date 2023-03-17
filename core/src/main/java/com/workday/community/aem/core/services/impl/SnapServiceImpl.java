package com.workday.community.aem.core.services.impl;

import com.adobe.xfa.ut.StringUtils;
import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.pojos.restclient.APIRequest;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * The OSGi service implementation for snap logic.
 */
@Component(
  service = SnapService.class,
  property = {
    "service.pid=aem.core.services.snap"
  },
  configurationPolicy = ConfigurationPolicy.OPTIONAL,
  immediate = true
)
@Designate(ocd = SnapConfig.class)
public class SnapServiceImpl implements SnapService {
  private final static Logger logger = LoggerFactory.getLogger(SnapService.class);

  /**
   * The resource resolver factory.
   */
  @Reference
  ResourceResolverFactory resResolverFactory;

  private SnapConfig config;

  private ObjectMapper objectMapper;

  @Activate
  @Modified
  @Override
  public void activate(SnapConfig config) {
    this.config = config;
    this.objectMapper = new ObjectMapper();
    logger.info("SnapService is activated.");
  }

  @Override
  public void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
    this.resResolverFactory = resourceResolverFactory;
  }

  public String getUserHeaderMenu(String sfId) {
    String jsonResponse = "";

    String snapUrl = config.snapUrl(), navApi = config.navApi(),
      apiToken = config.navApiToken(), apiKey = config.navApiKey();

    if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(snapUrl) ||
      StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
      return jsonResponse;
    }

    try {
      snapUrl = snapUrl.endsWith("/") ? snapUrl.substring(0, snapUrl.length()-2) : snapUrl;
      navApi = navApi.startsWith("/") ? navApi : '/' + navApi;

      String url = String.format("%s%s/%s", snapUrl, navApi, sfId);
      String traceId = "Community AEM-" + new Date().getTime();

      // Construct the request header.
      APIRequest getUserNavigationDataReq = RestApiUtil.constructAPIRequestHeader(url,
        apiToken, apiKey, traceId);
      logger.debug("NavMenuApiServiceImpl: Calling SNAP getUserNavigationData() - " + url);

      // Execute the request.
      APIResponse snapRes = RestApiUtil.getRequest(getUserNavigationDataReq);

      if (StringUtils.isEmpty(snapRes.getResponseBody())) {
        logger.debug("Sfdc menu fetch is empty, fallback to use local default");
        return this.getFailStateHeaderMenu();
      }

      // Gson object for json handling.
      Gson gson = new Gson();
      JsonObject navResponseObj = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);
      jsonResponse = gson.toJson(navResponseObj);
    } catch (Exception e) {
      logger.error("Error in getNavUserData method call :: {}", e.getMessage());
    }

    return jsonResponse;
  }

  @Override
  public ProfilePhoto getProfilePhoto(String userId) {
    String snapUrl = config.snapUrl(), avatarUrl = config.sfdcUserAvatarUrl();
    if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(avatarUrl)) {
      return null;
    }

    snapUrl = snapUrl.endsWith("/") ? snapUrl.substring(0, snapUrl.length()-2) : snapUrl;
    avatarUrl = avatarUrl.startsWith("/") ? avatarUrl : '/' + avatarUrl;
    String url = String.format("%s%s/%s", snapUrl, avatarUrl, userId);

    try {
      logger.info("SnapImpl: Calling SNAP getProfilePhoto()..." + config.snapUrl() + config.sfdcUserAvatarUrl());
      String jsonResponse = RestApiUtil.requestSnapJsonResponse(url, config.sfdcUserAvatarToken(), config.sfdcApiKey());
      if (jsonResponse != null) {
        return objectMapper.readValue(jsonResponse, ProfilePhoto.class);
      }
    } catch (Exception e) {
      logger.error("Error in getProfilePhoto method, {} ", e.getMessage());
    }
    return null;
  }

  private String getFailStateHeaderMenu() {
    // Reading the JSON File from DAM
    try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resResolverFactory,
      "navserviceuser")) {
      Resource resource = resourceResolver.getResource("/content/dam/workday-community/FailStateHeaderData.json");
      Asset asset = resource.adaptTo(Asset.class);
      Resource original = asset.getOriginal();
      InputStream content = original.adaptTo(InputStream.class);
      if (content == null) {
        logger.error("content is null in SnaServiceImpl");
        return "";
      }
      StringBuilder sb = new StringBuilder();
      String line;
      BufferedReader br = new BufferedReader(new InputStreamReader(
        content, StandardCharsets.UTF_8));

      while (true) {
        if ((line = br.readLine()) == null) {
          break;
        }
        sb.append(line);
      }

      // Gson object for json handling.
      Gson gson = new Gson();
      JsonObject navResponseObj = gson.fromJson(sb.toString(), JsonObject.class);
      return gson.toJson(navResponseObj);
    } catch (Exception e) {
      logger.error(String.format("Exception in SnaServiceImpl while getFailStateHeaderMenu, error: %s", e.getMessage()));
      return "";
    }
  }
}
