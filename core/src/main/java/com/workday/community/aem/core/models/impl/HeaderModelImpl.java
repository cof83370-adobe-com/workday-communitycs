package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.drew.lang.annotations.NotNull;
import com.google.gson.Gson;
import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.Cookie;

import static com.workday.community.aem.core.constants.GlobalConstants.PUBLISH;
import static com.workday.community.aem.core.constants.GlobalConstants.CONTENT_TYPE_MAPPING;

/**
 * The model implementation class for the common nav header menus.
 */
@Model(adaptables = {
    Resource.class,
    SlingHttpServletRequest.class
}, adapters = { HeaderModel.class }, resourceType = {
    HeaderModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class HeaderModelImpl implements HeaderModel {

  @Self
  private SlingHttpServletRequest request;

  @SlingObject
  private SlingHttpServletResponse response;

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/react/header";

  /**
   * Default search redirect URL.
   */
  protected static final String DEFAULT_SEARCH_REDIRECT = "https://resourcecenter.workday.com/en-us/wrc/home/search.html";

  /**
   * The logger.
   */
  private final Logger logger = LoggerFactory.getLogger(HeaderModelImpl.class);

  /**
   * The navMenuApi service.
   */
  @NotNull
  @OSGiService
  SnapService snapService;

  /** The run mode config service. */
  @OSGiService
  RunModeConfigService runModeConfigService;

  /** The Search API config service. */
  @OSGiService
  SearchApiConfigService searchApiConfigService;

  @OSGiService
  UserService userService;

  @Inject
  private Page currentPage;

  /** SFID */
  String sfId;

  private final Gson gson = new Gson();

  /** The global search url. */
  String globalSearchURL;

  @PostConstruct
  protected void init() {
    logger.debug("Initializing HeaderModel ....");
    sfId = OurmUtils.getSalesForceId(request.getResourceResolver());
  }

  /**
   * Calls the snapService to get header menu data.
   *
   * @return Nav menu as string.
   */
  public String getUserHeaderMenus() {
    Cookie menuCache = request.getCookie("cacheMenu");
    String value = menuCache == null ? null : menuCache.getValue();

    if (!StringUtils.isEmpty(value)) {
      // Same user and well cached in browser
      if (value.equals(userService.getUserUUID(sfId))) return "";
    }

    String ret = this.snapService.getUserHeaderMenu(sfId);
    Cookie cacheMenuCookie = new Cookie("cacheMenu",
        StringUtils.isEmpty(ret) || OurmUtils.isMenuEmpty(gson, ret) ?
            "FALSE" : userService.getUserUUID(sfId));
    HttpUtils.addCookie(cacheMenuCookie, response);

    return ret;
  }

  @Override
  public String getDataLayerData() {
    String instance = runModeConfigService.getInstance();
    if (instance != null && instance.equals(PUBLISH)) {
      Template template = currentPage.getTemplate();
      String pageTitle = currentPage.getTitle();
      String templatePath = template.getPath();
      String contentType = CONTENT_TYPE_MAPPING.get(templatePath);
      return this.snapService.getAdobeDigitalData(sfId, pageTitle, contentType);
    }
    return null;
  }

  @Override
  public String getGlobalSearchURL() {
    String searchURLFromConfig = searchApiConfigService.getGlobalSearchURL();
    globalSearchURL = StringUtils.isBlank(searchURLFromConfig) ? DEFAULT_SEARCH_REDIRECT : searchURLFromConfig;
    return globalSearchURL;
  }

  @Override
  public String userClientId() {
    return userService.getUserUUID(sfId);
  }
}