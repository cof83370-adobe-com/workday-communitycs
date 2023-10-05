package com.workday.community.aem.core.services.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.OktaConfig;
import com.workday.community.aem.core.services.OktaService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The OktaService implementation class.
 */
@Component(
    service = OktaService.class,
    immediate = true,
    configurationPid = "com.workday.community.aem.core.config.OktaConfig"
)
@Designate(ocd = OktaConfig.class)
public class OktaServiceImpl implements OktaService {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private OktaConfig config;

  @Activate
  public void activate(OktaConfig config) {
    this.config = config;
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public String getCustomDomain() {
    return config.customDomain();
  }

  @Override
  public boolean isOktaIntegrationEnabled() {
    return config.isOktaIntegrationEnabled();
  }


}
