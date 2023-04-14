package com.workday.community.aem.core.services.impl;

import com.adobe.xfa.ut.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.CommunityUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.commons.collections4.map.LRUMap;
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

  private JsonObject defaultMenu;

  private LRUMap<String, String> snapCache;

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
    this.snapCache = new LRUMap<>(config.maxMenuCache());
    this.objectMapper = new ObjectMapper();
    logger.info("SnapService is activated.");
  }

  @Override
  public void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory) {
    this.resResolverFactory = resourceResolverFactory;
  }

  @Override
  public String getUserHeaderMenu(String sfId) {
    String cacheKey = String.format("menu_%s", sfId);
    String cachedResult = snapCache.get(cacheKey);
    if ( cachedResult != null) {
      return cachedResult;
    }

    String snapUrl = config.snapUrl(), navApi = config.navApi(),
      apiToken = config.navApiToken(), apiKey = config.navApiKey();

    if (StringUtils.isEmpty(snapUrl) || StringUtils.isEmpty(navApi) ||
      StringUtils.isEmpty(apiToken) || StringUtils.isEmpty(apiKey)) {
      // No Snap configuration provided, just return the default one.
      return gson.toJson(this.getDefaultHeaderMenu());
    }

    try {
      String url = CommunityUtils.formUrl(snapUrl, navApi);
      url = String.format(url, sfId);

      String traceId = "Community AEM-" + new Date().getTime();
      // Execute the request.
      APIResponse snapRes = RestApiUtil.doGetMenu(url, apiToken, apiKey, traceId);
      JsonObject defaultMenu = this.getDefaultHeaderMenu();
      if (StringUtils.isEmpty(snapRes.getResponseBody())) {
        logger.debug("Sfdc menu fetch is empty, fallback to use local default");
        return gson.toJson(defaultMenu);
      }

      // Gson object for json handling.
      JsonObject sfMenu = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);
      // Need to make merge sfMenu with local cache with beta experience.
      if (config.beta()) {
        String finalMenu = this.getMergedHeaderMenu(sfMenu, defaultMenu);
        snapCache.put(cacheKey, finalMenu);
        return finalMenu;
      }

      // Non-Beta will directly return the sf menu
      return gson.toJson(sfMenu);

    } catch (SnapException e) {
      logger.error("Error in getNavUserData method call :: {}", e.getMessage());
    }

    return gson.toJson(this.getDefaultHeaderMenu());
  }

  @Override
  public JsonObject getUserContext(String sfId) {
    try {
      logger.debug("SnapImpl: Calling SNAP getUserContext()...");
      String url = CommunityUtils.formUrl(config.snapUrl() , config.snapContextPath());
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
  private JsonObject getDefaultHeaderMenu() {
    if (defaultMenu != null) {
      return defaultMenu;
    }

    // Reading the JSON File from DAM
    try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resResolverFactory, config.navFallbackMenuServiceUser())) {
      return defaultMenu = DamUtils.readJsonFromDam(resourceResolver, config.navFallbackMenuData());
    } catch (RuntimeException | LoginException e) {
      logger.error(String.format("Exception in SnaServiceImpl while getFailStateHeaderMenu, error: %s", e.getMessage()));
      return new JsonObject();
    }
  }

  /**
   * Get merged header menu.
   * 
   * @param sfNavObj The json object of nav.
   * @return The menu.
   */
  private String getMergedHeaderMenu(JsonObject sfNavObj, JsonObject defaultMenu) {
    if (sfNavObj != null && config.beta()) {
      CommonUtils.updateSourceFromTarget(sfNavObj, defaultMenu);
      return gson.toJson(sfNavObj);
    }

   return gson.toJson(sfNavObj);
  }
}
