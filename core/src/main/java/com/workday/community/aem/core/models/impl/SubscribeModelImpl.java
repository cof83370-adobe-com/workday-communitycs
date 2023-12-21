package com.workday.community.aem.core.models.impl;

import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_PROP;
import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_VAL;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.drew.lang.annotations.NotNull;
import com.workday.community.aem.core.models.SubscribeModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

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

  @Self
  SlingHttpServletRequest request;

  @NotNull
  @OSGiService
  private  DrupalService drupalService;

  @Inject
  private ResourceResolverFactory resourceResolverFactory;

  @NotNull
  @OSGiService
  private RunModeConfigService runModeConfigService;

  @Override
  public boolean enabled() {
    boolean configEnable =  drupalService.getConfig().enableSubscribe();
    PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);

    Page currentPage = pageManager.getContainingPage(request.getResource());
    Object ret = currentPage.getProperties().get(RETIREMENT_STATUS_PROP);
    boolean retired = ret != null && ret.equals(RETIREMENT_STATUS_VAL);
    return configEnable && !retired;
  }

  @Override
  public boolean readOnly() {
    String inst = runModeConfigService.getInstance();
    return inst != null && inst.equals("author");
  }
}
