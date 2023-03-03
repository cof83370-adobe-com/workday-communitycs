package com.workday.community.aem.core.models.impl;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import com.workday.community.aem.core.models.CoveoModel;
import com.workday.community.aem.core.services.QueryService;

/**
 * The Class CoveoModelImpl.
 */
@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = {CoveoModel.class},
        resourceType = {CoveoModelImpl.RESOURCE_TYPE})
public class CoveoModelImpl implements CoveoModel {

    /** The Constant RESOURCE_TYPE. */
    final protected static String RESOURCE_TYPE = "workday-community/components/coveo";

    /** The query service. */
    @OSGiService 
    private QueryService queryService;

    /** The total pages. */
    private long totalPages;

    /** The total indexed pages. */
    private long indexedPages;

    private boolean serverStatus;

    @PostConstruct
    private void init() {
        totalPages = queryService.getNumOfTotalPages();
        
        // @todo Once we have coveo service, then we can get dynamic data.
        serverStatus = true;
        indexedPages = 2;
    }

    @Override
    public long getTotalPages() {
        return totalPages;
    }

    @Override
    public long getIndexedPages() {
        return indexedPages;
    }

    @Override
    public float getPercentage() {
        if (totalPages == 0) {
            return (float) 0.0;
        }
        return (float) indexedPages / totalPages;
    } 

    @Override
    public boolean getServerStatus() {
        return serverStatus;
    } 
}
