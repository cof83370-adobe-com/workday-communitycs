package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.CoveoSearchConfig;
import com.workday.community.aem.core.services.SearchApiConfigService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class SearchApiConfigServiceImplTest {

  private final SearchApiConfigService searchApiConfigService = new SearchApiConfigServiceImpl();
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
    public String defaultEmail() {
      return "foo@workday.com";
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
    public String searchHub() {
      return "searchHub";
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
    ((SearchApiConfigServiceImpl) searchApiConfigService).activate(testConfig);
  }

  @Test
  public void testAllApis() {
    assertEquals(searchApiConfigService.getSearchTokenAPI(), testConfig.tokenApi());
    assertEquals(searchApiConfigService.getRecommendationAPIKey(), testConfig.recommendationApiKey());
    assertEquals(searchApiConfigService.getTokenValidTime(), testConfig.tokenValidTime());
    assertEquals(searchApiConfigService.getSearchTokenAPIKey(), testConfig.tokenApiKey());
    assertEquals(searchApiConfigService.getUpcomingEventAPIKey(), testConfig.upcomingEventApiKey());
    assertEquals(searchApiConfigService.getOrgId(), testConfig.orgId());
    assertEquals(searchApiConfigService.isDevMode(), testConfig.devMode());
  }
}
