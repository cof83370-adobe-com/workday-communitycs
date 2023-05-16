package com.workday.community.aem.core.models;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
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
public class AuthorshipRenderConditionModelTest {

    /** The context. */
    private final AemContext context = new AemContext();

    private AuthorshipRenderConditionModel authorshipRenderConditionModelTest;

    /** The current page. */
    private Page currentPage;

    /** The request. */
    private MockSlingHttpServletRequest request;

    /** The resource. */
    private Resource resource;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.addModelsForClasses(AuthorshipRenderConditionModel.class);
        Map<String, Object> pageProperties = new HashMap<>();

        currentPage = context.create().page("/content/workday-community/event",
                "/conf/workday-community/settings/wcm/templates/event-page-template", pageProperties);
 
        currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
        resource = context.create().resource(currentPage, "eventspage",
        "sling:resourceType", "workday-community/components/structure/eventspage","editGroups","[cc-admin]");
        context.registerService(Page.class, currentPage);
    }

    /**
     * Test AuthorTitleRenderConditionModel.
     *
     * @throws Exception the exception
     */
    @Test
    void testAuthorTitleRenderConditionModel() throws Exception {
        context.request().setResource(resource);
        MockRequestPathInfo requestPathInfo = (MockRequestPathInfo) context.request().getRequestPathInfo();
        requestPathInfo.setResourcePath("/authorReadOnly/granite:condition");
        authorshipRenderConditionModelTest = context.request().adaptTo(AuthorshipRenderConditionModel.class);
        assertNotNull(authorshipRenderConditionModelTest);
    }
}
