package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.MockitoAnnotations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;

import com.workday.community.aem.core.models.NavHeaderModel;
import com.workday.community.aem.core.services.NavMenuApiService;
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
    NavMenuApiService navMenuApiService;

    /**
     * Set up method for test run.
     * 
     * @throws Exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(NavHeaderModelImpl.class);
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for getUserNavigationHeaderMenu in NavHeaderModel class.
     * 
     * @throws Exception
     */
    @Test
    void testGetUserNavigationHeaderMenu() throws Exception {
        when(navMenuApiService.getUserNavigationHeaderData("masterdata")).thenReturn("");
        NavHeaderModel navModel = context.request().adaptTo(NavHeaderModel.class);
        assertEquals("", navModel.getUserNavigationHeaderMenu());
    }
}