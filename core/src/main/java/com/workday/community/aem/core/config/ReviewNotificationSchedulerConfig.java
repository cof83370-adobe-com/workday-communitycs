package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Interface ReviewNotificationSchedulerConfig.
 */
@ObjectClassDefinition(
  name = "Review Notification Scheduler Configuration",
  description = "Paramerters for Review Notification Scheduler"
)
public @interface ReviewNotificationSchedulerConfig {
  
  /**
   * Scheduler name.
   *
   * @return the string
   */
  @AttributeDefinition(name = "Scheduler name", description = "Scheduler name", type = AttributeType.STRING)
  String schedulerName() default "ReviewNotificationScheduler";

  /**
   * Workflow notification cron.
   *
   * @return the string
   */
  @AttributeDefinition(name = "Cron job expression", description = "Cron job expression", type = AttributeType.STRING)
  String workflowNotificationCron() default "0 0 18 * * ?";

  /**
   * Workflow notification review 10 months.
   *
   * @return true, if successful
   */
  @AttributeDefinition(name = "Enable Scheduler", description = "Enable Scheduler", type = AttributeType.BOOLEAN)
  boolean workflowNotificationReview10Months() default true;

  /**
   * Author domain.
   *
   * @return the string
   */
  @AttributeDefinition(name = "Author Domain", description = "Author Domain", type = AttributeType.STRING)
  String authorDomain() default "";
}
