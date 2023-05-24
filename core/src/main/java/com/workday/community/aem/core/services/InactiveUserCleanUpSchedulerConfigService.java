package com.workday.community.aem.core.services;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import com.workday.community.aem.core.config.InactiveUserCleanUpSchedulerConfig;

/**
 * The InactiveUserCleanUpSchedulerConfigService class.
 */
@Component(
    service = InactiveUserCleanUpSchedulerConfigService.class,
    immediate = true,
    configurationPid = "com.workday.community.aem.core.config.InactiveUserCleanUpSchedulerConfig"
)
@Designate(ocd = InactiveUserCleanUpSchedulerConfig.class)
public class InactiveUserCleanUpSchedulerConfigService {

    /** The InactiveUserCleanUpSchedulerConfig. */
    private InactiveUserCleanUpSchedulerConfig config;
    
    @Activate
    @Modified
    public void activate(InactiveUserCleanUpSchedulerConfig config) {
        this.config = config;
    }

    /**
     * Get scheduler is enabled or not.
     *
     * @return Scheduler is enabled or not.
     */
    public Boolean getIsSchedulerEnabled() {
        return config.isSchedulerEnabled();
    }

    /**
     * Get scheduler name.
     *
     * @return Scheduler name.
     */
    public String getSchedulerName() {
        return config.schedulerName();
    }

    /**
     * Get scheduler cron expression.
     *
     * @return Scheduler cron expression.
     */
    public String getCronExpression() {
        return config.cronExpression();
    }
    
}
