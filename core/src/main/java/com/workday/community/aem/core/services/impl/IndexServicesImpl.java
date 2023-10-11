package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.IndexServices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * The Class IndexServicesImpl.
 */
@Component(
    service = IndexServices.class,
    immediate = true
)
public class IndexServicesImpl implements IndexServices {

  /**
   * The jobManager service.
   */
  @Reference
  JobManager jobManager;

  /**
   * The CoveoIndexApiConfigService.
   */
  @Reference
  private CoveoIndexApiConfigService coveoIndexApiConfigService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void indexPages(List<String> paths) {
    List<String> pagePaths = new ArrayList<>();
    Integer batchSize = coveoIndexApiConfigService.getBatchSize();
    for (int i = 0; i < paths.size(); i++) {
      pagePaths.add(paths.get(i));
      if (i + 1 % batchSize == 0 && createJobs(pagePaths) != null) {
        pagePaths.clear();
      }
    }

    if (!pagePaths.isEmpty()) {
      createJobs(pagePaths);
    }
  }

  /**
   * Create job with given list.
   *
   * @param pagePaths the page paths
   * @return created Job
   */
  protected Job createJobs(List<String> pagePaths) {
    Map<String, Object> jobProperties = new HashMap<>();
    jobProperties.put("op", "index");
    jobProperties.put("paths", pagePaths);
    return jobManager.addJob(GlobalConstants.COMMUNITY_COVEO_JOB, jobProperties);
  }

}
