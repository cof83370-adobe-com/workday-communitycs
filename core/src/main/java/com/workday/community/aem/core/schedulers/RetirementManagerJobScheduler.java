package com.workday.community.aem.core.schedulers;

import com.workday.community.aem.core.services.RetirementManagerJobConfigService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobBuilder.ScheduleBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.ScheduledJobInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class RetirementManagerJobScheduler.
 */
@Slf4j
@Component(immediate = true)
public class RetirementManagerJobScheduler {

  /** The job topic. */
  private static final String TOPIC = "content/retirement/manager/job";

  /** The job manager. */
  @Reference
  private JobManager jobManager;
  
  /** The retirement manager job config service. */
  @Reference
  private RetirementManagerJobConfigService retirementManagerJobConfigService;

  /**
   * Activate RetirementManagerJobScheduler scheduler.
   */
  @Activate
  protected void activate() {
    log.debug("in activate of RetirementManagerJobScheduler");
    // Call this if you want to change schedule of the job. It will unregister and register again
    stopRetirementManagerJob();
    startRetirementManagerJob();
  }

  /**
   * This method will unschedule job.
   */
  public void stopRetirementManagerJob() {
    log.debug("in stopRetirementManagerJob: {}", TOPIC);
    Collection<ScheduledJobInfo> myJobs = jobManager.getScheduledJobs(TOPIC, 10, null);
    myJobs.forEach(sji -> sji.unschedule());
  }

  /**
   * This method will get call every day based on CRON to create and schedule job.
   */
  public void startRetirementManagerJob() {
    log.debug("in startRetirementManagerJob: {}", TOPIC);
    // Check if the scheduled job already exists.
    Collection<ScheduledJobInfo> myJobs = jobManager.getScheduledJobs(TOPIC, 1, null);
    if (myJobs.isEmpty()) {
      if (retirementManagerJobConfigService != null) {
        // Setting some properties to pass to the JOb
        Map<String, Object> jobProperties = new HashMap<>();
        jobProperties.put("customJob", "retirementManagerJob");

        JobBuilder jobBuilder = jobManager.createJob(TOPIC);
        if (jobBuilder != null) {
          jobBuilder.properties(jobProperties);

          ScheduleBuilder scheduleBuilder = jobBuilder.schedule();
          if (scheduleBuilder != null) {
            scheduleBuilder.cron(retirementManagerJobConfigService.getWorkflowNotificationCron());

            if (scheduleBuilder.add() == null) {
              log.debug(
                  "Something went wrong here, use scheduleBuilder.add(List<String>) instead");
            } else {
              log.debug("Job scheduled for: {}", TOPIC);
            }
          }
        }
      }
    }
  }
}