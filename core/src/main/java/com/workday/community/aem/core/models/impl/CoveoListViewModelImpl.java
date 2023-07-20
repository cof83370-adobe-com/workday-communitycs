package com.workday.community.aem.core.models.impl;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CategoryFacetModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CoveoUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.w3c.dom.DOMException;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Model(
        adaptables = {
            Resource.class,
            SlingHttpServletRequest.class
        },
        adapters = { CoveoListViewModel.class },
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

  /**
   * The snap service object.
   */
  @OSGiService
  private SnapService snapService;

  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      this.request = request;
    }
  }

  @Override
  public List<CategoryFacetModel> getCategories() {
    return new ArrayList<>(categories);
  }

  public JsonObject getSearchConfig() {
    JsonObject config = new JsonObject();
    config.addProperty("orgId", searchConfigService.getOrgId());
    config.addProperty("searchHub", searchConfigService.getSearchHub());
    config.addProperty("clientId", CoveoUtils.getCurrentUserClientId(request, searchConfigService, snapService));
    config.addProperty("userContext", CoveoUtils.getCurrentUserContext(request, snapService));
    return config;
  }

  @Override
  public String getExtraCriteria() throws DamException {
    throw new DOMException((short)500, "ExtraCriteria is not available for list view");
  }
}