package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.CoveoFilterModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.pojos.CategoryFacet;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.osgi.service.component.annotations.Reference;

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
  private List<CoveoFilterModel> categories;

  @Reference
  private SnapService snapService;

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
  public List<CoveoFilterModel> getCategories() {
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

  public List<CategoryFacet> getFacets() {
    for (CoveoFilterModel category : categories) {
      new CategoryFacet();
    }
    return List.of();
  }
}