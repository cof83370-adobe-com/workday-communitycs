package com.workday.community.aem.core.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.utils.CommunityUtils;

/**
 * The Class TaxonomyModel.
 * 
 * 
 * @author palla.pentayya
 */
@Model(adaptables = { Resource.class,
        SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TaxonomyModel {

    /** The current page. */
    @Inject
    private Page currentPage;

    /** The resolver. */
    @Inject
    private ResourceResolver resolver;

    /** The program type tags. */
    private List<String> programTypeTags = new ArrayList<>();

    /** The Release tags. */
    private List<String> releaseTags = new ArrayList<>();

    /** The product tags. */
    private List<String> productTags = new ArrayList<>();

    /** The industry tags. */
    private List<String> industryTags = new ArrayList<>();

    /** The using workday tags. */
    private List<String> usingWorkdayTags = new ArrayList<>();

    /** The has content. */
    private Boolean hasContent = false;

    /**
     * Inits the.
     */
    @PostConstruct
    protected void init() {
        if (null != currentPage) {
            final ValueMap map = currentPage.getProperties();
            programTypeTags = CommunityUtils.getPageTagsList(map, "programsToolsTags", resolver);
            releaseTags = CommunityUtils.getPageTagsList(map, "releaseTags", resolver);
            productTags = CommunityUtils.getPageTagsList(map, "productTags", resolver);
            industryTags = CommunityUtils.getPageTagsList(map, "industryTags", resolver);
            usingWorkdayTags = CommunityUtils.getPageTagsList(map, "usingWorkdayTags", resolver);
            this.hasContent = !programTypeTags.isEmpty() || !productTags.isEmpty() || !industryTags.isEmpty()
                    || !usingWorkdayTags.isEmpty();
        }

    }

    /**
     * Gets the program type tags.
     *
     * @return the program type tags
     */
    public List<String> getProgramTypeTags() {
        return Optional.ofNullable(programTypeTags).orElseGet(ArrayList::new).stream().sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Gets the product tags.
     *
     * @return the product tags
     */
    public List<String> getProductTags() {
        return Optional.ofNullable(productTags).orElseGet(ArrayList::new).stream().sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Gets the Release tags.
     *
     * @return the release tags
     */
    public List<String> getReleaseTags() {
        return Optional.ofNullable(releaseTags).orElseGet(ArrayList::new).stream().sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Gets the industry tags.
     *
     * @return the industry tags
     */
    public List<String> getIndustryTags() {
        return Optional.ofNullable(industryTags).orElseGet(ArrayList::new).stream().sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Gets the using workday tags.
     *
     * @return the using workday tags
     */
    public List<String> getUsingWorkdayTags() {
        return Optional.ofNullable(usingWorkdayTags).orElseGet(ArrayList::new).stream().sorted()
                .collect(Collectors.toUnmodifiableList());

    }

    /**
     * Gets the has content.
     *
     * @return the has content
     */
    public Boolean getHasContent() {
        return !hasContent;
    }
}
