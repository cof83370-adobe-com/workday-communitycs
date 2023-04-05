package com.workday.community.aem.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.RunModeConfig;

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
}
