package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.RetirementManagerJobConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The RetirementManagerJobConfigService class.
 */
@Component(
    service = RetirementManagerJobConfigService.class,
    configurationPid = "com.workday.community.aem.core.config.RetirementManagerJobConfig",
    immediate = true
)
@Designate(ocd = RetirementManagerJobConfig.class)
public class RetirementManagerJobConfigService {

  /**
   * The RetirementManagerJobConfig config.
   */
  private RetirementManagerJobConfig config;

  @Activate
  @Modified
  protected void activate(RetirementManagerJobConfig retirementManagerJobConfig) {
    this.config = retirementManagerJobConfig;
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
  public boolean getEnableWorkflowNotificationReview() {
    return config.enableWorkflowNotificationReview();
  }

  /**
   * Get enable workflow notification retirement.
   *
   * @return Enable workflow notification retirement
   */
  public boolean getEnableWorkflowNotificationRetirement() {
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

}
