package com.workday.community.aem.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.SpeakersSearchConfig;
import com.workday.community.aem.core.services.SpeakersApiConfigService;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

@Component(service = SpeakersApiConfigService.class, property = {
        "service.pid=aem.core.services.speakers"
}, configurationPid = "com.workday.community.aem.core.config.SpeakersSearchConfig", configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = SpeakersSearchConfig.class)
public class SpeakersApiConfigServiceImpl implements SpeakersApiConfigService {
    private SpeakersSearchConfig config;

    @Activate
    @Modified
    public void activate(SpeakersSearchConfig config) {
        this.config = config;
    }

    @Override
    public String getSearchFieldLookupAPI() {
        String lookupApi = config.searchFieldLookupApi();
        return lookupApi;
    }

    @Override
    public String getSearchFieldConsumerKey() {
        return config.searchFieldConsumerKey();
    }

    @Override
    public String getSearchFieldConsumerSecret() {
        return config.searchFieldConsumerSecret();
    }
}
