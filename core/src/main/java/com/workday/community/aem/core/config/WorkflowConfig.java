package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Interface WorkflowConfig.
 */
@ObjectClassDefinition(name = "Workflow Configuration", 
description = "Paramerters for Retirement Manager Scheduler")
public @interface WorkflowConfig {

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
  @AttributeDefinition(name = "Enable Scheduler for Review 10 Months", 
      description = "Enable Scheduler for Review 10 Months", type = AttributeType.BOOLEAN)
  boolean enableWorkflowNotificationReview() default true;

  /**
   * Workflow notification retirement 11 months.
   *
   * @return true, if successful
   */
  @AttributeDefinition(name = "Enable Scheduler for Retirement 11 Months", 
      description = "Enable Scheduler for Retirement 11 Months", type = AttributeType.BOOLEAN)
  boolean enableWorkflowNotificationRetirement() default true;

  /**
   * Author domain.
   *
   * @return the string
   */
  @AttributeDefinition(name = "Author Domain", description = "Author Domain", type = AttributeType.STRING)
  String authorDomain() default "";
  
  /**
   * Archival days.
   *
   * @return the days
   */
  @AttributeDefinition(name = "Archival Days", description = "Archival Days", type = AttributeType.INTEGER)
  int archivalDays() default 90;
}
