package com.workday.community.aem.core.listeners;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import com.workday.community.aem.core.services.RunModeConfigService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for content updates/deletes and pushes those changes to Coveo.
 */
@Slf4j
@Component(
    service = JobConsumer.class,
    immediate = true,
    property = {
        JobConsumer.PROPERTY_TOPICS + "=" + GlobalConstants.COMMUNITY_COVEO_JOB
    }
)
public class CoveoIndexJobConsumer implements JobConsumer {

  /**
   * The query service.
   */
  @Reference
  private CoveoPushApiService coveoPushApiService;

  /**
   * The extract page properties service.
   */
  @Reference
  private ExtractPagePropertiesService extractPagePropertiesService;

  /**
   * The run mode config service.
   */
  @Reference
  private RunModeConfigService runModeConfigService;

  /**
   * {@inheritDoc}
   */
  @Override
  public JobResult process(Job job) {
    List<String> paths = (ArrayList<String>) job.getProperty("paths");
    String op = (String) job.getProperty("op");
    if (paths != null) {
      if (op.equals("delete")) {
        return startCoveoDelete(paths);
      }

      if (op.equals("index")) {
        return startCoveoIndex(paths);
      }
    }

    log.error("Error occur in Coveo index job consumer, job does not have required properties: "
        + "path and op.");
    return JobResult.FAILED;
  }

  /**
   * Deletes a list of pages from Coveo.
   *
   * @param paths A list of path paths.
   * @return Whether the job was successful.
   */
  private JobResult startCoveoDelete(List<String> paths) {
    for (String path : paths) {
      String documentId =
          runModeConfigService.getPublishInstanceDomain().concat(path).concat(".html");
      Integer status = coveoPushApiService.callDeleteSingleItemUri(documentId);
      if (status != HttpStatus.SC_ACCEPTED) {
        log.error("Error occurred in coveo job consumer when deleting path: {}", path);
        return JobResult.FAILED;
      }
    }

    return JobResult.OK;
  }

  /**
   * Sends a list of pages to be indexed by Coveo.
   *
   * @param paths A list of path paths.
   * @return Whether the job was successful.
   */
  private JobResult startCoveoIndex(List<String> paths) {
    List<Object> payload = new ArrayList<>();
    for (String path : paths) {
      payload.add(extractPagePropertiesService.extractPageProperties(path));
    }
    Integer status = coveoPushApiService.indexItems(payload);
    if (status != HttpStatus.SC_ACCEPTED) {
      log.error("Error occurred in coveo job consumer when indexing paths: {}", paths.toArray());
      return JobResult.FAILED;
    }

    return JobResult.OK;
  }

}
