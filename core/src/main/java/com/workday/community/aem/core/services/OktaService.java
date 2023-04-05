package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface OktaService {
    /**
     * Get Redirection Uri.
     *
     * @return Redirection Uri.
     */
    String getRedirectUri();

    /**
     * Get Custom Domain Url.
     *
     * @return Custom Domain Url.
     */
    String getCustomDomain();
}