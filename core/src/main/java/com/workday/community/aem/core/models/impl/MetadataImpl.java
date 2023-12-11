package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.models.Metadata;
import java.util.Date;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

/**
 * The Class MetadataImpl.
 */
@Slf4j
@Model(adaptables = { Resource.class, SlingHttpServletRequest.class }, adapters = { Metadata.class }, resourceType = {
    MetadataImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class MetadataImpl implements Metadata {

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/common/metadata";

  /**
   * The current page.
   */
  @Inject
  private Page currentPage;

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUserName() {
    log.debug("Entered in getAuthorName method of  MetadataImpl");
    ValueMap currentPageProperties = currentPage.getProperties();
    if (null != currentPageProperties) {
      return currentPageProperties.get(GlobalConstants.USER_NAME, StringUtils.EMPTY);
    }
    return StringUtils.EMPTY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Date getPostedDate() {
    return currentPage.getProperties().get(GlobalConstants.PROP_POSTED_DATE, Date.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Date getUpdatedDate() {
    return currentPage.getProperties().get(GlobalConstants.PROP_UPDATED_DATE, Date.class);
  }
}
