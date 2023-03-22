package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.CoveoSearchConfig;
import com.workday.community.aem.core.services.SearchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The Coveo search implementation class.
 */
@Component(
    service = SearchService.class,
    property = {
        "service.pid=aem.core.services.search"
    },
    configurationPolicy = ConfigurationPolicy.OPTIONAL,
    immediate = true
)
@Designate(ocd = CoveoSearchConfig.class)
public class SearchServiceImpl implements SearchService {

  private CoveoSearchConfig config;

  @Activate
  @Modified
  public void activate(CoveoSearchConfig config) {
     this.config = config;
  }

  @Override
  public String getOrgId() {
    return config.orgId();
  }

  @Override
  public String getSearchTokenAPI() {
    return config.tokenApi();
  }

  @Override
  public String getSearchTokenAPIKey() {
    return config.tokenApiKey();
  }

  @Override
  public String getRecommendationAPIKey() {
    return config.recommendationApiKey();
  }

  @Override
  public String getUpcomingEventAPIKey() {
    return config.upcomingEventApiKey();
  }

  @Override
  public int getTokenValidTime() {
    return config.tokenValidTime();
  }

  @Override
  public boolean isDevMode() {
    return config.devMode();
  }
}
