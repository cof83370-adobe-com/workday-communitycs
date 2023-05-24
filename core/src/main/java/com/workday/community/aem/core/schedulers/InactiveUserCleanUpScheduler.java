package com.workday.community.aem.core.schedulers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.commons.scheduler.Job;
import org.apache.sling.commons.scheduler.JobContext;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.services.InactiveUserCleanUpSchedulerConfigService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.UserService;

@Component(immediate = true, service = Job.class)
public class InactiveUserCleanUpScheduler implements Job {

    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The scheduler job id. */
    private int schedulerJobId;

    /** The scheduler cron expression. */
    private String cronExpression;

    /** The scheduler service. */
    @Reference
    private Scheduler scheduler;

    /** The InactiveUserCleanUpSchedulerConfigService. */
    @Reference
    private InactiveUserCleanUpSchedulerConfigService configService;

    /** The query service. */
    @Reference
    private QueryService queryService;

    /** The user service. */
    @Reference
    private UserService userService;

    @Activate
    protected void activate() {
        schedulerJobId = configService.getSchedulerName().hashCode();
        cronExpression = configService.getCronExpression();
        if (configService.getIsSchedulerEnabled()) {
            addSchedulerJob();
        }
    }

    @Deactivate
    protected void deactivate() {
        removeSchedulerJob();
    }

    private void removeSchedulerJob() {
        logger.info("Deactivate inactive user clean up scheduler.");
        scheduler.unschedule(String.valueOf(schedulerJobId));
    }

    private void addSchedulerJob() {
        List<String> userList = queryService.getInactiveUsers();
        for (String path: userList) {
            ScheduleOptions options = scheduler.EXPR(cronExpression);
            Map<String, Serializable> map = new HashMap<>();
            map.put("userPath", path);
            options.config(map);
            scheduler.schedule(this, options);
        }
    }

    @Override
    public void execute(JobContext jobContext) {
        logger.info("Scheduler job to clean up inactive users is running.");
        String userPath = jobContext.getConfiguration().get("userPath").toString();
        userService.deleteUser(userPath, true);
    } 
}
