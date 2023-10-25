package com.workday.community.aem.core.models.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoRelatedInformationModel;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.DamUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.service.component.annotations.Reference;

/**
 * Coveo related information mode implementation.
 */
@Slf4j
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    adapters = {CoveoRelatedInformationModel.class},
    resourceType = {CoveoRelatedInformationModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoRelatedInformationModelImpl implements CoveoRelatedInformationModel {

  protected static final String RESOURCE_TYPE =
      "/content/workday-community/components/common/relatedinformation";

  private static final String COVEO_FILED_MAP_CONFIG =
      "/content/dam/workday-community/resources/coveo-field-map.json";

  /**
   * The cache manager.
   */
  @Reference
  CacheManagerService cacheManager;

  @Self
  private SlingHttpServletRequest request;

  private JsonObject searchConfig;

  private List<String> facetFields;

  /**
   * SearchConfig service object.
   */
  @OSGiService
  private SearchApiConfigService searchConfigService;

  /**
   * The drupal service object.
   */
  @OSGiService
  private DrupalService drupalService;

  @OSGiService
  private UserService userService;

  /**
   * Initialize the Coveo related information mode model.
   *
   * @param request The request object.
   */
  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      this.request = request;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getFacetFields() throws DamException {
    if (facetFields != null) {
      return Collections.unmodifiableList(facetFields);
    }

    facetFields = new ArrayList<>();

    // fall back to the page tag properties
    PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
    if (pageManager == null) {
      return Collections.unmodifiableList(facetFields);
    }

    String pagePath = request.getPathInfo();
    // Trim .html at end.
    pagePath = pagePath.substring(0, pagePath.indexOf("."));
    Page page = pageManager.getPage(pagePath);
    if (page == null) {
      log.error(
          "getFacetFields in CoveoRelatedInformationModelImpl failed because current Page "
              + "unresolved, path: {}",
          pagePath);
      return Collections.unmodifiableList(facetFields);
    }

    Tag[] tags = page.getTags();
    if (tags == null || tags.length == 0) {
      return Collections.unmodifiableList(facetFields);
    }
    try (ResourceResolver resolver = cacheManager.getServiceResolver(READ_SERVICE_USER)) {
      JsonObject fieldMapConfig = DamUtils.readJsonFromDam(resolver, COVEO_FILED_MAP_CONFIG)
          .getAsJsonObject("tagIdToCoveoField");
      for (Tag tag : tags) {
        JsonElement facetFieldObj = fieldMapConfig.get(tag.getNamespace().getName());
        if (facetFieldObj == null || facetFieldObj.isJsonNull()) {
          continue;
        }
        String basePath = "";
        while (tag.getParent() != null) {
          if (basePath.isEmpty()) {
            basePath = tag.getTitle();
          } else {
            basePath = String.format("%s:%s", tag.getTitle(), basePath);
          }
          tag = tag.getParent();
        }
        if (!basePath.isEmpty()) {
          facetFields.add(String.format("%s::%s", facetFieldObj.getAsString(), basePath));
        } else {
          facetFields.add(facetFieldObj.getAsString());
        }
      }
    } catch (CacheException e) {
      throw new DamException(String.format(
          "Exception in getFacetFields call in CoveoRelatedInformationModelImpl. error %s",
          e.getMessage()));
    }

    return Collections.unmodifiableList(facetFields);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonObject getSearchConfig() {
    if (this.searchConfig == null) {
      this.searchConfig = CoveoUtils.getSearchConfig(
          searchConfigService,
          request,
          drupalService,
          userService);
    }
    return this.searchConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getExtraCriteria() throws DamException {
    throw new DamException("ExtraCriteria is not available for related information currently");
  }

}
