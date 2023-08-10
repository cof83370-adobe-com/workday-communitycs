package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.exceptions.OurmException;
import org.apache.sling.api.resource.ResourceResolver;
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
    List<String> getLoggedInUsersGroups(ResourceResolver resourceResolve) throws OurmException;

    /**
     *
     * @param resourceResolver        the admin user resource resolver
     * @param requestResourceResolver the request resource resolver
     * @param pagePath                the requested page path
     * @return boolean to indicate whether the user is valid user or invalid user.
     */
    boolean validateTheUser(ResourceResolver resourceResolver, ResourceResolver requestResourceResolver,
            String pagePath);

    /**
     * Validates if logged in user has the passed in access control tags.
     * 
     * @param requestResourceResolver Request resource resolver.
     * @param accessControlTags       List of access control tags to be checked
     *                                against.
     * @return True if logged in user has given access control tags, else false.
     */
    boolean checkLoggedInUserHasAccessControlTags(ResourceResolver requestResourceResolver,
            List<String> accessControlTags);
}
