package com.workday.community.aem.core.models.impl;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;

import com.workday.community.aem.core.models.FooterModel;
import com.workday.community.aem.core.services.RunModeConfigService;

/**
 * The Class FooterModelImpl.
 */
@Model(
    adaptables = {Resource.class,SlingHttpServletRequest.class},
    adapters = {FooterModel.class},
    resourceType = {FooterModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class FooterModelImpl implements FooterModel {

    /** The Constant RESOURCE_TYPE. */
    protected static final String RESOURCE_TYPE = "workday-community/components/react/footer";

    /** The run mode config service. */
    @OSGiService 
    private RunModeConfigService runModeConfigService;

    /** The adobe analytics uri. */
    String adobeAnalyticsUri;

    @PostConstruct
    protected void init() {
        adobeAnalyticsUri = runModeConfigService.getAdobeAnalyticsUri();
    }

    @Override
    public String getAdobeAnalyticsUri() {
        return adobeAnalyticsUri;
    } 
}
