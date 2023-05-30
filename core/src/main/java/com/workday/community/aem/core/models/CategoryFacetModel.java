package com.workday.community.aem.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonElement;
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
    private static final String COVEO_FILED_MAP_CONFIG = "/content/dam/workday-community/resources/coveo-field-map.json";

    /**
     * Search config json object
     */
    private JsonObject fieldMapConfig;

    /**
     * Category string value from jcr.
     */
    @Inject
    private String category;

    @Inject
    private String searchHelpText;

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

        JsonElement facetField = this.getFieldMapConfig().get(nameSpace);
        if (facetField != null) {
            field = facetField.getAsString();
            List<String> tags = new ArrayList<>();
            while (tag != null && !tag.isNamespace()) {
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
     * @return object
     */
    private JsonObject getFieldMapConfig() {
        if (fieldMapConfig == null) {
            fieldMapConfig = DamUtils.readJsonFromDam(resourceResolver, COVEO_FILED_MAP_CONFIG);
        }
        return fieldMapConfig.getAsJsonObject("tagIdToCoveoField");
    }

    /**
     * Returns coveo field name.
     *
     * @return field name
     */
    public String getField() {
        return field;
    }

    /**
     * Returns sub category.
     *
     * @return Sub category
     */
    public String getSubCategory() {
        return subCategory;
    }

    /**
     * Returns search help text.
     *
     * @return search help text
     */
    public String getSearchHelpText() {
        return searchHelpText;
    }

}