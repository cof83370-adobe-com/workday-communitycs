package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.CoveoSearchConfig;
import com.workday.community.aem.core.services.SearchService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class SearchServiceImplTest {

  private final SearchService searchService = new SearchServiceImpl();
  private final CoveoSearchConfig testConfig = new CoveoSearchConfig() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public String tokenApi() {
      return "http://coveo/token";
    }

    @Override
    public String tokenApiKey() {
      return "tokenApiKey";
    }

    @Override
    public String recommendationApiKey() {
      return "recommendationApiKey";
    }

    @Override
    public String upcomingEventApiKey() {
      return "upcomingEventApiKey";
    }

    @Override
    public String orgId() {
      return "orgId";
    }

    @Override
    public int tokenValidTime() {
      return 12000;
    }

    @Override
    public boolean devMode() {
      return true;
    }
  };

  @BeforeEach
  public void setup() {
    ((SearchServiceImpl) searchService).activate(testConfig);
  }

  @Test
  public void testAllApis() {
    assertEquals(searchService.getSearchTokenAPI(), testConfig.tokenApi());
    assertEquals(searchService.getRecommendationAPIKey(), testConfig.recommendationApiKey());
    assertEquals(searchService.getTokenValidTime(), testConfig.tokenValidTime());
    assertEquals(searchService.getSearchTokenAPIKey(), testConfig.tokenApiKey());
    assertEquals(searchService.getUpcomingEventAPIKey(), testConfig.upcomingEventApiKey());
    assertEquals(searchService.getOrgId(), testConfig.orgId());
    assertEquals(searchService.isDevMode(), testConfig.devMode());
  }
}
