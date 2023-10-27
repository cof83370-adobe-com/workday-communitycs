package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.dto.ImageLinkDto;
import com.workday.community.aem.core.models.ImageLinkModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.utils.PageUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * The Class ImageLinkModelImpl.
 */
@Slf4j
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = {
    ImageLinkModel.class }, resourceType = {
        ImageLinkModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ImageLinkModelImpl implements ImageLinkModel {

  /** The Constant RESOURCE_TYPE. */
  protected static final String RESOURCE_TYPE = "workday-community/components/common/imagelink";

  /** The request. */
  @Self
  private SlingHttpServletRequest request;

  /** The user group service. */
  @OSGiService
  private UserGroupService userGroupService;

  /** The run mode config service. */
  @OSGiService
  private RunModeConfigService runModeConfigService;

  /**
   * Gets the final list.
   *
   * @return the final list
   */
  @Override
  public List<ImageLinkDto> getFinalList() {
    List<ImageLinkDto> finalList = new ArrayList<>();
    Resource resource = request.getResource();
    log.debug("Current resource is not available:{}:{}", resource);
    if (null != resource && null != userGroupService) {
      Resource itemsResource = resource.getChild("items");
      if (null != itemsResource && itemsResource.hasChildren()) {
        prepareImageLinks(itemsResource, finalList);
      }
    } else {
      log.error("Current resource is not available");
    }
    return finalList;
  }

  
  /**
   * Prepare image links.
   *
   * @param itemsResource the items resource
   * @param finalList the final list
   */
  private void prepareImageLinks(Resource itemsResource, List<ImageLinkDto> finalList) {
    Iterator<Resource> resourceItr = itemsResource.listChildren();
    while (resourceItr.hasNext()) {
      Resource eachItemRes = resourceItr.next();
      ValueMap properties = eachItemRes.adaptTo(ValueMap.class);
      String pagePath = properties.get("pagepath", StringUtils.EMPTY);

      if (StringUtils.isNotBlank(pagePath)) {
        boolean checkAccess = !PageUtils.isPublishInstance(runModeConfigService.getInstance())
            || userGroupService.hasAccessToViewLink(pagePath, request);
        if (checkAccess) {
          ImageLinkDto imageLink = new ImageLinkDto();
          imageLink.setFileReference(properties.get("fileReference", StringUtils.EMPTY));
          imageLink.setImageAltText(properties.get("imgalttext", StringUtils.EMPTY));
          imageLink.setLinkText(properties.get("linktext", StringUtils.EMPTY));
          imageLink.setNewTab(properties.get("newTab", StringUtils.EMPTY));
          imageLink.setPagePath(PageUtils.appendExtension(pagePath));
          finalList.add(imageLink);
        }
      }
    }
  }
}