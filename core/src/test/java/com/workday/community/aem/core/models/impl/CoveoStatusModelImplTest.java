package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.CoveoStatusModel;
import com.workday.community.aem.core.services.CoveoSourceApiService;
import com.workday.community.aem.core.services.QueryService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class CoveoStatusModelImplTest.
 */
@ExtendWith(AemContextExtension.class)
public class CoveoStatusModelImplTest {

    /** The AemContext. */
    private final AemContext context = new AemContext();  

    /** The currentPage. */
    private Page currentPage;
    
    /**
     * Set up before each test run.
     */
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(CoveoStatusModelImpl.class);
        currentPage = context.create().page("/content/workday-community/coveostatus",
                "/conf/workday-community/settings/wcm/templates/page-content");
        currentPage = context.currentResource("/content/workday-community/coveostatus").adaptTo(Page.class);
        QueryService queryService = mock(QueryService.class);
        CoveoSourceApiService coveoSourceApiService = mock(CoveoSourceApiService.class);
        context.registerService(QueryService.class, queryService);
        context.registerService(CoveoSourceApiService.class, coveoSourceApiService);
        lenient().when(coveoSourceApiService.getTotalIndexedNumber()).thenReturn((long) 2);
        lenient().when(queryService.getNumOfTotalPublishedPages()).thenReturn((long) 20);
        context.registerService(Page.class, currentPage);
    }

    /**
     * Test getTotalPages.
     */
    @Test
    void testGetTotalPages() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        assertEquals(20, coveoModel.getTotalPages());
    }

    /**
     * Test getIndexedPages.
     */
    @Test
    void testGetIndexedPages() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        assertEquals(2, coveoModel.getIndexedPages());
    }

    /**
     * Test getPercentage.
     */
    @Test
    void testGetPercentage() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        String percentage = String.format("%.02f",coveoModel.getPercentage());
        assertEquals("0.10", percentage);
    }

    /**
     * Test getServerHasError.
     */
    @Test
    void testGetServerHasError() throws Exception {
        CoveoStatusModel coveoModel = context.request().adaptTo(CoveoStatusModel.class);
        assertEquals(false, coveoModel.getServerHasError());
    }
}
