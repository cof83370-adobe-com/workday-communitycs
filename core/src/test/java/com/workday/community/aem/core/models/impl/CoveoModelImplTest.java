package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.CoveoModel;
import com.workday.community.aem.core.services.QueryService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class CoveoModelImplTest {

    private final AemContext context = new AemContext();  

    private Page currentPage;
    
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(CoveoModelImpl.class);
        currentPage = context.create().page("/content/workday-community/coveo",
                "/conf/workday-community/settings/wcm/templates/page-content");
        currentPage = context.currentResource("/content/workday-community/coveo").adaptTo(Page.class);
        QueryService queryService = mock(QueryService.class);
        context.registerService(QueryService.class, queryService);
        context.registerService(Page.class, currentPage);
    }

    @Test
    void testGetTotalPages() throws Exception {
        CoveoModel coveoModel = context.request().adaptTo(CoveoModel.class);
        assertEquals(0, coveoModel.getTotalPages());
    }

    @Test
    void testGetIndexedPages() throws Exception {
        CoveoModel coveoModel = context.request().adaptTo(CoveoModel.class);
        assertEquals(2, coveoModel.getIndexedPages());
    }

    @Test
    void testGetPercentage() throws Exception {
        CoveoModel coveoModel = context.request().adaptTo(CoveoModel.class);
        assertEquals(0.0, coveoModel.getPercentage());
    }

    @Test
    void testGetServerStatus() throws Exception {
        CoveoModel coveoModel = context.request().adaptTo(CoveoModel.class);
        assertEquals(true, coveoModel.getServerStatus());
    }
}
