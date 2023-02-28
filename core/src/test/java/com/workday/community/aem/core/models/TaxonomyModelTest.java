package com.workday.community.aem.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.AccessControlException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class TaxonomyModelTest.
 * 
 * @author palla.pentayya
 */
@ExtendWith(AemContextExtension.class)
public class TaxonomyModelTest {

    /** The context. */
    private final AemContext context = new AemContext();

    /** The tm. */
    TagManager tm;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.load().json("/com/workday/community/aem/core/models/impl/TaxonomyModelTest.json", "/content");

    }

    /**
     * Test init.
     * 
     * @throws InvalidTagFormatException
     * @throws AccessControlException
     */
    @Test
    public void testInit() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        TagManager tm = context.resourceResolver().adaptTo(TagManager.class);
        tm.createTag("programs-and-tools:program-type", "Program Type", "Program Type");
        tm.createTag("programs-and-tools:program-type/the-next-level", "The Next Level", "the next level");

        tm.createTag("using-workday:workday-acquisition-integrations", "Workday Acquisition Integrations",
                "Workday Acquisition Integrations");
        tm.createTag("using-workday:7028/7046", "Content Management", "Content Management");

        tm.createTag("product:4903/7728", "Connection to Workday Financial Management",
                "workday 7 - retired");
        tm.createTag("product:92", "Analytics & Reporting", "Analytics & Reporting");

        tm.createTag("industry:187", "Education", "Education");
        tm.createTag("industry:utilities", "Utilities", "Utilities");

        List<String> expectedIndustryTagsList = Arrays.asList("Education", "Utilities");
        List<String> expectedProductTagsList = Arrays.asList("Connection to Workday Financial Management",
                "Analytics & Reporting");
        List<String> expectedUsingWorkdayTagsList = Arrays.asList("Workday Acquisition Integrations",
                "Content Management");
        List<String> expectedProgramsAndToolsTagsList = Arrays.asList("Program Type", "The Next Level");

        TaxonomyModel taxonomyModel = context.request().adaptTo(TaxonomyModel.class);

        assertEquals(expectedIndustryTagsList, taxonomyModel.getIndustryTags());
        assertEquals(expectedProductTagsList, taxonomyModel.getProductTags());
        assertEquals(expectedUsingWorkdayTagsList, taxonomyModel.getUsingWorkdayTags());
        assertEquals(expectedProgramsAndToolsTagsList, taxonomyModel.getProgramTypeTags());
        // hasContent method uses negation
        assertEquals(false, taxonomyModel.getHasContent());
    }

    @Test
    public void testNoTagsCase() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        TaxonomyModel taxonomyModel = context.request().adaptTo(TaxonomyModel.class);
        assertEquals(true, taxonomyModel.getHasContent());
    }
}