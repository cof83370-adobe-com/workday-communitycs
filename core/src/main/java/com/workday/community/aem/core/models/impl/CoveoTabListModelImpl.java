package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.CoveoStatusModel;
import com.workday.community.aem.core.models.CoveoTabListModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import java.util.HashMap;
import java.util.Map;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = { CoveoTabListModel.class },
    resourceType = { CoveoTabListModelImpl.RESOURCE_TYPE },
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CoveoTabListModelImpl implements CoveoTabListModel {

  protected static final String RESOURCE_TYPE = "workday-community/components/common/coveotablist";

  @Override
  public Map<String, Object> searchConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("orgId", "workdayp3sqtwnv");
    config.put("searchHub", "communityv1");
    config.put("analytics", true);
    config.put("resultsPerPage", 5);
    return config;
  }
}
