package com.workday.community.aem.core.services;

import com.workday.community.aem.core.exceptions.CacheException;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;

/**
 * The UserService interface.
 */
public interface UserService {
  /**
   * @param request The sling request object.
   * @return the current logged-in user.
   */
  User getCurrentUser(SlingHttpServletRequest request) throws CacheException;

  /**
   * Get user.
   *
   * @param serviceUserId The service user id
   * @param userId        The user id
   * @return The user
   */
  User getUser(String serviceUserId, String userId) throws CacheException;

  /**
   * @param sfId the user's sf id.
   * @return The user's UUID.
   */
  String getUserUUID(String sfId);

  /**
   * Delete the current user.
   *
   * @param request The current sling request object.
   * @param isPath  Is the user path
   */
  void invalidCurrentUser(SlingHttpServletRequest request, boolean isPath) throws CacheException;
}
