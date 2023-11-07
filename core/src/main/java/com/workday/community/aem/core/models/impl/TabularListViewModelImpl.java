package com.workday.community.aem.core.models.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.FeedTabModel;
import com.workday.community.aem.core.models.TabularListViewModel;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.DamUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
  private DrupalService drupalService;

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
          drupalService,
          userService);
    }
    return this.searchConfig;
  }

  /**
   * Get field names.
   *
   * @return Array of fields
   */
  @Override
  public JsonArray getFields() {
    return this.getModelConfig().getAsJsonArray("fields");
  }

  /**
   * Returns extra criteria of search.
   *
   * @return Expression string
   */
  @Override
  public String getExtraCriteria() {
    return getModelConfig().getAsJsonObject("extraCriteria").get("value").getAsString();
  }

  /**
   * Gets feed url for all items.
   *
   * @return Url string
   */
  @Override
  public String getFeedUrlBase() {
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
   */
  private JsonObject getModelConfig() {
    if (this.modelConfig == null) {
      try {
        this.modelConfig = DamUtils.readJsonFromDam(this.request.getResourceResolver(), MODEL_CONFIG_FILE);
      } catch (DamException e) {
        log.error("Failed to call getModelConfig(): {}", e.getMessage());
        return new JsonObject();
      }
    }
    return this.modelConfig;
  }

  /**
   * Returns array of search data expression objects.
   *
   * @return Array of json objects
   */
  @Override
  public JsonArray getSelectedFields() {
    JsonArray fields = new JsonArray();
    for (int i = 0; i < searches.size(); i++) {
      FeedTabModel search = searches.get(i);
      String tagQuery = search.getTagQuery();
      JsonObject selectedFieldsData = search.getSelectedFieldsData();
      // Update dataExpression.
      String dataExpression = selectedFieldsData.get("dataExpression").getAsString();
      selectedFieldsData.addProperty("dataExpression", dataExpression + tagQuery + getExtraCriteria());
      // Update name with index of search tab.
      String name = selectedFieldsData.get("name").getAsString();
      selectedFieldsData.addProperty("name", name + "_" + i);
      fields.add(selectedFieldsData);
    }
    return fields;
  }
}