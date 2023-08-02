package com.workday.community.aem.core.models.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import com.workday.community.aem.core.models.CourseDetailModel;
import com.workday.community.aem.core.services.LMSService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class CourseDetailModelImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class CourseDetailModelImplTest {
    /**
     * AemContext.
     */
    private final AemContext context = new AemContext();

    /**
     * LMSService object.
     */
    @Mock
    LMSService lmsService;

    /**
     * Set up method for test run.
     */
    @BeforeEach
    public void setup() {
        context.addModelsForClasses(CourseDetailModelImpl.class);
        context.registerService(LMSService.class, lmsService, SERVICE_RANKING, Integer.MAX_VALUE);
    }

    /**
     * Test method for getCourseDetailData in CourseDetailModel class.
     */
    @Test
    void testGetCourseDetailData() {
        lenient().when(lmsService.getCourseDetail("groupedTitle")).thenReturn("");
        CourseDetailModel courseDetailModel = context.request().adaptTo(CourseDetailModel.class);
        assertNotNull(courseDetailModel);
        assertEquals("", courseDetailModel.getCourseDetailData());
    }

}
