package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoTabListModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.DamUtils;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Coveo tab list implementation.
 */
@Slf4j
@Model(
    adaptables = {
        Resource.class,
        SlingHttpServletRequest.class
    },
    adapters = {CoveoTabListModel.class},
    resourceType = {CoveoTabListModelImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoTabListModelImpl implements CoveoTabListModel {

  protected static final String RESOURCE_TYPE = "workday-community/components/common/coveotablist";

  private static final String MODEL_CONFIG_FILE =
      "/content/dam/workday-community/resources/tab-list-criteria-data.json";

  @ValueMapValue
  String[] productTags;

  @ValueMapValue
  String[] feeds;

  private JsonObject searchConfig;

  @Self
  private SlingHttpServletRequest request;

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

  private JsonObject modelConfig;

  /**
   * Initialize the CoveoTabListModelImpl object.
   *
   * @param request The request object.
   */
  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      log.debug("pass-in request object, mostly for test purpose");
      this.request = request;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonObject getSearchConfig() {
    if (this.searchConfig == null) {
      this.searchConfig =
          CoveoUtils.getSearchConfig(searchConfigService, request, snapService, userService);
    }
    return this.searchConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonArray getFields() throws DamException {
    return this.getModelConfig().getAsJsonArray("fields");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonArray getSelectedFields() throws DamException {
    JsonArray allFields = this.getModelConfig().getAsJsonArray("fields");
    JsonArray selectedFields = new JsonArray();
    if (feeds != null && feeds.length > 0) {
      for (int i = 0; i < allFields.size(); i++) {
        for (String feed : feeds) {
          JsonObject item = allFields.get(i).getAsJsonObject();
          if (item.get("name").getAsString().equals(feed)) {
            selectedFields.add(item);
          }
        }
      }
    }

    return selectedFields;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getProductCriteria() {
    ResourceResolver resourceResolver = this.request.getResourceResolver();
    TagManager tagManager = resourceResolver.adaptTo(TagManager.class);

    if (this.productTags == null || this.productTags.length == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("(@druproducthierarchy==(");

    Arrays.stream(productTags).forEach(productTag -> {
      Tag tag = tagManager.resolve(productTag);
      sb.append("\"").append(this.getTitle(tag)).append("\",");
    });

    sb.deleteCharAt(sb.length() - 1);
    sb.append("))");
    return sb.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFeedUrlBase() throws DamException {
    return this.getModelConfig().getAsJsonObject("feedUrlBase").get("value").getAsString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getExtraCriteria() throws DamException {
    return this.getModelConfig().getAsJsonObject("extraCriteria").get("value").getAsString();
  }

  private String getTitle(Tag tag) {
    StringBuilder result = new StringBuilder();
    while (!tag.getTagID().equals("product:")) {
      if (result.length() == 0) {
        result = new StringBuilder(tag.getTitle());
      } else {
        result.insert(0, tag.getTitle() + "|");
      }
      tag = tag.getParent();
    }

    return result.toString();
  }

  private JsonObject getModelConfig() throws DamException {
    if (this.modelConfig == null) {
      this.modelConfig =
          DamUtils.readJsonFromDam(this.request.getResourceResolver(), MODEL_CONFIG_FILE);
    }
    return this.modelConfig;
  }
}
