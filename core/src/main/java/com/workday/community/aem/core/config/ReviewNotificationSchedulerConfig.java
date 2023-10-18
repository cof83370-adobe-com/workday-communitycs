package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "ReviewNotificationSchedulerConfiguration OCD", description = "ReviewNotificationSchedulerConfiguration OCD description")
public @interface ReviewNotificationSchedulerConfig {
	@AttributeDefinition(name = "Scheduler name", description = "Scheduler name", type = AttributeType.STRING)
	String schedulerName() default "ReviewNotificationScheduler";

	@AttributeDefinition(name = "Cron job expression", description = "Cron job expression", type = AttributeType.STRING)
	String workflowNotificationCron() default "0 0 18 * * ?";

	@AttributeDefinition(name = "Enable Scheduler", description = "Enable Scheduler", type = AttributeType.BOOLEAN)
	boolean workflowNotificationReview10Months() default true;

	@AttributeDefinition(name = "Author Domain", description = "Author Domain", type = AttributeType.STRING)
	String authorDomain() default "";
}
