package com.workday.community.aem.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.config.AemRunModeConfig;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class ExtractPagePropertiesServiceImplTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class AemRunModeConfigServiceTest {

    /** The service aemRunModeConfigService. */
    private final AemRunModeConfigService aemRunModeConfigService = new AemRunModeConfigService();

    /** The config aemRunModeConfig. */
    private final AemRunModeConfig testConfig = new AemRunModeConfig() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return null;
        }

        @Override
        public String aemEnv() {
            return "dev";
        }

        @Override
        public String aemInstance() {
            return "publish";
        }
    };

    @BeforeEach
    public void setup() {
        ((AemRunModeConfigService) aemRunModeConfigService).activate(testConfig);
    }

    /**
     * Test all methods.
     */
    @Test
    public void testAllMethods() {
        assertEquals(aemRunModeConfigService.getAemEnv(), testConfig.aemEnv());
        assertEquals(aemRunModeConfigService.getAemInstance(), testConfig.aemInstance());
    }

}