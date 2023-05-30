package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoListViewModelImplTest {

    /** The AemContext object. */
    private final AemContext context = new AemContext();

    @Mock
    SearchApiConfigService searchApiConfigService;

    @Mock
    ResourceResolver resourceResolver;

    @Mock
    TagManager tagManager;

    MockedStatic<DamUtils> mockDamUtils;


    @BeforeEach
    public void setUp() {
        context.addModelsForClasses(CoveoListViewModelImpl.class);
        context.addModelsForClasses(CategoryFacetModel.class);
        context.load().json("/com/workday/community/aem/core/models/impl/CoveoListViewModel.json", "/component");
        context.registerService(SearchApiConfigService.class, searchApiConfigService);
        when(searchApiConfigService.getSearchHub()).thenReturn("TestSearchHub");
        when(searchApiConfigService.getOrgId()).thenReturn("TestOrgId");
        when(resourceResolver.adaptTo(TagManager.class)).thenReturn(tagManager);
        context.registerService(ResourceResolver.class, resourceResolver);

        Tag tag1Namespace = mock(Tag.class);
        when(tag1Namespace.getName()).thenReturn("product");

        Tag tag2Namespace = mock(Tag.class);
        when(tag2Namespace.getName()).thenReturn("using-workday");
        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);

        when(tag1.getNamespace()).thenReturn(tag1Namespace);
        when(tag1.isNamespace()).thenReturn(true);
        when(tag2.getNamespace()).thenReturn(tag2Namespace);
        when(tag2.isNamespace()).thenReturn(true);
        when(tagManager.resolve("product:")).thenReturn(tag1);
        when(tagManager.resolve("using-workday:")).thenReturn(tag2);

        String fieldMapConfig = "{\"tagIdToField\": {\"product\" : \"coveo_product\", \"using-workday\": \"coveo_using-workday\"}}";
        Gson gson = new Gson();
        JsonObject fieldMapConfigObj = gson.fromJson(fieldMapConfig, JsonObject.class);

        mockDamUtils = mockStatic(DamUtils.class);

        mockDamUtils.when(() -> DamUtils.readJsonFromDam(eq(resourceResolver), eq("/content/dam/workday-community/resources/coveo-field-map.json")))
                .thenReturn(fieldMapConfigObj);


    }

    @Test
    void testComponent() {
        CoveoListViewModel listViewModel = context.currentResource("/component/listView").adaptTo(CoveoListViewModel.class);

        assertTrue(listViewModel.getDisplayMetadata());
        assertTrue(listViewModel.getDisplayTags());
        assertEquals("TestOrgId", listViewModel.getOrgId());
        assertEquals("TestSearchHub", listViewModel.getSearchHub());
        List<CategoryFacetModel> categoryFacetModels = listViewModel.getCategories();
        assertEquals(2, categoryFacetModels.size());
        CategoryFacetModel prod = categoryFacetModels.get(0);
        assertEquals("coveo_product", prod.getField());
        CategoryFacetModel usingWorkday = categoryFacetModels.get(1);
        assertEquals("coveo_using-workday", usingWorkday.getField());
    }

    @AfterEach
    public void after() {
        resourceResolver.close();
        mockDamUtils.close();
    }

}
