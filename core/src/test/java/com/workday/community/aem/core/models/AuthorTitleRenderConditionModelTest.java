package com.workday.community.aem.core.models;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.HashMap;
import java.util.Map;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class AuthorTitleRenderConditionModelTest.
 * 
 * @author uttej.vardineni
 */
@ExtendWith(AemContextExtension.class)
public class AuthorTitleRenderConditionModelTest {

    /** The context. */
    private final AemContext context = new AemContext();

    private AuthorTitleRenderConditionModel authorTitleRenderConditionModelTest;

    /** The current page. */
    private Page currentPage;

    /** The request. */
    private MockSlingHttpServletRequest request;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(EventDetailsModel.class);
        Map<String, Object> pageProperties = new HashMap<>();
        pageProperties.put("startDate", "2022-11-22T00:44:02.000+05:30");
        pageProperties.put("endDate", "2022-11-24T00:54:02.000+05:30");
        pageProperties.put("eventLocation", "California");
        pageProperties.put("eventHost", "workday");
        pageProperties.put("eventFormat", new String[] { "event:event-format/webinar" });
        currentPage = context.create().page("/content/workday-community/event",
                "/conf/workday-community/settings/wcm/templates/event-page-template", pageProperties);

        currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
    }

    /**
     * Test AuthorTitleRenderConditionModel.
     *
     * @throws Exception the exception
     */
    @Test
    void testAuthorTitleRenderConditionModel() throws Exception {
        request = context.request();
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo) request.getRequestPathInfo();
        requestPathInfo.setSuffix("/conf/workday-community/settings/wcm/templates/kits-and-tools");

        authorTitleRenderConditionModelTest = context.request().adaptTo(AuthorTitleRenderConditionModel.class);
        assertNotNull(authorTitleRenderConditionModelTest);
    }
}
