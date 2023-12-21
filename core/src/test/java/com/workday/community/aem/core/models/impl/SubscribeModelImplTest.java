package com.workday.community.aem.core.models.impl;

import static com.workday.community.aem.core.constants.WorkflowConstants.RETIREMENT_STATUS_VAL;
import static junitx.framework.Assert.assertFalse;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.models.SubscribeModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.RunModeConfigService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class SubscribeModelImplTest {
  /**
   * AemContext.
   */
  private final AemContext context = new AemContext();

  @Spy
  @InjectMocks
  MockSlingHttpServletRequest request = context.request();

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

    SubscribeModel model = request.adaptTo(SubscribeModel.class);
    DrupalConfig config = mock(DrupalConfig.class);
    lenient().when(drupalService.getConfig()).thenReturn(config);
    ResourceResolver resolver = mock(ResourceResolver.class);
    lenient().when(request.getResourceResolver()).thenReturn(resolver);
    Resource mockResource = mock(Resource.class);
    lenient().when(request.getResource()).thenReturn(mockResource);
    PageManager pageManager = mock(PageManager.class);
    lenient().when(resolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    Page mockPage = mock(Page.class);
    when(pageManager.getContainingPage(mockResource)).thenReturn(mockPage);
    ValueMap properties = mock(ValueMap.class);
    when(mockPage.getProperties()).thenReturn(properties);
    when(properties.get(anyString())).thenReturn(RETIREMENT_STATUS_VAL);
    lenient().when(config.enableSubscribe()).thenReturn(true);
    assertFalse("The subscription is enabled", model.enabled());
  }

  @Test
  void testReadOnly() {
    SubscribeModel model = context.request().adaptTo(SubscribeModel.class);
    lenient().when(runModeConfigService.getInstance()).thenReturn("author");
    assertTrue("The subscription is readonly", model.readOnly());
  }
}
