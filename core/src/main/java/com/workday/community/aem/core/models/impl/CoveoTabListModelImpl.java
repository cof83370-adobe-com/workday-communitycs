package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoTabListModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Model(
    adaptables = {
        Resource.class,
        SlingHttpServletRequest.class
    },
    adapters = { CoveoTabListModel.class },
    resourceType = { CoveoTabListModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoTabListModelImpl implements CoveoTabListModel {
  private static final Logger logger = LoggerFactory.getLogger(CoveoTabListModelImpl.class);
  protected static final String RESOURCE_TYPE = "workday-community/components/common/coveotablist";
  private static final String MODEL_CONFIG_FILE = "/content/dam/workday-community/resources/tab-list-criteria-data.json";

  @ValueMapValue
  String[] productTags;

  @ValueMapValue
  String[] feeds;

  @Self
  private SlingHttpServletRequest request;

  @OSGiService
  private SearchApiConfigService searchConfigService;

  private JsonObject modelConfig;

  public void init(SlingHttpServletRequest request) {
    if (request != null) {
      logger.debug("pass-in request object, mostly for test purpose");
      this.request = request;
    }
  }

  @Override
  public JsonObject getSearchConfig() {
    JsonObject config = new JsonObject();
    config.addProperty("orgId", this.searchConfigService.getOrgId());
    config.addProperty("searchHub", this.searchConfigService.getSearchHub());
    config.addProperty("analytics", true);

    return config;
  }


  @Override
  public JsonArray getFields() throws DamException {
    return this.getModelConfig().getAsJsonArray("fields");
  }

  @Override
  public JsonArray getSelectedFields() throws DamException {
    JsonArray allFields = this.getModelConfig().getAsJsonArray("fields");
    JsonArray selectedFields = new JsonArray();
    if (feeds != null && feeds.length > 0 ) {
       for (int i=0; i<allFields.size(); i++) {
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

    sb.deleteCharAt(sb.length()-1);
    sb.append("))");
    return sb.toString();
  }

  @Override
  public String getFeedUrlBase() throws DamException {
    return this.getModelConfig().getAsJsonObject("feedUrlBase").get("value").getAsString();
  }

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
      this.modelConfig = DamUtils.readJsonFromDam(this.request.getResourceResolver(), MODEL_CONFIG_FILE);
    }
    return this.modelConfig;
  }
}
