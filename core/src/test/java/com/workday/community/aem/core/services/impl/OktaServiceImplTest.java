package com.workday.community.aem.core.services.impl;

import com.workday.community.aem.core.config.OktaConfig;
import com.workday.community.aem.core.services.OktaService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class OktaServiceImplTest {
  private final OktaService service = new OktaServiceImpl();
  private final OktaConfig testConfig = new OktaConfig() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }

    @Override
    public String customDomain() {
      return "community";
    }

    @Override
    public boolean isOktaIntegrationEnabled() {
      return false;
    }


  };

  @BeforeEach
  public void setup() {
    ((OktaServiceImpl)service).activate(testConfig);
  }

  @Test
  public void testConfigs() {
    assertEquals(service.getCustomDomain(), testConfig.customDomain());
  }
}
