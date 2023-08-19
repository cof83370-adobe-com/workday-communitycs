package com.workday.community.aem.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.services.cache.EhCacheManager;
import com.workday.community.aem.core.utils.DamUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

/**
 * The Class CategoryFacetModel.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CategoryFacetModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryFacetModel.class);

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

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** The cache manager */
    @Reference
    EhCacheManager ehCacheManager;

    /**
     * Post construct to build facet object.
     */
    @PostConstruct
    private void init() throws DamException {
        try (ResourceResolver resolver = ehCacheManager.getServiceResolver(READ_SERVICE_USER)) {
            TagManager tagManager = resolver != null ?  resolver.adaptTo(TagManager.class): null;
            Tag tag = tagManager != null ? tagManager.resolve(category): null;
            if (tag == null) {
                return;
            }
            String nameSpace = tag.getNamespace().getName();
            if (searchHelpText == null) {
                searchHelpText = tag.getNamespace().getTitle();
            }
            if (nameSpace == null) {
                return;
            }

            JsonElement facetField = this.getFieldMapConfig(resolver).get(nameSpace);
            if (facetField != null) {
                field = facetField.getAsString();
                StringBuilder sb = new StringBuilder();
                List<String> tags = new ArrayList<>();
                while (!tag.isNamespace()) {
                    if (sb.length() > 0) {
                        sb.insert(0, ", ");
                    }
                    sb.insert(0, "\"" + tag.getTitle() + "\"");

                    tags.add(tag.getTitle());
                    tag = tag.getParent();

                }
                if (!tags.isEmpty()) {
                    Collections.reverse(tags);
                    subCategory = "\"" + String.join("\", \"", tags) + "\"";
                }
            }
        } catch (CacheException e) {
            LOGGER.error("Initialization of CategoryFacetModel fails");
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
     * Returns filed mapping object
     *
     * @param resourceResolver Resource resolver object
     * @return field map config.
     */
    private JsonObject getFieldMapConfig(ResourceResolver resourceResolver) throws DamException {
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