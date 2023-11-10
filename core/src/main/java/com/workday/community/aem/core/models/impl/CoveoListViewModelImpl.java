package com.workday.community.aem.core.models.impl;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * Coveo list view model implementation.
 */
@Slf4j
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    adapters = {CoveoListViewModel.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoListViewModelImpl implements CoveoListViewModel {

  @Self
  private SlingHttpServletRequest request;

  @ChildResource
  @Named("categories/.")
  private List<CategoryFacetModel> categories;

  /**
   * SearchConfig service object.
   */
  @OSGiService
  private SearchApiConfigService searchConfigService;

  @OSGiService
  private UserService userService;

  /**
   * The drupal service object.
   */
  @OSGiService
  private DrupalService drupalService;

  private JsonObject searchConfig;

  /**
   * Initializer for the Coveo list view model class.
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
  public List<CategoryFacetModel> getCategories() {
    return categories == null ? new ArrayList<>() : new ArrayList<>(categories);
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
  public String getExtraCriteria() {
    log.error("ExtraCriteria is not available for list view");
    return "";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonObject getHelpTextMap() {
    JsonObject helpTextMap = new JsonObject();
    categories.forEach(category -> {
      helpTextMap.addProperty(category.getLabel(), category.getSearchHelpText());
    });
    return helpTextMap;
  }
}
