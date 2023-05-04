package com.workday.community.aem.core.services;

import java.util.List;
import java.util.Map;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * The UserService interface.
 */
public interface UserService {

	/**
	 * Get user.
	 * 
	 * @param userId The user id
	 * @return The user
	 */
	User getUser(String userId);

	/**
	 * Get user.
	 *
	 * @param userId The user id
	 * @param resourceResolver
	 *
	 * @return The user
	 */
	User getUser(String userId, ResourceResolver resourceResolver);

    /**
	 * Update user.
	 *
	 * @param userId The user id
     * @param fields The fields need update
	 * @param fields The user groups
	 */
	void updateUser(String userId, Map<String, String> fields, List<String> groups);

    /**
	 * Delete user.
	 *
	 * @param userId The user id
	 */
    void deleteUser(String userId);
    
}
