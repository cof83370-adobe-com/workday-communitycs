package com.workday.community.aem.core.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;

import com.workday.community.aem.core.config.InactiveUserCleanUpSchedulerConfig;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class InactiveUserCleanUpSchedulerConfigServiceTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class InactiveUserCleanUpSchedulerConfigServiceTest {

    /** The service InactiveUserCleanUpSchedulerConfigService. */
    private final InactiveUserCleanUpSchedulerConfigService configService = new InactiveUserCleanUpSchedulerConfigService();

    /** The config InactiveUserCleanUpSchedulerConfig. */
    private final InactiveUserCleanUpSchedulerConfig testConfig = new InactiveUserCleanUpSchedulerConfig() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }

        @Override
        public boolean isSchedulerEnabled() {
            return false;
        }

        @Override
        public String schedulerName() {
            return "Scheduler";
        }

        @Override
        public String cronExpression() {
            return "0 0 0 * * ?";
        }
    };

    @BeforeEach
    public void setup() {
        configService.activate(testConfig);
    }

    /**
     * Test all methods.
     */
    @Test
    public void testAllMethods() {
        assertEquals(configService.getIsSchedulerEnabled(), testConfig.isSchedulerEnabled());
        assertEquals(configService.getSchedulerName(), testConfig.schedulerName());
        assertEquals(configService.getCronExpression(), testConfig.cronExpression());
    }
    
}
