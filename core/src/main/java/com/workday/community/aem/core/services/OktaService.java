package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface OktaService {
    String getRedirectUri();
    String getCustomDomain();
}