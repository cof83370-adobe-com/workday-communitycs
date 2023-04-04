package com.workday.community.aem.core.services;

import java.util.List;
import java.util.Map;

public interface UserService {

    /**
	 * Update user.
	 *
	 * @param userId The user id
	 * @param fields The user fields
	 * @param fields The user groups
	 */
	void createUser(String userId, Map<String, String> fields, List<String> groups);

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
