package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoEventFeedModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static junitx.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoEventFeedModelImplTest {
  /**
   * AemContext
   */
  private final AemContext context = new AemContext();

  @Mock
  JsonObject modelConfig = new JsonObject();

  @Mock
  SlingHttpServletRequest slingHttpServletRequest;

  @Mock
  SearchApiConfigService searchApiConfigService;

  @InjectMocks
  CoveoEventFeedModel model = new CoveoEventFeedModelImpl();

  @BeforeEach
  public void setup() {
    Map<String, Object> value = new HashMap<>();
    value.put("featureEvent", "featureEventPath");
    value.put("eventTypes", new String[] {"test1", "test2"});
    context.registerService(ValueMap.class, new ValueMapDecorator(value));
  }

  @Test
  void testGetSearchConfig() {
    JsonObject searchConfig = model.getSearchConfig();
    assertEquals(3, searchConfig.size());
  }

  @Test
  void testGetFeatureEvent() throws RepositoryException {
    ResourceResolver resolverMock = mock(ResourceResolver.class);
    PageManager pageManager = mock(PageManager.class);
    Page pageObject = mock(Page.class);
    ValueMap valueMap = mock(ValueMap.class);
    GregorianCalendar startTime =  new GregorianCalendar(2018, 6, 27, 16, 16, 47);
    GregorianCalendar endTime =  new GregorianCalendar(2018, 7, 27, 16, 16, 47);

    lenient().when(slingHttpServletRequest.getResourceResolver()).thenReturn(resolverMock);
    lenient().when(resolverMock.adaptTo(PageManager.class)).thenReturn(pageManager);
    lenient().when(pageManager.getPage(anyString())).thenReturn(pageObject);
    lenient().when(pageObject.getProperties(anyString())).thenReturn(valueMap);
    lenient().when(valueMap.get(eq("startDate"))).thenReturn(startTime);
    lenient().when(valueMap.get(eq("endDate"))).thenReturn(endTime);
    lenient().when(valueMap.get(eq("eventLocation"))).thenReturn("eventLocation");
    lenient().when(valueMap.get(eq("eventHost"))).thenReturn("eventHost");

    Map<String, String> test = model.getFeatureEvent();
  }
}
