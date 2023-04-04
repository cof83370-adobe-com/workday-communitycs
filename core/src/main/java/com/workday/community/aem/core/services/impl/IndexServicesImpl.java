package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.CoveoIndexApiConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.IndexServices;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Class IndexServicesImpl.
 */
@Component(service = IndexServices.class, immediate = true)
public class IndexServicesImpl implements IndexServices {

    /** The jobManager service. */
    @Reference
    JobManager jobManager;

    /** Coveo index batch size. */
    int batchSize;

    /** Coveo index enabled. */
    boolean isCoveoEnabled;

    @Activate
    @Modified
    protected void activate(CoveoIndexApiConfig coveoIndexApiConfig){
        batchSize = coveoIndexApiConfig.batchSize();
        isCoveoEnabled = coveoIndexApiConfig.isCoveoIndexingEnabled();
    }

    @Override
    public boolean isCoveoEnabled() {
        return isCoveoEnabled;
    }

    @Override
    public void indexPages(List<String> paths) {
        ArrayList<String> pagePaths = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            pagePaths.add(paths.get(i));
            if (i + 1 % batchSize == 0) {
                if (createJobs(pagePaths) != null) {
                    pagePaths.clear();
                }
            }
        }

        if (!pagePaths.isEmpty()) {
            createJobs(pagePaths);
        }
    }

    /**
     * Create job with given list.
     *
     * @param pagePaths
     * @return created Job
     */
    protected Job createJobs(ArrayList<String> pagePaths) {
        Map<String, Object> jobProperties = new HashMap<>();
        jobProperties.put("op", "index");
        jobProperties.put("paths", pagePaths);
        return jobManager.addJob(GlobalConstants.COMMUNITY_COVEO_JOB, jobProperties);
    }
}
