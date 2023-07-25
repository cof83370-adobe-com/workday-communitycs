package com.workday.community.aem.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.SpeakersSearchConfig;
import com.workday.community.aem.core.services.SpeakersApiConfigService;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

/**
 * The Class SpeakersApiConfigServiceImpl.
 */
@Component(service = SpeakersApiConfigService.class, property = {
        "service.pid=aem.core.services.speakers"
}, configurationPid = "com.workday.community.aem.core.config.SpeakersSearchConfig", configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = SpeakersSearchConfig.class)
public class SpeakersApiConfigServiceImpl implements SpeakersApiConfigService {
    
    /** The config. */
    private SpeakersSearchConfig config;

    /**
     * Activate.
     *
     * @param config the config
     */
    @Activate
    @Modified
    public void activate(SpeakersSearchConfig config) {
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
