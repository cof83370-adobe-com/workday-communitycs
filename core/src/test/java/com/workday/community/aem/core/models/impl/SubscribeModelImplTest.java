package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.models.SubscribeModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.utils.PageUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class SubscribeModelImplTest {
  /**
   * AemContext.
   */
  private final AemContext context = new AemContext();

  @Mock
  DrupalService drupalService;

  @Mock
  ResourceResolverFactory resourceResolverFactory;

  @Mock
  RunModeConfigService runModeConfigService;

  @BeforeEach
  public void setup() {
    context.addModelsForClasses(SubscribeModelImpl.class);
    context.registerService(DrupalService.class, drupalService);
    context.registerService(RunModeConfigService.class, runModeConfigService);
  }

  @Test
  void testEnabled() {
    try(MockedStatic<PageUtils> mockPageUtils = mockStatic(PageUtils.class);
        MockedStatic<ResolverUtil> mockResolverUtils = mockStatic(ResolverUtil.class)) {
      MockSlingHttpServletRequest request = context.request();
      SubscribeModel model = request.adaptTo(SubscribeModel.class);

      DrupalConfig config = mock(DrupalConfig.class);
      ResourceResolver resolver = mock(ResourceResolver.class);
      lenient().when(drupalService.getConfig()).thenReturn(config);
      mockPageUtils.when(() -> PageUtils.isPageRetired(any(), anyString())).thenReturn(false);
      mockResolverUtils.when(() -> ResolverUtil.newResolver(any(), anyString())).thenReturn(resolver);

      lenient().when(config.enableSubscribe()).thenReturn(true);
      assertTrue("The subscription is enabled", model.enabled());
    }
  }

  @Test
  void testReadOnly() {
    SubscribeModel model = context.request().adaptTo(SubscribeModel.class);
    lenient().when(runModeConfigService.getInstance()).thenReturn("author");
    assertTrue("The subscription is readonly", model.readOnly());
  }
}
