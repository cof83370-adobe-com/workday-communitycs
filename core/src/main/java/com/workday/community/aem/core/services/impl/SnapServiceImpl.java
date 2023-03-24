package com.workday.community.aem.core.services.impl;

import com.adobe.xfa.ut.StringUtils;
import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.ResolverUtil;
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

  private final Gson gson = new Gson();

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
    String snapUrl = config.snapUrl(), navApi = config.navApi(),
      apiToken = config.navApiToken(), apiKey = config.navApiKey();

    if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(snapUrl) ||
      StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
      // No Snap configuration provided, just return the default one
      return this.getMergedHeaderMenu(null);
    }

    try {
      String url = CommunityUtils.formUrl(snapUrl, navApi);
      url = String.format("%s/%s", url, sfId);

      String traceId = "Community AEM-" + new Date().getTime();
      // Execute the request.
      APIResponse snapRes = RestApiUtil.doGetMenu(url, apiToken, apiKey, traceId);

      if (StringUtils.isEmpty(snapRes.getResponseBody())) {
        logger.debug("Sfdc menu fetch is empty, fallback to use local default");
        return this.getMergedHeaderMenu(null);
      }

      // Gson object for json handling.
      JsonObject navResponseObj = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);
      // Need to make merge with beta support.
      if (config.beta()) {
        return getMergedHeaderMenu(navResponseObj);
      }

    } catch (Exception e) {
      logger.error("Error in getNavUserData method call :: {}", e.getMessage());
    }

    return this.getMergedHeaderMenu(null);
  }

  @Override
  public JsonObject getUserContext(String sfId) {
    try {
      logger.debug("SnapImpl: Calling SNAP getUserContext()...");
      String url = String.format(config.snapUrl()+ config.snapContextUrl(), sfId);
      String jsonResponse = RestApiUtil.doSnapGet(url, config.snapContextApiToken(), config.sfdcApiKey());
      return gson.fromJson(jsonResponse, JsonObject.class);
    } catch (Exception e) {
      logger.error("Error in getUserContext method :: {}", e.getMessage());
    }

    logger.error("User context is not fetched from the snap context API call without error, please contact admin.");

    return null;
  }

  @Override
  public ProfilePhoto getProfilePhoto(String userId) {
    String snapUrl = config.snapUrl(), avatarUrl = config.sfdcUserAvatarUrl();
    if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(avatarUrl)) {
      return null;
    }

    String url = CommunityUtils.formUrl(snapUrl, avatarUrl);
    url = String.format("%s/%s", url, userId);

    try {
      logger.info("SnapImpl: Calling SNAP getProfilePhoto()..." + config.snapUrl() + config.sfdcUserAvatarUrl());
      String jsonResponse = RestApiUtil.doSnapGet(url, config.sfdcUserAvatarToken(), config.sfdcApiKey());
      if (jsonResponse != null) {
        return objectMapper.readValue(jsonResponse, ProfilePhoto.class);
      }
    } catch (Exception e) {
      logger.error("Error in getProfilePhoto method, {} ", e.getMessage());
    }
    return null;
  }

  private String getMergedHeaderMenu(JsonObject sfNavObj) {
    // Reading the JSON File from DAM
    try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resResolverFactory,
      config.fallbackMenuServiceUser())) {
      Resource resource = resourceResolver.getResource(config.fallbackMenuData());
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

      if (sfNavObj != null && config.beta()) {
         if (navResponseObj == null) {
           return gson.toJson(sfNavObj);
         }
         // TODO Merge local default to sfNavObj and return the merged result (once nav model strategy is finalized).
      }

      return gson.toJson(navResponseObj);

    } catch (Exception e) {
      logger.error(String.format("Exception in SnaServiceImpl while getFailStateHeaderMenu, error: %s", e.getMessage()));
      return "";
    }
  }
}
