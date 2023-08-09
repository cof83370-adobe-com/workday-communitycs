package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.TaxonomyBadge;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class TaxonomyBadgeImplTest.
 */
@ExtendWith(AemContextExtension.class)
public class TaxonomyBadgeImplTest {

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
        context.load().json("/com/workday/community/aem/core/models/impl/TaxonomyBadgeImplTest.json", "/content");
        TagManager tm = context.resourceResolver().adaptTo(TagManager.class);
        populateTagManager(tm);
    }

    /**
     * Test event taxonomy badges.
     */
    @Test
    public void testEventTaxonomyBadges() {
        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        final List<String> expectedBadges = Arrays.asList("Altitude", "Raising", "Elevate", "Training");
        TaxonomyBadge taxonomyBadge = context.request().adaptTo(TaxonomyBadge.class);
        List<String> actualBadges = taxonomyBadge.getBadgeList();
        assertEquals(expectedBadges, actualBadges);
    }

    @Test
    public void testGetRetired() {
        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        TaxonomyBadge taxonomyBadge = context.request().adaptTo(TaxonomyBadge.class);
        boolean retiredTagExist = taxonomyBadge.getRetired();
        assertFalse(retiredTagExist);
    }


    /**
     * Test release notes taxonomy badges.
     *
     * @throws AccessControlException the access control exception
     * @throws InvalidTagFormatException the invalid tag format exception
     */
    @Test
    public void testReleaseNotesTaxonomyBadges() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        final List<String> expectedBadges = Arrays.asList("Enhancement", "Fix", "What's Coming Next");
        TaxonomyBadge taxonomyBadge = context.request().adaptTo(TaxonomyBadge.class);
        List<String>  actualBadges = taxonomyBadge.getBadgeList();
        assertEquals(expectedBadges, actualBadges);
    }

    /**
     * Test training catalog taxonomy badges.
     *
     * @throws AccessControlException the access control exception
     * @throws InvalidTagFormatException the invalid tag format exception
     */
    @Test
    public void testTrainingCatalogTaxonomyBadges() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content/training-catalog-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        final List<String> expectedBadges = Arrays.asList("Learn On Demand", "Learn Virtual", "Workday Pro", "Refresher Training");
        TaxonomyBadge taxonomyBadge = context.request().adaptTo(TaxonomyBadge.class);
        List<String> actualBadges = taxonomyBadge.getBadgeList();
        assertEquals(expectedBadges, actualBadges);
    }

    /**
     * Test unsuported template type.
     *
     * @throws AccessControlException the access control exception
     * @throws InvalidTagFormatException the invalid tag format exception
     */
    @Test
    public void testUnsuportedTemplateType() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content/kits-and-tools-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        final List<String> expectedBadges = new ArrayList<>();
        TaxonomyBadge taxonomyBadge = context.request().adaptTo(TaxonomyBadge.class);
        List<String> actualBadges = taxonomyBadge.getBadgeList();
        assertEquals(expectedBadges, actualBadges);
    }

    /**
     * Test no tag event taxonomy.
     *
     * @throws AccessControlException the access control exception
     * @throws InvalidTagFormatException the invalid tag format exception
     */
    @Test
    public void testNoTagEventTaxonomy() throws AccessControlException, InvalidTagFormatException {
        Page currentPage = context.currentResource("/content/untaged-event-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        final List<String> expectedBadges = new ArrayList<>();
        TaxonomyBadge taxonomyBadge = context.request().adaptTo(TaxonomyBadge.class);
        List<String> actualBadges = taxonomyBadge.getBadgeList();
        assertEquals(expectedBadges, actualBadges);
    }


    /**
     * Populate tag manager.
     *
     * @param tagManager the tag manager
     */
    private void populateTagManager(TagManager tagManager) {
        try {
            tagManager.createTag("event:event-format/conference/rising", "Raising", "test tag");
            tagManager.createTag("event:event-format/conference/altitude", "Altitude", "test tag");
            tagManager.createTag("event:event-format/conference/elevate", "Elevate", "test tag");
            tagManager.createTag("event:event-format/conference/conversations-for-a-changing-world",
                    "Conversations for a Changing World", "test tag");
            tagManager.createTag("event:event-format/conference/hackathon", "Hackathon", "test tag");
            tagManager.createTag("event:event-format/conference/devcon", "DevCon", "test tag");
            tagManager.createTag("event:event-format/webinar/overview-demo", "Overview Demo", "test tag");
            tagManager.createTag("event:event-format/webinar/feature-demo", "Feature Demo", "test tag");
            tagManager.createTag("event:event-format/webinar/training", "Training", "test tag");
            tagManager.createTag("event:event-format/webinar/customer-spotlight", "Customer Spotlight", "test tag");
            tagManager.createTag("event:event-format/webinar/workshop", "Workshop", "test tag");
            tagManager.createTag("release-notes:change-type/enhancement", "Enhancement", "test tag");
            tagManager.createTag("release-notes:change-type/fix", "Fix", "test tag");
            tagManager.createTag("release-notes:change-type/new-feature", "New Feature", "test tag");
            tagManager.createTag("release-notes:change-type/what-s-coming-next", "What's Coming Next", "test tag");
            tagManager.createTag("release-notes:change-type/retirement", "Retirement", "test tag");
            tagManager.createTag("release-notes:change-type/alert", "Alert", "test tag");
            tagManager.createTag("release-notes:change-type/alert/operations", "Operations", "test tag");
            tagManager.createTag("release-notes:change-type/alert/product-defect", "Product Defect", "test tag");
            tagManager.createTag("training:/training-format/learn-on-demand", "Learn On Demand", "test tag");
            tagManager.createTag("training:/training-format/learn-independent", "Learn Independent", "test tag");
            tagManager.createTag("training:/training-format/learn-in-person", "Learn In Person", "test tag");
            tagManager.createTag("training:/training-format/learn-virtual", "Learn Virtual", "test tag");
            tagManager.createTag("training:/training-format/refresher-training", "Refresher Training", "test tag");
            tagManager.createTag("training:/training-format/workday-pro", "Workday Pro", "test tag");
        } catch (AccessControlException | InvalidTagFormatException e) {
            fail(e);
        }
    }
}