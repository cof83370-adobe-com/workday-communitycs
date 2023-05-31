package com.workday.community.aem.core.models;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.constants.GlobalConstants;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The Class BookModelTest.
 *  @author uttej.vardineni
 */
@ExtendWith(AemContextExtension.class)
public class RelatedInfoModelTest {

    /** The context. */
    private final AemContext context = new AemContext();

    /** The request. */
    private MockSlingHttpServletRequest request;

    /** The relatedinfo path model test. */
    private RelatedInfoModel relatedInfoModelTest;

        /** The current page. */
        private Page currentPage;

    /**
     * Sets the RelatedInfoModelTest.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        context.addModelsForPackage("com.workday.community.aem.core.models");
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("jcr:title", "Accordion image test");
        currentPage = context.create().page("/content/workday-community/en-us/thomas-sandbox/accordion-image-test",
                "/conf/workday-community/settings/wcm/templates/faq", pageProperties);
        request = context.request();
        context.registerService(Page.class, currentPage);
    }

    /**
     * Test Root Path.
     */
    @Test
    public void testRootPath() {
        relatedInfoModelTest = request.adaptTo(RelatedInfoModel.class);
        String actualRootPath = relatedInfoModelTest.getRootPath();
        String expectedRootPath = "/content/workday-community/";
        assertEquals(expectedRootPath, actualRootPath);
    }
}