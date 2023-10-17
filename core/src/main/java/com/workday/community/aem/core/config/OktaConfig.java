package com.workday.community.aem.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The Okta configuration interface.
 */
@ObjectClassDefinition(name = "Okta Config", description = "Okta Config Values")
public @interface OktaConfig {

  /**
   * The Okta domain name.
   *
   * @return The domain.
   */
  @AttributeDefinition(name = "Custom Okta domain name", description = "Custom domain")
  String customDomain();

  /**
   * Whether the Okta integration is enabled.
   *
   * @return True if enabled, otherwise false.
   */
  @AttributeDefinition(
      name = "Okta Integration Enabled",
      description = "Is OURM enabled",
      type = AttributeType.BOOLEAN
  )
  boolean isOktaIntegrationEnabled() default false;

}
