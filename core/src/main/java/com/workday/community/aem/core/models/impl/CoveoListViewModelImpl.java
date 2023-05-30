package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Model(
        adaptables = Resource.class,
        adapters = { CoveoListViewModel.class },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoListViewModelImpl implements CoveoListViewModel {

  @Inject
  private boolean displayTags;

  @Inject
  private boolean displayMetadata;

  @Inject
  @Named("categories/.")
  private List<CategoryFacetModel> categories;

  @OSGiService
  private SearchApiConfigService searchConfigService;

  @Override
  public boolean getDisplayTags() {
    return displayTags;
  }

  @Override
  public boolean getDisplayMetadata() {
    return displayMetadata;
  }

  @Override
  public List<CategoryFacetModel> getCategories() {
    return categories;
  }

  @Override
  public String getSearchHub() {
    return searchConfigService.getSearchHub();
  }

  @Override
  public String getOrgId() {
    return searchConfigService.getOrgId();
  }
}