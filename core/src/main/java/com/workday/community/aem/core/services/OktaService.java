package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface OktaService {
  /**
   * Get Custom Domain Url.
   *
   * @return Custom Domain Url.
   */
  String getCustomDomain();

  /**
   * Check Okta integration is enabled or not.
   * ( alternative to Sling settings service To test whether it is author mode or publish mode)
   *
   * @return boolean.
   */
  boolean isOktaIntegrationEnabled();
}
