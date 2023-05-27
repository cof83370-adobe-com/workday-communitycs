package com.workday.community.aem.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.utils.DamUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class CategoryFacetModel.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CategoryFacetModel {

    /**
     * Facet field.
     */
    String field = null;

    /**
     * Facet label.
     */
    String label = null;

    /**
     * Facet sub category.
     */
    String subCategory = "";

    /**
     * Resource resolver.
     */
    @Inject
    private ResourceResolver resourceResolver;

    /**
     * Search config file path.
     */
    private static final String MODEL_CONFIG_FILE = "/content/dam/workday-community/resources/tab-list-criteria-data.json";

    /**
     * Search config json object
     */
    private static JsonObject modelConfig;

    /**
     * Category string value from jcr.
     */
    @Inject
    private String category;

    /**
     * Post construct to build facet object.
     */
    @PostConstruct
    private void init() {
        TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
        Tag tag = tagManager.resolve(category);
        if (tag == null) {
            return;
        }
        String nameSpace = tag.getNamespace().getName();
        if (nameSpace == null) {
            return;
        }
        JsonObject facet = this.getModelConfig().getAsJsonObject(nameSpace);
        if (facet != null) {
            field = facet.get("field").getAsString();
            label = facet.get("label").getAsString();
            List<String> tags = new ArrayList<>();
            while( tag != null && !tag.isNamespace() ) {
                tags.add(tag.getTitle());
                tag = tag.getParent();
            }
            if (!tags.isEmpty()) {
                Collections.reverse(tags);
                subCategory = "\"" + String.join("\", \"", tags) + "\"";
            }
        }
    }

    /**
     * Returns selected category value.
     *
     * @return category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Json object from config.
     *
     * @return
     */
    private JsonObject getModelConfig() {
        if (true || modelConfig == null) {
           modelConfig = DamUtils.readJsonFromDam(resourceResolver, MODEL_CONFIG_FILE);
        }
        return modelConfig.getAsJsonObject("tagIdToField");
    }

    /**
     * Returns coveo field name.
     *
     * @return
     */
    public String getField() {
        return field;
    }

    /**
     * Returns facet title.
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns sub category.
     *
     * @return
     */
    public String getSubCategory() {
        return subCategory;
    }

}