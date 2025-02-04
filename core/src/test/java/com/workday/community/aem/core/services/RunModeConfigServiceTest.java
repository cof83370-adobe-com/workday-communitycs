package com.workday.community.aem.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workday.community.aem.core.config.RunModeConfig;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class RunModeConfigServiceTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class RunModeConfigServiceTest {

  /**
   * The service runModeConfigService.
   */
  private final RunModeConfigService runModeConfigService = new RunModeConfigService();

  /**
   * The config runModeConfig.
   */
  private final RunModeConfig testConfig = new RunModeConfig() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public String env() {
      return "dev";
    }

    @Override
    public String instance() {
      return "publish";
    }

    @Override
    public String adobeAnalyticsUri() {
      return "https://www.adobe.com";
    }

    @Override
    public String publishInstanceDomain() {
      return "https://dev-content.workday.com ";
    }
  };

  @BeforeEach
  public void setup() {
    runModeConfigService.activate(testConfig);
  }

  /**
   * Test all methods.
   */
  @Test
  public void testAllMethods() {
    assertEquals(runModeConfigService.getEnv(), testConfig.env());
    assertEquals(runModeConfigService.getInstance(), testConfig.instance());
    assertEquals(runModeConfigService.getAdobeAnalyticsUri(), testConfig.adobeAnalyticsUri());
    assertEquals(runModeConfigService.getPublishInstanceDomain(),
        testConfig.publishInstanceDomain());
  }

}
