package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.services.SnapService;
import org.mockito.MockitoAnnotations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;

import static org.mockito.Mockito.*;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class NavHeaderModelImplTest.
 */
@ExtendWith(AemContextExtension.class)
public class NavHeaderModelImplTest {

    /** AemContext */
    private final AemContext context = new AemContext();

    /** NavMenuApiService object */
    @Spy
    SnapService snapService;

    /**
     * Set up method for test run.
     * 
     * @throws Exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(HeaderModelImpl.class);
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for getUserNavigationHeaderMenu in NavHeaderModel class.
     * 
     * @throws Exception
     */
    @Test
    void testGetUserNavigationHeaderMenu() throws Exception {
        when(snapService.getUserHeaderMenu("masterdata")).thenReturn("");
//        when(navMenuApiService.getFailStateHeaderMenu()).thenReturn("");
        HeaderModel navModel = context.request().adaptTo(HeaderModel.class);
        assertEquals("", navModel.getUserHeaderMenus());
    }
}