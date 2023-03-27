package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.IndexServices;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Class QueryServiceImpl.
 */
@Component(service = IndexServices.class, immediate = true)
public class IndexServicesImpl implements IndexServices {

    /** The jobManager service. */
    @Reference
    JobManager jobManager;

    /**
     * Create Index jobs for the pages.
     *
     * @param paths
     */
    public void indexPages(List<String> paths) {
        ArrayList<String> pagePaths = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            pagePaths.add(paths.get(i));
            if (i + 1 % IndexServices.BATCH_SIZE == 0) {
                if (createJobs(pagePaths)) {
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
     */
    protected boolean createJobs(ArrayList<String> pagePaths) {
        Map<String, Object> jobProperties = new HashMap<>();
        jobProperties.put("op", "index");
        jobProperties.put("paths", pagePaths);
        jobManager.addJob(GlobalConstants.COMMUNITY_COVEO_JOB, jobProperties);
        return true;
    }
}
