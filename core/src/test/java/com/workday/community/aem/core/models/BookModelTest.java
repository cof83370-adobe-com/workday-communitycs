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
 * The Class BookModelTest.
 *  @author uttej.vardineni
 */
@ExtendWith(AemContextExtension.class)
public class BookModelTest {

    /** The context. */
    private final AemContext context = new AemContext();

    /** The current page. */
    private Page currentPage;

    /** The request. */
    private MockSlingHttpServletRequest request;

    /** The book path model test. */
    private BookModel bookModelTest;

    /**
     * Sets the BookModelTest.
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
        request.setAttribute("pagePath", currentPage.getPath());
        context.registerService(Page.class, currentPage);
    }

    /**
     * Test page title.
     */
    @Test
    public void testPageTitle() {
        bookModelTest = request.adaptTo(BookModel.class);
        String title = bookModelTest.getPageTitle();
        assertEquals("Accordion image test", title);
    }
}