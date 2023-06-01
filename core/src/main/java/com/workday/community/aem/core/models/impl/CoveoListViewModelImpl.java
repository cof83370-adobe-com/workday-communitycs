package com.workday.community.aem.core.models.impl;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Model(
        adaptables = Resource.class,
        adapters = { CoveoListViewModel.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoListViewModelImpl implements CoveoListViewModel {
  @ChildResource
  @Named("categories/.")
  private List<CategoryFacetModel> categories;

  @OSGiService
  private SearchApiConfigService searchConfigService;

  @Override
  public List<CategoryFacetModel> getCategories() {
    return new ArrayList<>(categories);
  }

  public JsonObject getSearchConfig() {
    JsonObject config = new JsonObject();
    config.addProperty("orgId", searchConfigService.getOrgId());
    config.addProperty("searchHub", searchConfigService.getSearchHub());
    return config;
  }
}