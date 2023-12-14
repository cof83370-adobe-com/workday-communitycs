package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.WorkflowConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class WorkflowConfigServiceTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class WorkflowConfigServiceTest {

  /**
   * The WorkflowConfigService.
   */
  private final WorkflowConfigService service = new WorkflowConfigService();

  /** The workflow config. */
  private final WorkflowConfig workflowConfig = new WorkflowConfig() {

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
		
		@Override
		public int archivalDays() {
		    return 90;
		}
  };

  @BeforeEach
  public void setup() {
    service.activate(workflowConfig);
  }

  /**
   * Test all methods.
   */
  @Test
  public void testMethods() {
    assertEquals(service.getWorkflowNotificationCron(), workflowConfig.workflowNotificationCron());
    assertEquals(service.enableWorkflowNotificationReview(), workflowConfig.enableWorkflowNotificationReview());
    assertEquals(service.getAuthorDomain(), workflowConfig.authorDomain());
    assertEquals(service.enableWorkflowNotificationRetirement(), workflowConfig.enableWorkflowNotificationRetirement());
  }

}
