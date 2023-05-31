package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.drew.lang.annotations.NotNull;
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
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/react/header";

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

  @OSGiService
  RunModeConfigService runModeConfigService;

  @Inject
  private Page currentPage;

  String sfId;

  @PostConstruct
  protected void init() {
    logger.debug("Initializing HeaderModel ....");
    ResourceResolver resourceResolver = request.getResourceResolver();
    if (resourceResolver == null) {
      throw new RuntimeException("ResourceResolver is not injected (null) in HeaderModelImpl init method.");
    }

    sfId = OurmUtils.getSalesForceId(resourceResolver);
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
}