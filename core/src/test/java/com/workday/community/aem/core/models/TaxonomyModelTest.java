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
     * @throws AccessControlException the access control exception
     * @throws InvalidTagFormatException the invalid tag format exception
     */
    @Test
    public void testInit() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        TagManager tm = context.resourceResolver().adaptTo(TagManager.class);
        tm.createTag("programs-and-tools:program-type", "Program Type", "Program Type");
        tm.createTag("programs-and-tools:program-type/the-next-level", "The Next Level", "the next level");
        tm.createTag("release:149", "Workday 10 - Retired", "Workday 10 - Retired");
        tm.createTag("release:165", "Workday 11 - Retired", "Workday 11 - Retired");

        tm.createTag("using-workday:workday-acquisition-integrations", "Workday Acquisition Integrations",
                "Workday Acquisition Integrations");
        tm.createTag("using-workday:7028/7046", "Content Management", "Content Management");

        tm.createTag("product:4903/7728", "Connection to Workday Financial Management",
                "workday 7 - retired");
        tm.createTag("product:92", "Analytics & Reporting", "Analytics & Reporting");

        tm.createTag("industry:187", "Education", "Education");
        tm.createTag("industry:utilities", "Utilities", "Utilities");

        List<String> expectedIndustryTagsList = Arrays.asList("Education", "Utilities");
        List<String> expectedProductTagsList = Arrays.asList(  "Analytics & Reporting","Connection to Workday Financial Management"
              );
        List<String> expectedReleaseTagsList = Arrays.asList(  "Workday 10 - Retired","Workday 11 - Retired"
        );
        List<String> expectedUsingWorkdayTagsList = Arrays.asList("Content Management","Workday Acquisition Integrations"
                );
        List<String> expectedProgramsAndToolsTagsList = Arrays.asList("Program Type", "The Next Level");

        TaxonomyModel taxonomyModel = context.request().adaptTo(TaxonomyModel.class);

        assertEquals(expectedIndustryTagsList, taxonomyModel.getIndustryTags());
        assertEquals(expectedProductTagsList, taxonomyModel.getProductTags());
        assertEquals(expectedReleaseTagsList, taxonomyModel.getReleaseTags());
        assertEquals(expectedUsingWorkdayTagsList, taxonomyModel.getUsingWorkdayTags());
        assertEquals(expectedProgramsAndToolsTagsList, taxonomyModel.getProgramTypeTags());
        // hasContent method uses negation
        assertEquals(false, taxonomyModel.getHasContent());
    }

    /**
     * Test with all tags empty.
     *
     * @throws AccessControlException the access control exception
     * @throws InvalidTagFormatException the invalid tag format exception
     */
    @Test
    public void testWithAllTagsEmpty() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        TaxonomyModel taxonomyModel = context.request().adaptTo(TaxonomyModel.class);
        assertEquals(true, taxonomyModel.getHasContent());
    }

    /**
     * Test with program type tags empty.
     *
     * @throws AccessControlException the access control exception
     * @throws InvalidTagFormatException the invalid tag format exception
     */
    @Test
    public void testWithProgramTypeTagsEmpty() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        TagManager tm = context.resourceResolver().adaptTo(TagManager.class);

        tm.createTag("using-workday:workday-acquisition-integrations", "Workday Acquisition Integrations",
                "Workday Acquisition Integrations");
        tm.createTag("using-workday:7028/7046", "Content Management", "Content Management");

        tm.createTag("product:4903/7728", "Connection to Workday Financial Management",
                "workday 7 - retired");
        tm.createTag("product:92", "Analytics & Reporting", "Analytics & Reporting");

        tm.createTag("industry:187", "Education", "Education");
        tm.createTag("industry:utilities", "Utilities", "Utilities");

        TaxonomyModel taxonomyModel = context.request().adaptTo(TaxonomyModel.class);
        assertEquals(false, taxonomyModel.getHasContent());
    }
   
}