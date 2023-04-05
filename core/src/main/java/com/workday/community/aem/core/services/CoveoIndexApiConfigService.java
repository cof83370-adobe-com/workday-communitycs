package com.workday.community.aem.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.CoveoIndexApiConfig;

/**
 * The CoveoIndexApiConfigService class.
 */
@Component(
    service = CoveoIndexApiConfigService.class,
    configurationPid = "com.workday.community.aem.core.config.CoveoIndexApiConfig",
    immediate = true
)
@Designate(ocd = CoveoIndexApiConfig.class)
public class CoveoIndexApiConfigService {

    /** The CoveoIndexApiConfig config. */
    private CoveoIndexApiConfig config;

    @Activate
    @Modified
    protected void activate(CoveoIndexApiConfig coveoIndexApiConfig){
        this.config = coveoIndexApiConfig;
    }

    /**
     * Get push api url.
     *
     * @return Push api url
     */
    public String getPushApiUri() {
        return config.pushApiUri();
    }

    /**
     * Get source api url.
     *
     * @return Source api url
     */
    public String getSourceApiUri() {
        return config.sourceApiUri();
    }

    /**
     * Get coveo api key.
     *
     * @return Coveo api key
     */
    public String getCoveoApiKey() {
        return config.coveoApiKey();
    }

    /**
     * Get organization Id.
     *
     * @return Organization id
     */
    public String getOrganizationId () {
        return config.organizationId();
    }

    /**
     * Get source id.
     *
     * @return Source id
     */
    public String getSourceId () {
        return config.sourceId();
    }

    /**
     * Get coveo index is enabled.
     *
     * @return Coveo index is enabled or not
     */
    public Boolean isCoveoIndexEnabled() {
        return config.isCoveoIndexingEnabled();
    }

    /**
     * Get batch size.
     *
     * @return Batch size
     */
    public Integer getBatchSize() {
        return config.batchSize();
    }
    
}
