package com.workday.community.aem.core.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.config.SnapConfig;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface UserGroupService {
    void activate(SnapConfig config);

    /**
     * List of user groups from SF.
     *
     * @return User groups
     */
    List<String> getLoggedInUsersGroups();
}
