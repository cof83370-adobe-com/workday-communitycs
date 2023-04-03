package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface UserGroupService {
    /**
     * List of user groups from SF.
     *
     * @param sfId
     * @return User groups
     */
    List<String> getUserGroupsBySfId(String sfId);
}
