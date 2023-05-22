package com.workday.community.aem.core.models.impl;

import com.day.cq.commons.Filter;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
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
import org.apache.sling.models.factory.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertNull;
import static junitx.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoTabListModelImplTest {
  /**
   * AemContext
   */
  private final AemContext context = new AemContext();
  JsonObject modelConfig = new JsonObject();

  @Mock
  SlingHttpServletRequest slingHttpServletRequest;

  @Mock
  SearchApiConfigService searchApiConfigService;

  private CoveoTabListModel coveoTabListModel;

  @BeforeEach
  public void setup() {
    context.load().json("/com/workday/community/aem/core/models/impl/event-feed-test.json", "/content");
    Resource res = context.request().getResourceResolver().getResource("/content/event-feed-page");
    Page currentPage = res.adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    context.registerService(JsonObject.class, modelConfig);
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    context.registerService(SlingHttpServletRequest.class, slingHttpServletRequest);
    context.addModelsForClasses(CoveoTabListModelImpl.class);
    coveoTabListModel = context.getService(ModelFactory.class).createModel(res, CoveoTabListModel.class);

    JsonArray fields = new JsonArray();
    JsonObject field = new JsonObject();
    field.addProperty("name", "Question");
    fields.add(field);
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
      ((CoveoTabListModelImpl) coveoTabListModel).init(this.slingHttpServletRequest);
      mocked.when(() -> DamUtils.readJsonFromDam(eq(this.slingHttpServletRequest.getResourceResolver()), anyString())).thenReturn(modelConfig);
      JsonArray res = coveoTabListModel.getFields();
      assertEquals(1, res.size());
    }
  }

  @Test
  void TestGetSelectedFields() {
    try (MockedStatic<DamUtils> mocked = mockStatic(DamUtils.class)) {
      ((CoveoTabListModelImpl) coveoTabListModel).init(this.slingHttpServletRequest);
      mocked.when(() -> DamUtils.readJsonFromDam(eq(this.slingHttpServletRequest.getResourceResolver()), anyString())).thenReturn(modelConfig);
      JsonArray res = coveoTabListModel.getSelectedFields();
      assertEquals(1, res.size());
    }
  }

  @Test
  void TestGetProductCriteria() {
    try (MockedStatic<DamUtils> mockedStatic = mockStatic(DamUtils.class)) {
      mockedStatic.when(() -> DamUtils.readJsonFromDam(eq(this.slingHttpServletRequest.getResourceResolver()), anyString())).thenReturn(modelConfig);
      ResourceResolver resolverMock = mock(ResourceResolver.class);
      TagManager tagManager = mock(TagManager.class);
      Tag parentTag = new Tag() {

        @Override
        public <AdapterType> AdapterType adaptTo(Class<AdapterType> aClass) {
          return null;
        }

        @Override
        public String getName() {
          return null;
        }

        @Override
        public String getTagID() {
          return "product:";
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
          return null;
        }

        @Override
        public String getTitle(Locale locale) {
          return null;
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
      };

      Tag productTag = new Tag() {
        @Override
        public <AdapterType> AdapterType adaptTo(Class<AdapterType> aClass) {
          return null;
        }

        @Override
        public String getName() {
          return null;
        }

        @Override
        public String getTagID() {
          return "product:1572";
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
          return null;
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
          return parentTag;
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
      };

      lenient().when(slingHttpServletRequest.getResourceResolver()).thenReturn(resolverMock);
      lenient().when(resolverMock.adaptTo(TagManager.class)).thenReturn(tagManager);
      lenient().when(tagManager.resolve(anyString())).thenReturn(productTag);
      ((CoveoTabListModelImpl) coveoTabListModel).init(this.slingHttpServletRequest);

      String prodCriteria = coveoTabListModel.getProductCriteria();

      assertEquals("(@druproducthierarchy==(\"Financial Management\"))", prodCriteria);
    }
  }
}
