package com.workday.community.aem.core.services.impl;

import com.adobe.xfa.ut.StringUtils;
import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import org.apache.sling.api.resource.LoginException;
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
import java.io.IOException;
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

  /** The logger. */
  private final static Logger logger = LoggerFactory.getLogger(SnapService.class);

  /** The resource resolver factory. */
  @Reference
  ResourceResolverFactory resResolverFactory;

  /** The snap Config. */
  private SnapConfig config;

  /** The ObjectMapper serice. */
  private ObjectMapper objectMapper;

  /** The gson service. */
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

  @Override
  public String getUserHeaderMenu(String sfId) {
    String snapUrl = config.snapUrl(), navApi = config.navApi(),
      apiToken = config.navApiToken(), apiKey = config.navApiKey();

    if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(navApi) ||
      StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
      // No Snap configuration provided, just return the default one.
      return this.getDefaultHeaderMenu();
    }

    try {
      String url = CommunityUtils.formUrl(snapUrl, navApi);
      url = String.format(url, sfId);

      String traceId = "Community AEM-" + new Date().getTime();
      // Execute the request.
      APIResponse snapRes = RestApiUtil.doGetMenu(url, apiToken, apiKey, traceId);

      if (StringUtils.isEmpty(snapRes.getResponseBody())) {
        logger.debug("Sfdc menu fetch is empty, fallback to use local default");
        return this.getDefaultHeaderMenu();
      }

      // Gson object for json handling.
      JsonObject navResponseObj = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);
      // Need to make merge with beta support.
      if (config.beta()) {
        return this.getMergedHeaderMenu(navResponseObj);
      }

    } catch (SnapException e) {
      logger.error("Error in getNavUserData method call :: {}", e.getMessage());
    }

    return this.getDefaultHeaderMenu();
  }

  @Override
  public JsonObject getUserContext(String sfId) {
    try {
      logger.debug("SnapImpl: Calling SNAP getUserContext()...");
      String url = CommunityUtils.formUrl(config.snapUrl() , config.snapContextUrl());
      url = String.format(url, sfId);
      String jsonResponse = RestApiUtil.doSnapGet(url, config.snapContextApiToken(), config.snapContextApiKey());
      return gson.fromJson(jsonResponse, JsonObject.class);
    } catch (SnapException | JsonSyntaxException e) {
      logger.error("Error in getUserContext method :: {}", e.getMessage());
    }

    logger.error("User context is not fetched from the snap context API call without error, please contact admin.");

    return null;
  }

  @Override
  public ProfilePhoto getProfilePhoto(String userId) {
    String snapUrl = config.snapUrl(), avatarUrl = config.sfdcUserAvatarUrl();

    String url = CommunityUtils.formUrl(snapUrl, avatarUrl);
    url = String.format(url, userId);

    try {
      logger.info("SnapImpl: Calling SNAP getProfilePhoto(), url is {}", url);
      String jsonResponse = RestApiUtil.doSnapGet(url, config.sfdcUserAvatarToken(), config.sfdcUserAvatarApiKey());
      if (jsonResponse != null) {
        return objectMapper.readValue(jsonResponse, ProfilePhoto.class);
      }
    } catch (SnapException | JsonProcessingException e) {
      logger.error("Error in getProfilePhoto method, {} ", e.getMessage());
    }
    return null;
  }

  /**
   * Get default header menu.
   *
   * @return The menu.
   */
  private String getDefaultHeaderMenu() {
    // Reading the JSON File from DAM
    try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resResolverFactory, config.navFallbackMenuServiceUser())) {
      Resource resource = resourceResolver.getResource(config.navFallbackMenuData());
      Asset asset = resource.adaptTo(Asset.class);
      Resource original = asset.getOriginal();
      InputStream content = original.adaptTo(InputStream.class);
      if (content == null) {
        logger.error("Content is null in SnaServiceImpl.");
        return "";
      }
      StringBuilder sb = new StringBuilder();
      String line;
      BufferedReader br = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));

      while (true) {
        if ((line = br.readLine()) == null) {
          break;
        }
        sb.append(line);
      }
      content.close();
      br.close();

      JsonObject navResponseObj = gson.fromJson(sb.toString(), JsonObject.class);
      return gson.toJson(navResponseObj);
    } catch (IOException | LoginException e) {
      logger.error(String.format("Exception in SnaServiceImpl while getFailStateHeaderMenu, error: %s", e.getMessage()));
      return "";
    }
  }

  /**
   * Get merged header menu.
   * 
   * @param sfNavObj The json object of nav.
   * @return The menu.
   */
  private String getMergedHeaderMenu(JsonObject sfNavObj) {
    String defaultHeaderMenu = getDefaultHeaderMenu();
    if (sfNavObj != null && config.beta()) {
      if (defaultHeaderMenu.isEmpty()) {
        return gson.toJson(sfNavObj);
      }
      // TODO Merge local default to sfNavObj and return the merged result (once nav model strategy is finalized).
      return gson.toJson(sfNavObj);
   }

   return defaultHeaderMenu;
  }
}
