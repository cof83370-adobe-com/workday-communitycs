package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CoveoSearchConfig;
import com.workday.community.aem.core.services.SearchApiConfigService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class SearchApiConfigServiceImplTest {

  private final SearchApiConfigService searchApiConfigService = new SearchApiConfigServiceImpl();

  private final CoveoSearchConfig testConfig = TestUtil.getCoveoSearchConfig();

  @BeforeEach
  public void setup() {
    ((SearchApiConfigServiceImpl) searchApiConfigService).activate(testConfig);
  }

  @Test
  public void testAllApis() {
    assertEquals(searchApiConfigService.getSearchTokenApi(), testConfig.tokenApi());
    assertEquals(searchApiConfigService.getRecommendationApiKey(),
        testConfig.recommendationApiKey());
    assertEquals(searchApiConfigService.getTokenValidTime(), testConfig.tokenValidTime());
    assertEquals(searchApiConfigService.getSearchTokenApiKey(), testConfig.tokenApiKey());
    assertEquals(searchApiConfigService.getUpcomingEventApiKey(), testConfig.upcomingEventApiKey());
    assertEquals(searchApiConfigService.getOrgId(), testConfig.orgId());
    assertEquals(searchApiConfigService.isDevMode(), testConfig.devMode());

    assertEquals(searchApiConfigService.getSearchHub(), testConfig.searchHub());
    assertEquals(searchApiConfigService.getDefaultEmail(), testConfig.defaultEmail());
    assertEquals(searchApiConfigService.getUserIdProvider(), testConfig.userIdProvider());
    assertEquals(searchApiConfigService.getUserIdType(), testConfig.userType());
    assertEquals(searchApiConfigService.getSearchFieldLookupApi(), "foo");
    assertEquals(searchApiConfigService.getGlobalSearchUrl(), testConfig.globalSearchUrl());

  }
}
