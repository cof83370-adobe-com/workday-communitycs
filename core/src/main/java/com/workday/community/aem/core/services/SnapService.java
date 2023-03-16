package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface SnapService {
    void activate(SnapConfig config);
    void setResourceResolverFactory(ResourceResolverFactory resourceResolverFactory);

    String getUserHeaderMenu(String sfId);

    ProfilePhoto getProfilePhoto(String sfId);
}
