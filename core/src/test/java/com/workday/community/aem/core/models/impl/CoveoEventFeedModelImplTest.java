package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;

import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoEventFeedModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import java.util.GregorianCalendar;
import java.util.Map;

import static java.util.Calendar.*;
import static junitx.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoEventFeedModelImplTest {
  /**
   * AemContext
   */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  @Mock
  SlingHttpServletRequest request;
  @Mock
  SearchApiConfigService searchApiConfigService;

  private CoveoEventFeedModel coveoEventFeedModel;

  @BeforeEach
  public void setup() {
    context.load().json("/com/workday/community/aem/core/models/impl/CoveoEventFeedTestData.json", "/content");
    Resource res = context.request().getResourceResolver().getResource("/content/event-feed-page");
    Page currentPage = res.adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    context.registerService(SlingHttpServletRequest.class, request);
    context.addModelsForClasses(CoveoEventFeedModelImpl.class);

    coveoEventFeedModel = context.getService(ModelFactory.class).createModel(res, CoveoEventFeedModel.class);
  }

  @Test
  void testGetSearchConfig() {
    ((CoveoEventFeedModelImpl)coveoEventFeedModel).init(request);

    JsonObject searchConfig = coveoEventFeedModel.getSearchConfig();
    assertEquals(3, searchConfig.size());
  }

  @Test
  void testGetFeatureEventNotResolved() throws RepositoryException {
    ((CoveoEventFeedModelImpl)coveoEventFeedModel).init(request);

    ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
    PageManager pageManager = mock(PageManager.class);
    lenient().when(request.getResourceResolver()).thenReturn(mockResourceResolver);
    lenient().when(mockResourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);

    Map<String, String> test = coveoEventFeedModel.getFeatureEvent();
    assertEquals(0, test.size());
  }

  @Test
  void testGetEventCriteria() {
    try (MockedStatic<DamUtils> mocked = mockStatic(DamUtils.class)) {
      ((CoveoEventFeedModelImpl) coveoEventFeedModel).init(this.request);
      JsonObject modelConfig = new JsonObject();
      modelConfig.addProperty("eventCriteria", "foo");
      mocked.when(() -> DamUtils.readJsonFromDam(eq(this.request.getResourceResolver()), anyString())).thenReturn(modelConfig);

      String res = coveoEventFeedModel.getEventCriteria();
      assertEquals("(foo)", res);
    }
  }

  @Test
  void testOthers() {
    try (MockedStatic<DamUtils> mocked = mockStatic(DamUtils.class)) {
      ((CoveoEventFeedModelImpl) coveoEventFeedModel).init(this.request);
      JsonObject modelConfig = new JsonObject();
      modelConfig.addProperty("sortCriteria", "foo");
      modelConfig.addProperty("allEventsUrl", "foo1");
      modelConfig.addProperty("extraCriteria", "foo2");
      mocked.when(() -> DamUtils.readJsonFromDam(eq(this.request.getResourceResolver()), anyString())).thenReturn(modelConfig);

      assertEquals("foo", coveoEventFeedModel.getSortCriteria());
      assertEquals("foo1", coveoEventFeedModel.getAllEventsUrl());
      assertEquals("foo2", coveoEventFeedModel.getExtraCriteria());
    }
  }

  @Test
  void testGetFeatureEventResolved() throws RepositoryException {
    ((CoveoEventFeedModelImpl)coveoEventFeedModel).init(request);

    ResourceResolver mockResourceResolver = mock(ResourceResolver.class);
    PageManager pageManager = mock(PageManager.class);
    Page page = mock(Page.class);

    ValueMap testValues = new ValueMapDecorator(ImmutableMap.of(
        "startDate", new GregorianCalendar(2023, JUNE,3),
        "endDate", new GregorianCalendar(2023, OCTOBER,3),
        "eventLocation", "Bay area"
    ));

    lenient().when(request.getResourceResolver()).thenReturn(mockResourceResolver);
    lenient().when(mockResourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    lenient().when(pageManager.getPage(anyString())).thenReturn(page);
    lenient().when(page.getProperties()).thenReturn(testValues);

    Map<String, String> test = coveoEventFeedModel.getFeatureEvent();
    assertEquals(9, test.size());
    assertEquals("featureEventPath.html", test.get("link"));
  }
}
