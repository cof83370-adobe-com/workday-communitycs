package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.dto.RelatedInfoDto;
import com.workday.community.aem.core.dto.RelatedInfoItemDto;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.models.RelatedInfoModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.PageUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * The Class RelatedInfoModelImpl.
 */
@Slf4j
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = {
    RelatedInfoModel.class }, resourceType = {
        RelatedInfoModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RelatedInfoModelImpl implements RelatedInfoModel {

  /** The Constant RESOURCE_TYPE. */
  protected static final String RESOURCE_TYPE = "workday-community/components/common/relatedinformation";

  /** The request. */
  @Self
  private SlingHttpServletRequest request;

  /** The user group service. */
  @OSGiService
  private UserGroupService userGroupService;

  /** The user service. */
  @OSGiService
  private UserService userService;

  /** The run mode config service. */
  @OSGiService
  private RunModeConfigService runModeConfigService;

  /** The is publish instance. */
  private boolean isPublishInstance;

  /**
   * Gets the related info block data.
   *
   * @return the related info block data
   */
  @Override
  public RelatedInfoDto getRelatedInfoBlockData() {
    log.debug("Entered into getRelatedInfoBlockData method:");

    RelatedInfoDto dto = new RelatedInfoDto();
    if (request == null || userGroupService == null) {
      return dto;
    }

    Resource resource = request.getResource();
    if (resource == null) {
      return dto;
    }

    ValueMap currentResourceprops = resource.adaptTo(ValueMap.class);
    final String type = currentResourceprops.get("type", StringUtils.EMPTY);
    if (isStaticType(type)) {
      Resource itemsResource = resource.getChild("items");
      if (null != itemsResource) {
        isPublishInstance = PageUtils.isPublishInstance(runModeConfigService);
        prepareRelatedInfoLinks(itemsResource, dto);
      }
    }
    setCurrentResourceProps(resource, dto);
    log.debug("Prepared RelatedInfo block details: {}", dto);
    return dto;
  }

  /**
   * Checks if is anonymous user.
   *
   * @return true, if is anonymous user
   */
  private boolean isAnonymousUser() {
    try {
      User user = userService.getCurrentUser(request);
      if (isPublishInstance && (user == null || (UserConstants.DEFAULT_ANONYMOUS_ID).equals(user.getID()))) {
        return true;
      }
    } catch (CacheException | RepositoryException e) {
      log.error("Exception occured while getting currentUser:{}", e.getMessage());
    }
    return false;
  }

  /**
   * Prepare related info links.
   *
   * @param itemsResource the items resource
   * @param dto the dto
   */
  private void prepareRelatedInfoLinks(Resource itemsResource, RelatedInfoDto dto) {
    Iterator<Resource> resourceItr = itemsResource.listChildren();
    List<RelatedInfoItemDto> relatedInfoItemsList = new ArrayList<>();
    while (resourceItr.hasNext()) {
      Resource eachItemRes = resourceItr.next();
      ValueMap properties = eachItemRes.adaptTo(ValueMap.class);
      String pagePath = properties.get("pagepath", StringUtils.EMPTY);
      if (isValidPagePath(pagePath)) {
        RelatedInfoItemDto relatedInfoItem = new RelatedInfoItemDto();
        relatedInfoItem.setLinkTitle(properties.get("linktitle", StringUtils.EMPTY));
        relatedInfoItem.setNewTab(properties.get("newTab", StringUtils.EMPTY));
        relatedInfoItem.setPagePath(PageUtils.appendExtension(pagePath));
        relatedInfoItemsList.add(relatedInfoItem);
      }
    }
    log.debug("Prepared RelatedInfo Item links: {}", relatedInfoItemsList);
    dto.setRelatedInfoItemsList(relatedInfoItemsList);
  }

  /**
   * Sets the current resource props.
   *
   * @param resource the resource
   * @param dto      the dto
   */
  private void setCurrentResourceProps(Resource resource, RelatedInfoDto dto) {
    ValueMap currentResourceprops = resource.adaptTo(ValueMap.class);
    dto.setAnonymousUser(isAnonymousUser());
    dto.setFileReference(currentResourceprops.get("fileReference", StringUtils.EMPTY));
    dto.setDescription(currentResourceprops.get("description", StringUtils.EMPTY));
    dto.setAltText(currentResourceprops.get("alttext", StringUtils.EMPTY));
    dto.setDecorative(currentResourceprops.get("isDecorative", "false"));
    dto.setHeadingTitle(currentResourceprops.get("title", StringUtils.EMPTY));
    dto.setType(currentResourceprops.get("type", "static"));
    dto.setRows(currentResourceprops.get("rows", StringUtils.EMPTY));
    String footerLinkUrl = currentResourceprops.get("footerlinkurl", StringUtils.EMPTY);
    if (isValidPagePath(footerLinkUrl)) {
      dto.setFooterLinkUrl(PageUtils.appendExtension(currentResourceprops.get("footerlinkurl", StringUtils.EMPTY)));
      dto.setFooterLinkText(currentResourceprops.get("footerlinktext", StringUtils.EMPTY));
      dto.setFooterNewTab(currentResourceprops.get("footernewTab", StringUtils.EMPTY));
    }
  }

  /**
   * Checks if is static type.
   *
   * @param type the type
   * @return true, if is static type
   */
  private boolean isStaticType(String type) {
    return StringUtils.isNotBlank(type) && type.equalsIgnoreCase("static");
  }

  /**
   * Checks if is valid page path.
   *
   * @param pagePath the page path
   * @return true, if is valid page path
   */
  private boolean isValidPagePath(String pagePath) {
    return StringUtils.isNotBlank(pagePath)
        && (!isPublishInstance || userGroupService.validateCurrentUser(request, pagePath));
  }
}
