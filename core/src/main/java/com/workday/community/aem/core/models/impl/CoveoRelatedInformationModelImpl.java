package com.workday.community.aem.core.models.impl;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.CoveoRelatedInformationModel;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;

@Model(
    adaptables = {
        Resource.class,
        SlingHttpServletRequest.class
    },
    adapters = { CoveoRelatedInformationModel.class },
    resourceType = { CoveoRelatedInformationModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoRelatedInformationModelImpl implements CoveoRelatedInformationModel {
  private static final Logger LOGGER = LoggerFactory.getLogger(CoveoEventFeedModelImpl.class);
  private static final String COVEO_FILED_MAP_CONFIG = "/content/dam/workday-community/resources/coveo-field-map.json";
  protected static final String RESOURCE_TYPE = "/content/workday-community/components/common/relatedinformation";

  @ValueMapValue
  String[] categories;

  @Inject
  ResourceResolverFactory resourceResolverFactory;

  @Self
  private SlingHttpServletRequest request;

  private JsonObject searchConfig;
  private List<String> facetFields;

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
  public List<String> getFacetFields() throws DamException {
    if (facetFields != null) {
      return Collections.unmodifiableList(facetFields);
    }

    try {
      ResourceResolver resolver = ResolverUtil.newResolver(resourceResolverFactory, READ_SERVICE_USER);
      JsonObject fieldMapConfig = DamUtils.readJsonFromDam(resolver, COVEO_FILED_MAP_CONFIG).getAsJsonObject("tagIdToCoveoField");
      TagManager tagManager = resolver.adaptTo(TagManager.class);

      for (String category : categories) {
        Tag tag = Objects.requireNonNull(tagManager).resolve(category);
        if (tag != null) {
          String nameSpace = tag.getNamespace().getName();
          JsonElement facetFieldObj = fieldMapConfig.get(nameSpace);
          if (facetFieldObj != null) {
            facetFields.add(facetFieldObj.getAsString());
          }
        }
      }
    } catch (LoginException e) {
      LOGGER.error("exception in getFacetFields call in CoveoRelatedInformationModelImpl.");
      throw new DamException(e.getMessage());
    }
    return Collections.unmodifiableList(facetFields);
  }

  @Override
  public JsonObject getSearchConfig() {
    if (this.searchConfig == null) {
      this.searchConfig = CoveoUtils.getSearchConfig(searchConfigService, request, snapService);
    }
    return this.searchConfig;
  }

  @Override
  public String getExtraCriteria() throws DamException {
    throw new DamException("ExtraCriteria is not available for related information currently");
  }
}
