package com.workday.community.aem.core.models.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.workday.community.aem.core.constants.PageResourceType;
import com.workday.community.aem.core.constants.TagPropertyName;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.TaxonomyBadge;

/**
 * The Class TaxonomyBadgeImpl.
 */
@Model( 
        adaptables = { Resource.class, SlingHttpServletRequest.class },
        adapters = {TaxonomyBadge.class},
        resourceType = {TaxonomyBadgeImpl.RESOURCE_TYPE}, 
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TaxonomyBadgeImpl implements TaxonomyBadge {

    /** The Constant RESOURCE_TYPE. */
    protected static final String RESOURCE_TYPE = "workday-community/components/common/taxonomybadge";

    /** The Constant RETIRED_PROP. */
    protected static final String RETIRED_PROP = "sling:alias";

    /** The badge list. */
    private List<String> badgeList = new ArrayList<>();

    /** The current page. */
    @Inject
    private Page currentPage;

    /** The resource resolver. */
    @Inject
    private ResourceResolver resourceResolver;

    /**
     * Gets the badge list.
     *
     * @return the badge list
     */
    @Override
    public List<String> getBadgeList() {
        String[] tagIDs;
        switch (currentPage.getContentResource().getResourceType()) {
            case PageResourceType.EVENT:
                tagIDs = currentPage.getProperties().get(TagPropertyName.EVENT_FORMAT, String[].class);
                badgeList = getTagTitlesByTagID(tagIDs);
                break;

            case PageResourceType.RELEASE_NOTES:
                tagIDs = currentPage.getProperties().get(TagPropertyName.RELEASE_NOTES_CHNAGE_TYPE,
                        String[].class);
                badgeList = getTagTitlesByTagID(tagIDs);
                break;

            case PageResourceType.TRAINING_CATALOG:
                tagIDs = currentPage.getProperties().get(TagPropertyName.TRAINING_FORMAT,
                        String[].class);
                badgeList = getTagTitlesByTagID(tagIDs);
                break;

            default:
                return Collections.unmodifiableList(badgeList);
        }
        return Collections.unmodifiableList(badgeList);

    }

    /**
     * Gets the tag titles by tag ID.
     *
     * @param tagIDs the tag IDs
     * @return the tag titles by tag ID
     */
    private List<String> getTagTitlesByTagID(String[] tagIDs) {
        if (tagIDs != null) {
            TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
            for (String tagID : tagIDs) {
                Tag tag = tagManager.resolve(tagID);
                if (tag != null) {
                    badgeList.add(tag.getTitle());
                }
            }
        }
        return Collections.unmodifiableList(badgeList);
    }

    /**
     * Gets the retired.
     *
     * @return the retired
     */
    @Override
    public boolean getRetired() {
     return  StringUtils.isNotBlank(currentPage.getProperties().get(RETIRED_PROP, StringUtils.EMPTY));
    }
}
