package com.workday.community.aem.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.OurmUserSearchConfig;
import com.workday.community.aem.core.services.OurmUsersApiConfigService;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

/**
 * The Class OurmUsersApiConfigServiceImpl.
 */
@Component(service = OurmUsersApiConfigService.class, property = {
        "service.pid=aem.core.services.ourmUsers"
}, configurationPid = "com.workday.community.aem.core.config.OurmUsersSearchConfig", configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = OurmUserSearchConfig.class)
public class OurmUsersApiConfigServiceImpl implements OurmUsersApiConfigService {
    
    /** The config. */
    private OurmUserSearchConfig config;

    /**
     * Activate.
     *
     * @param config the config
     */
    @Activate
    @Modified
    public void activate(OurmUserSearchConfig config) {
        this.config = config;
    }

    /**
     * Gets the search field lookup API.
     *
     * @return the search field lookup API
     */
    @Override
    public String getSearchFieldLookupAPI() {
        String lookupApi = config.searchFieldLookupApi();
        return lookupApi;
    }

    /**
     * Gets the search field consumer key.
     *
     * @return the search field consumer key
     */
    @Override
    public String getSearchFieldConsumerKey() {
        return config.searchFieldConsumerKey();
    }

    /**
     * Gets the search field consumer secret.
     *
     * @return the search field consumer secret
     */
    @Override
    public String getSearchFieldConsumerSecret() {
        return config.searchFieldConsumerSecret();
    }
}
