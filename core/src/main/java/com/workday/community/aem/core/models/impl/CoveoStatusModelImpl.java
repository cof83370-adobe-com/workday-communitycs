package com.workday.community.aem.core.models.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.workday.community.aem.core.models.CoveoStatusModel;
import com.workday.community.aem.core.services.CoveoSourceApiService;
import com.workday.community.aem.core.services.QueryService;

/**
 * The Class CoveoStatusModelImpl.
 */
@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = {CoveoStatusModel.class},
        resourceType = {CoveoStatusModelImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoStatusModelImpl implements CoveoStatusModel {

    /** The Constant RESOURCE_TYPE. */
    final protected static String RESOURCE_TYPE = "workday-community/components/common/coveostatus";

    /** The query service. */
    @OSGiService 
    private QueryService queryService;

    /** The query service. */
    @OSGiService 
    private CoveoSourceApiService coveoSourceApiService;

    /** The total pages. */
    private long totalPages;

    /** The total indexed pages. */
    private long indexedPages;
    @ValueMapValue
    private List<String> templates;

    /** The coveo source server status. */
    private boolean serverHasError;

    @PostConstruct
    private void init() {
        totalPages = queryService.getNumOfTotalPages();
        long number = coveoSourceApiService.getTotalIndexedNumber();
        
        serverHasError = number == -1 ? true : false;
        indexedPages = number == -1 ? 0 : number;
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
    public List<String> getTemplates() {
        return new ArrayList<String>(templates);
    }

    @Override
    public boolean getServerHasError() {
        return serverHasError;
    }
}
