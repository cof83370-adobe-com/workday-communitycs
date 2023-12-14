package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.models.SubscribeModel;
import com.workday.community.aem.core.services.DrupalService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class SubscribeModelImplTest {
  /**
   * AemContext.
   */
  private final AemContext context = new AemContext();

  @Mock
  DrupalService drupalService;

  @BeforeEach
  public void setup() {
    context.addModelsForClasses(SubscribeModelImpl.class);
    context.registerService(DrupalService.class, drupalService);

  }

  @Test
  void testSubscribeEnabled() {
    SubscribeModel model = context.request().adaptTo(SubscribeModel.class);
    DrupalConfig config = mock(DrupalConfig.class);
    lenient().when(drupalService.getConfig()).thenReturn(config);
    lenient().when(config.enableSubscribe()).thenReturn(true);
    assertTrue("The subscription is enabled", model.enabled());
  }
}
