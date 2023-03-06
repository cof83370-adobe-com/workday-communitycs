package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.CoveoStatusModel;
import com.workday.community.aem.core.services.QueryService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
public class CoveoStatusModelImplTest {

    private final AemContext context = new AemContext();  

    private Page currentPage;
    
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(CoveoStatusModelImpl.class);
        currentPage = context.create().page("/content/workday-community/coveostatus",
                "/conf/workday-community/settings/wcm/templates/page-content");
        currentPage = context.currentResource("/content/workday-community/coveostatus").adaptTo(Page.class);
        QueryService queryService = mock(QueryService.class);
        context.registerService(QueryService.class, queryService);
        context.registerService(Page.class, currentPage);
    }

    @Test
    void testGetTotalPages() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        assertEquals(0, coveoModel.getTotalPages());
    }

    @Test
    void testGetIndexedPages() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        assertEquals(2, coveoModel.getIndexedPages());
    }

    @Test
    void testGetPercentage() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        assertEquals(0.0, coveoModel.getPercentage());
    }

    @Test
    void testGetServerStatus() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        assertEquals(true, coveoModel.getServerStatus());
    }
}
