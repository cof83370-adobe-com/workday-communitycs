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
   * @param resourceResolver the ResourceResolver object.
   * @param userId           The user id
   * @return The user
   */
  User getUser(ResourceResolver resourceResolver, String userId);

  /**
   * Update user.
   *
   * @param userId The user id
   * @param fields The fields need update
   * @param groups The user groups
   */
  void updateUser(String userId, Map<String, String> fields, List<String> groups);

  /**
   * Delete user.
   *
   * @param userParam The user is or user path
   * @param isPath    Is the user path
   */
  void deleteUser(String userParam, boolean isPath);


  /**
   *
   * @param sfId the user's sf id.
   * @return The user's UUID.
   */
  String getUserUUID(String sfId);
}
