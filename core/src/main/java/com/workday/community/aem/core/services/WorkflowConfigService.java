package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.WorkflowConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The WorkflowConfigService class.
 */
@Component(
    service = WorkflowConfigService.class,
    configurationPid = "com.workday.community.aem.core.config.WorkflowConfig",
    immediate = true
)
@Designate(ocd = WorkflowConfig.class)
public class WorkflowConfigService {

  /**
   * The WorkflowConfig config.
   */
  private WorkflowConfig config;

  @Activate
  @Modified
  protected void activate(WorkflowConfig workflowConfig) {
    this.config = workflowConfig;
  }

  /**
   * Get workflow notification cron.
   *
   * @return Workflow notification cron
   */
  public String getWorkflowNotificationCron() {
    return config.workflowNotificationCron();
  }

  /**
   * Get enable workflow notification review.
   *
   * @return Enable workflow notification review
   */
  public boolean enableWorkflowNotificationReview() {
    return config.enableWorkflowNotificationReview();
  }

  /**
   * Get enable workflow notification retirement.
   *
   * @return Enable workflow notification retirement
   */
  public boolean enableWorkflowNotificationRetirement() {
    return config.enableWorkflowNotificationRetirement();
  }

  /**
   * Get author domain.
   *
   * @return Author domain
   */
  public String getAuthorDomain() {
    return config.authorDomain();
  }
  
  /**
   * Get archival days.
   *
   * @return days
   */
  public int getArchivalDays() {
    return config.archivalDays();
  }

}
