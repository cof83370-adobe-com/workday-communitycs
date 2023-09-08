package com.workday.community.aem.core.services;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

/**
 * The Interface UserGroupService.
 */
@ProviderType
public interface UserGroupService {
    /**
     * Validates the user based on Roles tagged to the page and User roles from
     * Salesforce.
     *
     * @param request the Sling Request object
     * @param pagePath The Requested page path.
     * @return boolean: True if user has permissions to access the page, otherwise false.
     */
    boolean validateCurrentUser(SlingHttpServletRequest request, String pagePath);

    /**
     * Validates if logged-in user has the passed in access control tags.
     *
     * @param request the current Sling request object.
     * @param accessControlTags List of access control tags to be checked against.
     * @return True if logged-in user has given access control tags, else false.
     */
    boolean validateCurrentUser(SlingHttpServletRequest request, List<String> accessControlTags);

    /**
     * Returns current logged-in users groups.
     * Check whether user node has property roles. If it is there then return from
     * node property. If not, call API for roles.
     *
     * @param request: current Sling request object.
     * @return User group list.
     */
    List<String> getCurrentUserGroups(SlingHttpServletRequest request);
}
