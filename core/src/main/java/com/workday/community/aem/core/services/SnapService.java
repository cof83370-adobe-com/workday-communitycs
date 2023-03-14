package com.workday.community.aem.core.services;

import com.workday.community.aem.core.pojos.ProfilePhoto;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface SnapService {
    String getUserHeaderMenu(String sfId);

    ProfilePhoto getProfilePhoto(String sfid);
}
