package com.workday.community.aem.core.models.impl;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Model(adaptables = {
    Resource.class,
    SlingHttpServletRequest.class
}, adapters = { CoveoListViewModel.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
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

  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      this.request = request;
    }
  }

  @Override
  public List<CategoryFacetModel> getCategories() {
    return categories == null ? new ArrayList<>() : new ArrayList<>(categories);
  }

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

  @Override
  public String getExtraCriteria() throws DamException {
    throw new DamException("ExtraCriteria is not available for list view");
  }
}