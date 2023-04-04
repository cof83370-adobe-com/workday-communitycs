package com.workday.community.aem.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.AemRunModeConfig;

/**
 * The AemRunModeConfigService class.
 */
@Component(
    service = AemRunModeConfigService.class,
    immediate = true,
    configurationPid = "com.workday.community.aem.core.config.AemRunModeConfig"
)
@Designate(ocd = AemRunModeConfig.class)
public class AemRunModeConfigService {

    /** The AemRunModeConfig. */
    private AemRunModeConfig config;
    
    @Activate
    @Modified
    public void activate(AemRunModeConfig config) {
        this.config = config;
    }

    /**
     * Get aem running environment.
     *
     * @return Aem Environment.
     */
    public String getAemEnv() {
        return config.aemEnv();
    }

    /**
     * Get aem instance.
     *
     * @return Aem instance.
     */
    public String getAemInstance() {
        return config.aemInstance();
    }  
}
