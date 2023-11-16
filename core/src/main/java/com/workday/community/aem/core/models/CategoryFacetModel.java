package com.workday.community.aem.core.models;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * The Class CategoryFacetModel.
 */
@Slf4j
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CategoryFacetModel {

  /**
   * Search config file path.
   */
  private static final String COVEO_FILED_MAP_CONFIG =
      "/content/dam/workday-community/resources/coveo-field-map.json";

  /**
   * Facet field.
   */
  @Getter
  private String field = null;

  /**
   * Facet sub category.
   */
  @Getter
  private String subCategory = "";

  @Inject
  private ResourceResolverFactory resourceResolverFactory;

  /**
   * The cache manager.
   */
  @Inject
  private CacheManagerService cacheManager;

  /**
   * Search config json object.
   */
  private JsonObject fieldMapConfig;

  /**
   * Category string value from jcr.
   */
  @Getter
  @ValueMapValue
  private String category;

  /**
   * Search help text.
   */
  @Getter
  @ValueMapValue
  private String searchHelpText;

  /**
   * Facet label.
   */
  @Getter
  private String label;

  /**
   * Post construct to build facet object.
   */
  @PostConstruct
  private void init() {
    try (ResourceResolver resolver = cacheManager == null
        ? ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER)
        : cacheManager.getServiceResolver(READ_SERVICE_USER)) {
      TagManager tagManager = resolver != null ? resolver.adaptTo(TagManager.class) : null;
      Tag tag = tagManager != null ? tagManager.resolve(category) : null;
      if (tag == null) {
        return;
      }
      String nameSpace = tag.getNamespace().getName();
      label = tag.getNamespace().getTitle();
      if (nameSpace == null) {
        return;
      }

      JsonElement facetField = this.getFieldMapConfig(resolver).get(nameSpace);

      if (facetField != null) {
        field = facetField.getAsString();
        List<String> tags = new ArrayList<>();
        while (!tag.isNamespace()) {
          tags.add(tag.getTitle());
          tag = tag.getParent();
        }
        if (!tags.isEmpty()) {
          Collections.reverse(tags);
          subCategory = "\"" + String.join("\", \"", tags) + "\"";
        }
      }
    } catch (CacheException | LoginException e) {
      log.error("Initialization of CategoryFacetModel fails, {}", e.getMessage());
    }
  }

  /**
   * Returns filed mapping object.
   *
   * @param resourceResolver Resource resolver object
   * @return field map config.
   */
  private JsonObject getFieldMapConfig(ResourceResolver resourceResolver) {
    if (fieldMapConfig == null) {
      try {
        fieldMapConfig = DamUtils.readJsonFromDam(resourceResolver, COVEO_FILED_MAP_CONFIG);
      } catch (DamException e) {
        log.error("getFieldMapConfig() call throws exception: {}", e.getMessage());
        return new JsonObject();
      }
    }
    return fieldMapConfig.getAsJsonObject("tagIdToCoveoField");
  }

}
