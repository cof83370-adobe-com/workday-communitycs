package com.workday.community.aem.core.models.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.FeedTabModel;
import com.workday.community.aem.core.models.TabularListViewModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.DamUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

/**
 * TabularListViewModel implementation class.
 */
@Model(
    adaptables = {
        Resource.class,
        SlingHttpServletRequest.class
    },
    adapters = {TabularListViewModel.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TabularListViewModelImpl implements TabularListViewModel {
  /**
   * Tab list json file.
   */
  private static final String MODEL_CONFIG_FILE
      = "/content/dam/workday-community/resources/tab-list-criteria-data.json";

  /**
   * Default search redirect Url.
   */
  private static final String DEFAULT_SEARCH_REDIRECT =
      "https://resourcecenter.workday.com/en-us/wrc/home/search.html#tab=all-results&f[commoncontenttype]=";

  @Self
  private SlingHttpServletRequest request;

  @ChildResource
  @Named("searches/.")
  private List<FeedTabModel> searches;

  /**
   * SearchConfig service object.
   */
  @OSGiService
  private SearchApiConfigService searchConfigService;

  @OSGiService
  private UserService userService;

  /**
   * The snap service object.
   */
  @OSGiService
  private SnapService snapService;

  private JsonObject searchConfig;

  private JsonObject modelConfig;

  /**
   * Initialize method.
   *
   * @param request Sling request
   */
  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      this.request = request;
    }
  }

  /**
   * Returns the list of coveo searches.
   *
   * @return List of feed tab models
   */
  @Override
  public List<FeedTabModel> getSearches() {
    return searches == null ? new ArrayList<>() : new ArrayList<>(searches);
  }

  /**
   * Get search config.
   *
   * @return Jsonobject of search object
   */
  @Override
  public JsonObject getSearchConfig() {
    if (this.searchConfig == null) {
      this.searchConfig = CoveoUtils.getSearchConfig(
          searchConfigService,
          request,
          snapService,
          userService);
    }
    return this.searchConfig;
  }

  /**
   * Get field names.
   *
   * @return Array of fields
   * @throws DamException Dam Exception
   */
  @Override
  public JsonArray getFields() throws DamException {
    return this.getModelConfig().getAsJsonArray("fields");
  }

  /**
   * Returns extra criteria of search.
   *
   * @return Expression string
   * @throws DamException Dam Exception
   */
  @Override
  public String getExtraCriteria() throws DamException {
    return getModelConfig().getAsJsonObject("extraCriteria").get("value").getAsString();
  }

  /**
   * Gets feed url for all items.
   *
   * @return Url string
   * @throws DamException Dam Exception
   */
  @Override
  public String getFeedUrlBase() throws DamException {
    if (searchConfigService == null) {
      return DEFAULT_SEARCH_REDIRECT;
    }

    String searchUrlFromConfig = searchConfigService.getGlobalSearchUrl();
    return StringUtils.isBlank(searchUrlFromConfig) ? DEFAULT_SEARCH_REDIRECT :
        searchUrlFromConfig.concat("#tab=all-results&f[commoncontenttype]=");
  }

  /**
   * Read tab list criteria from DAM.
   *
   * @return Json object of the file data
   * @throws DamException Dam Exception
   */
  private JsonObject getModelConfig() throws DamException {
    if (this.modelConfig == null) {
      this.modelConfig = DamUtils.readJsonFromDam(this.request.getResourceResolver(), MODEL_CONFIG_FILE);
    }
    return this.modelConfig;
  }

  /**
   * Returns array of search data expression objects.
   *
   * @return Array of json objects
   * @throws DamException Dam Exception
   */
  @Override
  public JsonArray getSelectedFields() throws DamException {
    JsonArray fields = new JsonArray();
    for (int i = 0; i < searches.size(); i++) {
      String tagQuery = searches.get(i).getTagQuery();
      JsonObject selectedFieldsData = searches.get(i).getSelectedFieldsData();
      String dataExpression = selectedFieldsData.get("dataExpression").getAsString();
      selectedFieldsData.addProperty("dataExpression", dataExpression + tagQuery + getExtraCriteria());
      fields.add(selectedFieldsData);
    }
    return fields;
  }
}