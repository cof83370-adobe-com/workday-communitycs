package com.workday.community.aem.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.OurmUserGroupConfig;

/**
 * The OurmUserGroupConfigService class.
 */
@Component(
    service = OurmUserGroupConfigService.class,
    immediate = true
)
@Designate(ocd = OurmUserGroupConfig.class)
public class OurmUserGroupConfigService {

    /** The config OurmUserGroupConfig. */
    private OurmUserGroupConfig config;

    @Activate
    @Modified
    public void activate(OurmUserGroupConfig config) {
        this.config = config;
    }
    
    /**
     * Get the x api key.
     *
     * @return The x api key.
     */
    public String getXApiKey() {
        return config.xApiKey();
    }

    /**
     * Get the api uri.
     *
     * @return The api uri.
     */
    public String getApiUri() {
        return config.apiUri();
    }

    /**
     * Get the token.
     *
     * @return The bear token.
     */
    public String getToken() {
        return config.token();
    }
    
}
