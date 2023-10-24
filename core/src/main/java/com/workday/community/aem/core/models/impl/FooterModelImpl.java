package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.FooterModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

/**
 * The Class FooterModelImpl.
 */
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    adapters = {FooterModel.class},
    resourceType = {FooterModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class FooterModelImpl implements FooterModel {

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/react/footer";

  /**
   * The adobe analytics uri.
   */
  @Getter
  String adobeAnalyticsUri;

  /**
   * The run mode config service.
   */
  @OSGiService
  private RunModeConfigService runModeConfigService;

  @PostConstruct
  protected void init() {
    adobeAnalyticsUri = runModeConfigService.getAdobeAnalyticsUri();
  }

}
