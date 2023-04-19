package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.drew.lang.annotations.NotNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.RunModeConfig;
import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.PAGE_NAME;
import static com.workday.community.aem.core.constants.GlobalConstants.PUBLISH;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static com.workday.community.aem.core.constants.GlobalConstants.CONTENT_TYPE_MAPPING;

import java.util.HashMap;

/**
 * The model implementation class for the common nav header menus.
 */
@Model(
  adaptables = {
    Resource.class,
    SlingHttpServletRequest.class
  },
  adapters = {HeaderModel.class},
  resourceType = {HeaderModelImpl.RESOURCE_TYPE},
  defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class HeaderModelImpl implements HeaderModel {

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/react/header";

  /**
   * The logger.
   */
  private final Logger logger = LoggerFactory.getLogger(HeaderModelImpl.class);

  @NotNull
  @SlingObject
  private ResourceResolver resourceResolver;

  /**
   * The navMenuApi service.
   */
  @NotNull
  @OSGiService
  SnapService snapService;

  @OSGiService
  RunModeConfigService runModeConfigService;

  @Inject
  private Page currentPage;

  String sfId;

  @PostConstruct
  protected void init() {
    logger.debug("Initializing HeaderModel ....");
    if (resourceResolver == null) {
      throw new RuntimeException("ResourceResolver is not injected (null) in HeaderModelImpl init method.");
    }

    sfId = OurmUtils.getSalesForceId(resourceResolver);
    if (StringUtils.isBlank(sfId)) {
      // Default fallback.
      logger.debug("Salesforce Id for current user is unavailable");
      sfId = DEFAULT_SFID_MASTER;
    }
  }

  /**
   * Calls the snapService to get header menu data.
   *
   * @return Nav menu as string.
   */
  public String getUserHeaderMenus() {
    return this.snapService.getUserHeaderMenu(sfId);
  }

  public String getUserAvatar() {
    String extension;

    try {
      ProfilePhoto photoAPIResponse = this.snapService.getProfilePhoto(sfId);
      if (photoAPIResponse != null && StringUtils.isNotBlank(photoAPIResponse.getPhotoVersionId())) {
        String content = photoAPIResponse.getBase64content();
        if (content.contains("data:image/")) {
          return content;
        }

        int lastIndex = photoAPIResponse.getFileNameWithExtension().lastIndexOf('.');
        extension = photoAPIResponse.getFileNameWithExtension().substring(lastIndex + 1).toLowerCase();
        return "data:image/" + extension + ";base64," + photoAPIResponse.getBase64content();
      }

    } catch (Exception e) {
      logger.error("Exception in getUserAvatarUrl method = {}, {}", e.getClass().getName(), e.getMessage());
    }

    return "";
  }

  @Override
  public String getDataLayerData() {
    HashMap<String, Object> digitalData = this.snapService.getAdobeDigitalData(sfId);
    String instance = runModeConfigService.getInstance();
    if (instance.equals(PUBLISH)) {
      Template template = currentPage.getTemplate();
      String pageTitle = currentPage.getTitle();
      String templatePath = template.getPath();
      String contentType = CONTENT_TYPE_MAPPING.get(templatePath);
      HashMap<String, String> pageProperties = new HashMap<String, String>();
      pageProperties.put(CONTENT_TYPE, contentType);
      pageProperties.put(PAGE_NAME, pageTitle);
      digitalData.put("page", pageProperties);
      try {
        String jsonString = String.format("{\"%s\":%s}", "digitalData", new ObjectMapper().writeValueAsString(digitalData));
        return jsonString;
      } catch (JsonProcessingException e) {
        logger.error("Unable to generate dataLayer JSON string", e);
        return null;
      }
    }
    return null;
  }
}