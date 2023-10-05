package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.RunModeConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The RunModeConfigService class.
 */
@Component(
    service = RunModeConfigService.class,
    immediate = true,
    configurationPid = "com.workday.community.aem.core.config.RunModeConfig"
)
@Designate(ocd = RunModeConfig.class)
public class RunModeConfigService {

    /** The RunModeConfig. */
    private RunModeConfig config;

    @Activate
    @Modified
    public void activate(RunModeConfig config) {
        this.config = config;
    }

    /**
     * Get running environment.
     *
     * @return The Environment.
     */
    public String getEnv() {
        return config.env();
    }

    /**
     * Get instance.
     *
     * @return The instance.
     */
    public String getInstance() {
        return config.instance();
    }

    /**
     * Get adobe analytics script uri.
     *
     * @return The adobe analytics script uri.
     */
    public String getAdobeAnalyticsUri() {
        return config.adobeAnalyticsUri();
    }

    /**
     * Get publish instance domain.
     *
     * @return The publishing instance domain.
     */
    public String getPublishInstanceDomain() {
        return config.publishInstanceDomain();
    }
}
