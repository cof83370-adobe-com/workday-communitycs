package com.workday.community.aem.core.models.impl;

import com.adobe.xfa.ut.StringUtils;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoTabListModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.LRUCacheWithTimeout;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.util.Iterator;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { CoveoTabListModel.class },
    resourceType = { CoveoTabListModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoTabListModelImpl implements CoveoTabListModel {

  protected static final String RESOURCE_TYPE = "workday-community/components/common/coveotablist";
  private static final String MODEL_CONFIG_FILE = "/content/dam/workday-community/resources/tab-list-criteria-data.json";

  @Self
  private SlingHttpServletRequest request;

  @OSGiService
  private SearchApiConfigService searchConfigService;

  private LRUCacheWithTimeout<String, String> cache = new LRUCacheWithTimeout(100, 60 * 1000);

  private Gson gson = new Gson();

  private JsonObject modelConfig;

  // TODO: this should inject from component property
  @RequestAttribute
  private String product = "Financial Management";

  @PostConstruct
  private void init() {
    this.modelConfig = DamUtils.readJsonFromDam(request.getResourceResolver(), MODEL_CONFIG_FILE);
  }

  @Override
  public JsonObject searchConfig() {
    JsonObject config = new JsonObject();
    config.addProperty("orgId", searchConfigService.getOrgId());
    config.addProperty("searchHub", searchConfigService.getSearchHub());
    config.addProperty("analytics", true);

    return config;
  }

  @Override
  public JsonObject compConfig() {
    JsonObject props = new JsonObject();
    props.addProperty("containerWidth", "400px");
    props.addProperty("rows", 5);
    props.addProperty("product", this.product);
    return props;
  }

  public JsonArray fields() {
    return this.modelConfig.getAsJsonArray("fields");
  }

  @Override
  public String productCriteria() {
    ResourceResolver resourceResolver = request.getResourceResolver();
    TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
    Tag productTag = tagManager.resolve("/content/cq:tags/product");
    if (productTag != null) {
      Iterator<Tag> products = productTag.listAllSubTags();

      while (products != null && products.hasNext()) {
        Tag pro = products.next();
        if (pro.getTitle().equals(product)) {
          return getProductCriteria(pro);
        }
      }
    }

    return "";
  }

  @Override
  public String extraCriteria() {
    return this.modelConfig.getAsJsonObject("extraCriteria").get("value").getAsString();
  }

  private String getProductCriteria(Tag product) {
    String productCriteria = cache.get(this.product);

    if (StringUtils.isEmpty(productCriteria)) {
      StringBuilder sb = new StringBuilder();
      String prodTitle = product.getTitle();
      sb.append("(@druproducthierarchy==(\"").append(prodTitle).append("\",");

      Iterator<Tag> children = product.listAllSubTags();
      while (children != null && children.hasNext()) {
        Tag child = children.next();
        sb.append("\"").append(prodTitle + "|" + child.getTitle()).append("\",");
      };

      sb.deleteCharAt(sb.length()-1);
      sb.append("))");
      productCriteria = sb.toString();
      cache.put(this.product, productCriteria);
    }

    return productCriteria;
  }
}
