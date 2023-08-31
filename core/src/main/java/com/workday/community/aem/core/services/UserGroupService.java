package com.workday.community.aem.core.services;

import com.workday.community.aem.core.exceptions.CacheException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface UserGroupService {
    /**
     * Validates the user based on Roles tagged to the page and User roles from
     * Salesforce.
     *
     * @param request: the Sling Request object
     * @param pagePath                 : The Requested page path.
     * @return boolean: True if user has permissions otherwise false.
     */
    boolean validateCurrentUser(SlingHttpServletRequest request, String pagePath) throws CacheException;

    /**
     * Validates if logged-in user has the passed in access control tags.
     *
     * @param request the current Sling request object.
     * @param accessControlTags List of access control tags to be checked against.
     * @return True if logged-in user has given access control tags, else false.
     */
    boolean validateCurrentUser(SlingHttpServletRequest request, List<String> accessControlTags);
}
