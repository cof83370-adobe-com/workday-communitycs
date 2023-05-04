package com.workday.community.aem.core.models.impl;

import com.day.cq.commons.Filter;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoTabListModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertNull;
import static junitx.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoTabListModelImplTest {
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
  private CoveoTabListModel coveoTabListModel = new CoveoTabListModelImpl();

  @BeforeEach
  public void setup() {
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    JsonArray fields = new JsonArray();
    fields.add(new JsonObject());
    modelConfig.add("fields", fields);
  }

  @Test
  void testGetSearchConfig() {
    JsonObject searchConfig = coveoTabListModel.getSearchConfig();
    assertEquals(3, searchConfig.size());
  }

  @Test
  void TestGetFields() {
    try (MockedStatic<DamUtils> mocked = mockStatic(DamUtils.class)) {
      mocked.when(() -> DamUtils.readJsonFromDam(any(),anyString())).thenReturn(modelConfig);
      JsonArray res = coveoTabListModel.getFields();
      assertNull(res);
    }
  }

  @Test
  void TestGetCompConfig() {
    JsonObject searchConfig = coveoTabListModel.getCompConfig();
    assertEquals("400px", searchConfig.get("containerWidth").getAsString());
  }

  @Test
  void TestGetProductCriteria() {
    ResourceResolver resolverMock = mock(ResourceResolver.class);
    TagManager tagManager = mock(TagManager.class);
    Tag productTag = mock(Tag.class);
    lenient().when(slingHttpServletRequest.getResourceResolver()).thenReturn(resolverMock);
    lenient().when(resolverMock.adaptTo(TagManager.class)).thenReturn(tagManager);
    lenient().when(tagManager.resolve(anyString())).thenReturn(productTag);

    List<Tag> children = new ArrayList<>();
    children.add(new Tag() {
      @Override
      public <AdapterType>  AdapterType adaptTo(Class<AdapterType> aClass) {
        return null;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public String getTagID() {
        return null;
      }

      @Override
      public String getLocalTagID() {
        return null;
      }

      @Override
      public String getPath() {
        return null;
      }

      @Override
      public String getTitle() {
        return "Financial Management";
      }

      @Override
      public String getTitle(Locale locale) {
        return getTitle();
      }

      @Override
      public String getLocalizedTitle(Locale locale) {
        return null;
      }

      @Override
      public Map<Locale, String> getLocalizedTitles() {
        return null;
      }

      @Override
      public String getDescription() {
        return null;
      }

      @Override
      public String getTitlePath() {
        return null;
      }

      @Override
      public String getTitlePath(Locale locale) {
        return null;
      }

      @Override
      public Map<Locale, String> getLocalizedTitlePaths() {
        return null;
      }

      @Override
      public long getCount() {
        return 0;
      }

      @Override
      public long getLastModified() {
        return 0;
      }

      @Override
      public String getLastModifiedBy() {
        return null;
      }

      @Override
      public boolean isNamespace() {
        return false;
      }

      @Override
      public Tag getNamespace() {
        return null;
      }

      @Override
      public Tag getParent() {
        return null;
      }

      @Override
      public Iterator<Tag> listChildren() {
        return null;
      }

      @Override
      public Iterator<Tag> listChildren(Filter<Tag> filter) {
        return null;
      }

      @Override
      public Iterator<Tag> listAllSubTags() {
        return null;
      }

      @Override
      public Iterator<Resource> find() {
        return null;
      }

      @Override
      public String getXPathSearchExpression(String s) {
        return null;
      }

      @Override
      public String getGQLSearchExpression(String s) {
        return null;
      }
    });
    lenient().when(productTag.listAllSubTags()).thenReturn(children.iterator());

    String prodCriteria = coveoTabListModel.getProductCriteria();
    assertEquals("(@druproducthierarchy==(\"Financial Management\"))", prodCriteria);
  }
}
