package com.workday.community.aem.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CoveoFilterModel {

    String field = null;

    String label = null;

    List<String> categories = List.of();

    @Inject
    private String category;

    @Inject
    private ResourceResolver resourceResolver;

    private static final Map<String, String> tagidToField = Map.of("product", "productTags",
            "using-workday", "usingWorkdayTags", "programs-and-tools", "programsToolsTags",
            "release", "releaseTags", "industry", "industryTags", "user", "userTags",
            "region-and-country", "regionCountryTags", "training", "trainingTags");

    private static final Map<String, String> tagIdToLabel = Map.of("product", "productTags",
            "using-workday", "usingWorkdayTags", "programs-and-tools", "programsToolsTags",
            "release", "releaseTags", "industry", "industryTags", "user", "userTags",
            "region-and-country", "regionCountryTags", "training", "trainingTags");

    @PostConstruct
    private void init() {
        TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
        Tag tag = tagManager.resolve(category);
        String nameSpace = tag.getNamespace().toString();
        label = nameSpace;
        field = nameSpace;
        while( tag != null && !tag.isNamespace() ) {
            categories.add(tag.getTitle());
            tag = tag.getParent();
        }
    }

    String getField() {
        return field;
    }

    String getLabel() {
        return label;
    }


}