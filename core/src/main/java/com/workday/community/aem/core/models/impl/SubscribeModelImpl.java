package com.workday.community.aem.core.models.impl;

import com.drew.lang.annotations.NotNull;
import com.workday.community.aem.core.models.SubscribeModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

/**
 * Subscribe model implementation class.
 */
@Slf4j
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    adapters = {SubscribeModel.class},
    resourceType = {SubscribeModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.REQUIRED
)
public class SubscribeModelImpl implements SubscribeModel {
  /**
   * The Subscribe resource.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/react/subscribe";

  @NotNull
  @OSGiService
  private  DrupalService drupalService;

  @NotNull
  @OSGiService
  private RunModeConfigService runModeConfigService;

  @Override
  public boolean enabled() {
    return drupalService.getConfig().enableSubscribe();
  }

  @Override
  public boolean readOnly() {
    String inst = runModeConfigService.getInstance();
    return inst != null && inst.equals("author");
  }
}
