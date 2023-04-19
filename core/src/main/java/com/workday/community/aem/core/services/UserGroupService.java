package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.SnapConfig;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface UserGroupService {
    /**
     * Activate user group service.
     *
     * @param config Snap config.
     */
    void activate(SnapConfig config);

    /**
     * List of user groups from SF.
     *
     * @return User groups
     */
    List<String> getLoggedInUsersGroups();
}
