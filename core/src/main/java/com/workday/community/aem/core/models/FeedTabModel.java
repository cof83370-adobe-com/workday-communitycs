package com.workday.community.aem.core.models;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

/**
 * The Class CategoryFacetModel.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class FeedTabModel {

    /**
     * Logger object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedTabModel.class);

    /**
     * Tag query
     */
    String tagQuery = "";

    /**
     * Selected feeds criteria
     */
    JsonObject selectedFieldsData;

    /**
     * Search config file path
     */
    private static final String COVEO_FILED_MAP_CONFIG = "/content/dam/workday-community/resources/coveo-field-map.json";

    /**
     * Tab list criteria json path
     */
    private static final String MODEL_CONFIG_FILE = "/content/dam/workday-community/resources/tab-list-criteria-data.json";

    /**
     * Search config json object
     */
    private JsonObject fieldMapConfig;


    /**
     * Tab title
     */
    @ValueMapValue
    private String tabTitle;

    /**
     * Tags
     */
    @ValueMapValue
    private String[] tags;

    /**
     * Feed fields
     */
    @ValueMapValue
    private String[] feedFields;

    /**
     * Resource resolver factory
     */
    @Inject
    ResourceResolverFactory resourceResolverFactory;

    /**
     * The cache manager
     * */
    @Inject
    CacheManagerService cacheManager;

    /**
     * Config json object
     */
    private JsonObject modelConfig;

    /**
     * Post construct to build tag query and feed fields.
     */
    @PostConstruct
    private void init() {
        try (ResourceResolver resolver = cacheManager == null?
                ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER) :
                cacheManager.getServiceResolver(READ_SERVICE_USER)) {
                tagQuery = populateTagQuery(resolver);
                selectedFieldsData = populateSelectedFieldsData(resolver);

        } catch (CacheException | LoginException  e) {
            LOGGER.error("Initialization of FeedTabModel fails.");
        }
    }

    /**
     * Frames tag query.
     * @param resolver Resource resolver
     * @return Tags criteria
     */
    private String populateTagQuery(ResourceResolver resolver) {
        TagManager tagManager = resolver != null ?  resolver.adaptTo(TagManager.class): null;
        if (this.tags == null || this.tags.length == 0 || tagManager == null) {
            return "";
        }

        // Group by namespaces
        HashMap<String, List<String>> namespaceGroups = new HashMap<String, List<String>>();
        for (String selectedTag : tags) {
            Tag tag = tagManager.resolve(selectedTag);
            if (tag == null) {
                continue;
            }
            String nameSpace = tag.getNamespace().getName();
            if (nameSpace == null) {
                continue;
            }
            if (!namespaceGroups.containsKey(nameSpace)) {
                List<String> tagList = new ArrayList<String>();
                tagList.add(selectedTag);

                namespaceGroups.put(nameSpace, tagList);
            } else {
                namespaceGroups.get(nameSpace).add(selectedTag);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : namespaceGroups.entrySet())   {
           List<String> tagList = entry.getValue();
            String nameSpace = entry.getKey();//tag.getNamespace().getName();

            JsonElement facetField = null;
            try {
                facetField = this.getFieldMapConfig(resolver).get(nameSpace);
            } catch (DamException e) {
                LOGGER.error("Exception while getting field map config.");
            }

            if (facetField == null) {
                continue;
            }

            sb.append("(@").append(facetField.getAsString()).append("==(");
            tagList.forEach(selectedTag -> {
                Tag tag = tagManager.resolve(selectedTag);
                String tagTitle = this.getTagTitle(tag);
                if (StringUtils.isNotBlank(tagTitle)) {
                    sb.append("\"").append(tagTitle).append("\",");
                }
            });
            if(sb.length() > 0 && sb.charAt(sb.length()-1) == ',') {
                sb.deleteCharAt(sb.length()-1);
            }
            sb.append("))");
        }
        return sb.toString();
    }

    /**
     * Gets the title of the input tag hierarchy.
     * @param tag Input tag
     * @return Title of tag
     */
    private String getTagTitle(Tag tag) {
        StringBuilder result = new StringBuilder();
        while (!tag.getTagID().equals(tag.getNamespace().getName()+":")) {
            if (result.length() == 0) {
                result = new StringBuilder(tag.getTitle());
            } else {
                result.insert(0, tag.getTitle() + "|");
            }
            tag = tag.getParent();
        }

        return result.toString();
    }

    /**
     * Returns the json object with data expression of selected feed fields.
     * @param resolver Resource resolver
     * @return Data expression of feed fields
     */
    private JsonObject populateSelectedFieldsData(ResourceResolver resolver) {
        JsonArray allFields = null;
        try {
            allFields = this.getModelConfig(resolver).getAsJsonArray("fields");
        } catch (DamException e) {
            LOGGER.error("Exception while getting model config.");
        }
        JsonObject selectedObject = new JsonObject();
        selectedObject.addProperty("name", tabTitle.replace(" ", "_"));
        selectedObject.addProperty("desc", tabTitle);
        selectedObject.addProperty("selected", false);
        StringBuilder dataExpression = new StringBuilder("(");
        if (feedFields != null && feedFields.length > 0 && allFields != null) {
            for (int i = 0; i < allFields.size(); i++) {
                 for (String feed : feedFields) {
                   JsonObject item = allFields.get(i).getAsJsonObject();
                   if (item.get("name").getAsString().equals(feed)) {
                       String de = item.get("dataExpression").getAsString();
                       dataExpression.append(de, 1, de.length() - 1).append(" OR ");
                   }
                 }
            }
        }
        selectedObject.addProperty("dataExpression", dataExpression.substring(0,dataExpression.length()-4).concat(")"));
        return selectedObject;
    }

    /**
     * Read tab list criteria file.
     * @param resourceResolver Resource resolver
     * @return Json object of input file
     * @throws DamException Dam Exception
     */
    private JsonObject getModelConfig(ResourceResolver resourceResolver) throws DamException {
        if (this.modelConfig == null) {
            this.modelConfig = DamUtils.readJsonFromDam(resourceResolver, MODEL_CONFIG_FILE);
        }
        return this.modelConfig;
    }

    /**
     * Returns selected tags.
     *
     * @return tags.
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Returns selected feed fields.
     *
     * @return feed fields.
     */
    public String[] getFeedFields() {
        return feedFields;
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
     * Returns selected fields data.
     *
     * @return selected fields data
     */
    public JsonObject getSelectedFieldsData() {
        return selectedFieldsData;
    }

    /**
     * Returns tag query.
     *
     * @return Tag query
     */
    public String getTagQuery() {
        return tagQuery;
    }

    /**
     * Returns search help text.
     *
     * @return search help text
     */
    public String getTabTitle() {
        return tabTitle;
    }
}