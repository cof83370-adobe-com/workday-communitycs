package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Inactive user clean up scheduler configuration",
    description = "Parameters for scheduler."
)
public @interface InactiveUserCleanUpSchedulerConfig {

    @AttributeDefinition (
        name = "Enabled",
        description = "Is shceduler enabled",
        type = AttributeType.BOOLEAN
    )
    boolean isSchedulerEnabled() default false;

    @AttributeDefinition (
        name = "Scheduler name",
        description = "Inactive user clean up scheduler name.",
        type = AttributeType.STRING
    )
    String schedulerName() default "Inactive user clean up scheduler";

    // Runs every day at midnight.
    @AttributeDefinition(
        name = "Cron Expression",
        description = "Cron expression used by the scheduler",
        type = AttributeType.STRING)
    public String cronExpression() default "0 0 0 * * ?";
    
}
