package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

import com.workday.community.aem.core.models.FooterModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class FooterModelImplTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class FooterModelImplTest {

  /**
   * The AemContext.
   */
  private final AemContext context = new AemContext();

  /**
   * Run mode config service.
   */
  @Mock
  RunModeConfigService service;

  /**
   * Set up method for test run.
   */
  @BeforeEach
  public void setup() {
    context.addModelsForClasses(FooterModelImpl.class);
    context.registerService(RunModeConfigService.class, service);
  }

  /**
   * Test method getAdobeUri.
   */
  @Test
  void testGetAdobeUri() {
    String uri = "https://www.adobe.com";
    lenient().when(service.getAdobeAnalyticsUri()).thenReturn(uri);
    FooterModel footerModel = context.request().adaptTo(FooterModel.class);
    assertNotNull(footerModel);
    assertEquals(uri, footerModel.getAdobeAnalyticsUri());
  }

}
