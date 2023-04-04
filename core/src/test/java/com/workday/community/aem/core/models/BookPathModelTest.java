package com.workday.community.aem.core.models;

import com.day.cq.wcm.api.Page;
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
 * The Class BookPathModelTest.
 * 
 * @author uttej.vardineni
 */
@ExtendWith(AemContextExtension.class)
public class BookPathModelTest {

    /** The context. */
    private final AemContext context = new AemContext();

    /** The current page. */
    private Page currentPage;

    private MockSlingHttpServletRequest request;

    private BookPathModel bookPathModelTest;

    @BeforeEach
    public void setUp() throws Exception {
        context.addModelsForPackage("com.workday.community.aem.core.models");
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("jcr:title", "Accordion image test");
        currentPage = context.create().page("/content/workday-community/en-us/thomas-sandbox/accordion-image-test",
                "/conf/workday-community/settings/wcm/templates/faq", pageProperties);
        request = context.request();
        request.setAttribute("pagePath", currentPage.getPath());
        context.registerService(Page.class, currentPage);
    }

    @Test
    public void testPageTitle() {
        bookPathModelTest = request.adaptTo(BookPathModel.class);
        String title = bookPathModelTest.getPageTitle();
        assertEquals("Accordion image test", title);
    }
}