package com.workday.community.aem.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.workday.community.aem.core.config.CoveoIndexApiConfig;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class CoveoIndexApiConfigServiceTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoIndexApiConfigServiceTest {

    /** The CoveoIndexApiConfigService. */
    private final CoveoIndexApiConfigService service = new CoveoIndexApiConfigService();

    /** The CoveoIndexApiConfig. */
    private final CoveoIndexApiConfig mockConfig = new CoveoIndexApiConfig() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public boolean isCoveoIndexingEnabled() {
        return true;
    }

    @Override
    public String coveoApiKey() {
        return "coveoApiKey";
    }

    @Override
    public String pushApiUri() {
        return "https://www.pushapi.com";
    }

    @Override
    public String sourceApiUri() {
        return "https://www.sourceapi.com";
    }

    @Override
    public String organizationId() {
        return "organizationId";
    }

    @Override
    public String sourceId() {
        return "sourceId";
    }

    @Override
    public int batchSize() {
        return 50;
    }
  };

  @BeforeEach
  public void setup() {
    ((CoveoIndexApiConfigService)service).activate(mockConfig);
  }

  /**
   * Test all methods.
   */
  @Test
  public void testMethods() {
    assertEquals(service.getBatchSize(), mockConfig.batchSize());
    assertEquals(service.getCoveoApiKey(), mockConfig.coveoApiKey());
    assertEquals(service.getSourceApiUri(), mockConfig.sourceApiUri());
    assertEquals(service.getPushApiUri(), mockConfig.pushApiUri());
    assertEquals(service.getSourceId(), mockConfig.sourceId());
    assertEquals(service.getOrganizationId(), mockConfig.organizationId());
    assertEquals(service.isCoveoIndexEnabled(), mockConfig.isCoveoIndexingEnabled());
  }
    
}
