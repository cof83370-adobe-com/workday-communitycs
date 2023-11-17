package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.RetirementManagerJobConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class RetirementManagerJobConfigServiceTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class RetirementManagerJobConfigServiceTest {

  /**
   * The RetirementManagerJobConfigService.
   */
  private final RetirementManagerJobConfigService service = new RetirementManagerJobConfigService();

  /** The retirement manager job config. */
  private final RetirementManagerJobConfig retirementManagerJobConfig = new RetirementManagerJobConfig() {

		@Override
		public String workflowNotificationCron() {
			return "0 0 18 * * ?";
		}

		@Override
		public boolean enableWorkflowNotificationReview() {
			return true;
		}

		@Override
		public String authorDomain() {
			return "http://localhost:4502";
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return Annotation.class;
		}

		@Override
		public boolean enableWorkflowNotificationRetirement() {
			return true;
		}
  };

  @BeforeEach
  public void setup() {
    service.activate(retirementManagerJobConfig);
  }

  /**
   * Test all methods.
   */
  @Test
  public void testMethods() {
    assertEquals(service.getWorkflowNotificationCron(), retirementManagerJobConfig.workflowNotificationCron());
    assertEquals(service.getEnableWorkflowNotificationReview(), retirementManagerJobConfig.enableWorkflowNotificationReview());
    assertEquals(service.getAuthorDomain(), retirementManagerJobConfig.authorDomain());
    assertEquals(service.getEnableWorkflowNotificationRetirement(), retirementManagerJobConfig.enableWorkflowNotificationRetirement());
  }

}
