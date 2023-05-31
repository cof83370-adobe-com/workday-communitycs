package com.workday.community.aem.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

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
     * Search config file path.
     */
    private static final String COVEO_FILED_MAP_CONFIG = "/content/dam/workday-community/resources/coveo-field-map.json";

    /**
     * The user service user.
     */
    public static final String READ_SERVICE_USER = "readserviceuser";

    /**
     * Search config json object
     */
    private JsonObject fieldMapConfig;

    /**
     * Category string value from jcr.
     */
    @ValueMapValue
    private String category;

    @ValueMapValue
    private String searchHelpText;

    @Inject
    ResourceResolverFactory resourceResolverFactory;

    /**
     * Post construct to build facet object.
     */
    @PostConstruct
    private void init() {
        ResourceResolver resolver = getResourceResolver();
        TagManager tagManager = resolver != null ?  resolver.adaptTo(TagManager.class): null;
        Tag tag = tagManager != null ? tagManager.resolve(category): null;
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
            ResourceResolver resolver = getResourceResolver();
            fieldMapConfig = resolver != null ? DamUtils.readJsonFromDam(resolver, COVEO_FILED_MAP_CONFIG) : null;
            if (fieldMapConfig == null) {
                return null;
            }
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

    private ResourceResolver getResourceResolver() {
        try {
            return ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER);
        } catch (LoginException e) {
            return null;
        }
    }

}