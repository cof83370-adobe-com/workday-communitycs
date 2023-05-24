package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoFilterModel;
import com.workday.community.aem.core.models.CoveoListViewModel;
import com.workday.community.aem.core.models.CoveoTabListModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.LRUCacheWithTimeout;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { CoveoListViewModel.class },
    resourceType = { CoveoListViewModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoListViewModelImpl implements CoveoListViewModel {
  private static final Logger logger = LoggerFactory.getLogger(CoveoListViewModelImpl.class);

  protected static final String RESOURCE_TYPE = "workday-community/components/common/coveolistview";

  @ValueMapValue
  private boolean displayTags;

  @ValueMapValue
  private boolean displayMetadata;


  @ChildResource(name="categories")
  private List<CoveoFilterModelImpl> categories;

  @OSGiService
  private SearchApiConfigService searchConfigService;

  @Override
  public JsonObject getSearchConfig() {
    JsonObject config = new JsonObject();
    config.addProperty("orgId", searchConfigService.getOrgId());
    config.addProperty("searchHub", searchConfigService.getSearchHub());
    config.addProperty("analytics", true);
    return config;
  }

  @Override
  public boolean getDisplayTags() {
    return displayTags;
  }

  @Override
  public boolean getDisplayMetadata() {
    return displayMetadata;
  }

  public List<CoveoFilterModelImpl> getCategories() {
    return categories;
  }

  public Map<String, Objects> getCategoryConfiguration() {
    return Map.of();
  }

}
