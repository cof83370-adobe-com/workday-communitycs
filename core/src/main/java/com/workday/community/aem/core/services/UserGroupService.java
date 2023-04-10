package com.workday.community.aem.core.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface UserGroupService {
    /**
     * List of user groups from SF.
     *
     * @return User groups
     */
    List<String> getLoggedInUsersGroups();
}
