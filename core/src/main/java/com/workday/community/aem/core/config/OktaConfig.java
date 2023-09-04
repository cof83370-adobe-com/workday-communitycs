package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Okta Config", description = "Okta Config Values")
public @interface OktaConfig {
  @AttributeDefinition(name = "Custom Okta domain name", description = "Custom domain")
  String customDomain();

  @AttributeDefinition (
          name = "Okta Integration Enabled",
          description = "Is OURM enabled",
          type = AttributeType.BOOLEAN
  )
  boolean isOktaIntegrationEnabled() default false;
}
