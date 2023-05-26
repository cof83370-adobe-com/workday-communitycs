package com.workday.community.aem.core.schedulers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.commons.scheduler.JobContext;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.services.InactiveUserCleanUpSchedulerConfigService;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.UserService;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class InactiveUserCleanUpSchedulerTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class InactiveUserCleanUpSchedulerTest {

    /** The InactiveUserCleanUpScheduler. */
    @InjectMocks
    private InactiveUserCleanUpScheduler inactiveUserCleanUpScheduler;

    /** The query service. */
    @Mock
    QueryService queryService;

    /** The user service. */
    @Mock
    UserService userService;

    /** The scheduler service. */
    @Mock
    Scheduler scheduler;

    /** The InactiveUserCleanUpSchedulerConfigService service. */
    @Mock
    InactiveUserCleanUpSchedulerConfigService configService;

    /**
     * Test activate method.
     */
    @Test
    public void testActivate() {
        lenient().when(configService.getIsSchedulerEnabled()).thenReturn(false);
        lenient().when(configService.getSchedulerName()).thenReturn("scheduler name");
        lenient().when(configService.getCronExpression()).thenReturn("0 0 0 * * ?");
        inactiveUserCleanUpScheduler.activate();
        verify(scheduler, times(0)).schedule(any(), any());

        lenient().when(configService.getIsSchedulerEnabled()).thenReturn(true);
        lenient().when(configService.getSchedulerName()).thenReturn("Scheduler name");
        lenient().when(configService.getCronExpression()).thenReturn("0 0 0 * * ?");
        ScheduleOptions options = mock (ScheduleOptions.class);
        lenient().when(scheduler.EXPR("0 0 0 * * ?")).thenReturn(options);
        List<String> userList = new ArrayList<>();
        userList.add("/home/user/A");
        lenient().when(queryService.getInactiveUsers()).thenReturn(userList);
        lenient().when(options.config(anyMap())).thenReturn(options);
        inactiveUserCleanUpScheduler.activate();
        verify(scheduler).schedule(any(), any());
    }

    /**
     * Test execute method.
     */
    @Test
    public void testExecute() {
        Map<String, Serializable> map = new HashMap<>();
        String userPath = "/home/user/A";
        map.put("userPath", userPath);
        JobContext jobContext = mock(JobContext.class);
        lenient().when(jobContext.getConfiguration()).thenReturn(map);
        inactiveUserCleanUpScheduler.execute(jobContext);;
        verify(userService).deleteUser(userPath, true);
    }

    /**
     * Test deactivate method.
     */
    @Test 
    public void testDeactivate() {
        inactiveUserCleanUpScheduler.deactivate();
        verify(scheduler).unschedule(anyString());
    }
    
}
