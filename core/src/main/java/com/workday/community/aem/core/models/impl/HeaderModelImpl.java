package com.workday.community.aem.core.models.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.CONTENT_TYPE_MAPPING;
import static com.workday.community.aem.core.constants.GlobalConstants.PUBLISH;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.drew.lang.annotations.NotNull;
import com.google.gson.Gson;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
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

/**
 * The model implementation class for the common nav header menus.
 */
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    adapters = {HeaderModel.class},
    resourceType = {HeaderModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class HeaderModelImpl implements HeaderModel {

  /**
   * The react header resource.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/react/header";

  /**
   * Unauthenticated user's menu data.
   */
  protected static final String UNAUTHENTICATED_MENU = "HIDE_MENU_UNAUTHENTICATED";

  /**
   * Default search redirect URL.
   */
  private static final String DEFAULT_SEARCH_REDIRECT =
      "https://resourcecenter.workday.com/en-us/wrc/home/search.html";

  /**
   * The logger.
   */
  private static final Logger logger = LoggerFactory.getLogger(HeaderModelImpl.class);

  /**
   * The navMenuApi service.
   */
  @NotNull
  @OSGiService
  SnapService snapService;

  /**
   * The run mode config service.
   */
  @OSGiService
  RunModeConfigService runModeConfigService;

  /**
   * The Search API config service.
   */
  @OSGiService
  SearchApiConfigService searchApiConfigService;

  @OSGiService
  UserService userService;

  /**
   * SFID.
   */
  String sfId;

  /**
   * The global search url.
   */
  String globalSearchUrl;

  private final Gson gson = new Gson();

  @Self
  private SlingHttpServletRequest request;

  @SlingObject
  private SlingHttpServletResponse response;

  @Inject
  private Page currentPage;

  @PostConstruct
  protected void init() {
    logger.debug("Initializing HeaderModel ....");
    sfId = OurmUtils.getSalesForceId(request, userService);
  }

  /**
   * Calls the snapService to get header menu data.
   *
   * @return Nav menu as string.
   */
  public String getUserHeaderMenus() {
    try {
      User user = userService.getCurrentUser(request);
      if (user == null || (UserConstants.DEFAULT_ANONYMOUS_ID).equals(user.getID())) {
        return UNAUTHENTICATED_MENU;
      } else {
        logger.debug("Current logged in user " + user.getID());
      }
    } catch (CacheException | RepositoryException e) {
      logger.debug("Unable to check user session.");
      return UNAUTHENTICATED_MENU;
    }

    if (!snapService.enableCache()) {
      // Get a chance to disable browser cache if needed.
      return snapService.getUserHeaderMenu(sfId);
    }

    Cookie menuCache = request.getCookie("cacheMenu");
    String cookieValueFromRequest = menuCache == null ? null : menuCache.getValue();
    String cookieValueCurrentUser = userService.getUserUuid(sfId);

    if (!StringUtils.isEmpty(cookieValueCurrentUser) && !StringUtils.isEmpty(cookieValueFromRequest)
        && cookieValueFromRequest.equals(cookieValueCurrentUser)) {
      // Same user and well cached in browser
      return "";
    }

    String headerMenu = this.snapService.getUserHeaderMenu(sfId);
    if (StringUtils.isEmpty(headerMenu) || OurmUtils.isMenuEmpty(gson, headerMenu)
        || cookieValueCurrentUser != null) {
      cookieValueCurrentUser = "FALSE";
    }

    Cookie finalCookie;
    if (menuCache != null) {
      // Update existing cookie value and send back
      menuCache.setValue(cookieValueCurrentUser);
      finalCookie = menuCache;
    } else {
      // Create new cookie and setback.
      finalCookie = new Cookie("cacheMenu", cookieValueCurrentUser);
    }
    // set the cookie at root level.
    finalCookie.setPath("/");
    // set a default expire to 2 hour
    finalCookie.setMaxAge(7200);
    HttpUtils.addCookie(finalCookie, response);
    return headerMenu;
  }

  @Override
  public String getDataLayerData() {
    String instance = runModeConfigService.getInstance();
    if (instance != null && instance.equals(PUBLISH)) {
      Template template = currentPage.getTemplate();
      String pageTitle = currentPage.getTitle();
      String templatePath = template.getPath();
      String contentType = CONTENT_TYPE_MAPPING.get(templatePath);
      if (contentType == null) {
        return null;
      }
      return this.snapService.getAdobeDigitalData(sfId, pageTitle, contentType);
    }
    return null;
  }

  @Override
  public String getGlobalSearchUrl() {
    String searchUrlFromConfig = searchApiConfigService.getGlobalSearchUrl();
    globalSearchUrl = StringUtils.isBlank(searchUrlFromConfig)
        ? DEFAULT_SEARCH_REDIRECT : searchUrlFromConfig;

    return globalSearchUrl;
  }

  @Override
  public String userClientId() {
    return userService.getUserUuid(sfId);
  }
}
