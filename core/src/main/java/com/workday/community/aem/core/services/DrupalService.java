package com.workday.community.aem.core.services;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DrupalException;

/**
 * The Drupal service definition interface.
 */
public interface DrupalService {
  /**
   * Gets user data.
   *
   * @param sfId Salesforce Id.
   * @return The user data as a string for roles and adobe data.
   */
  String getUserData(String sfId);

  /**
   * Gets the user profile image data from drupal user data API.
   *
   * @param sfId SFID
   * @return image data as string
   */
  String getUserProfileImage(String sfId);

  /**
   * Gets the user timezone data from drupal user data API.
   *
   * @param sfId SFID
   * @return timezone as string
   */
  String getUserTimezone(String sfId);

  /**
   * Gets the adobe data to be set on digital data object on frontend.
   *
   * @param sfId        SFID.
   * @param pageTitle   Page title.
   * @param contentType Content type.
   * @return Adobe data.
   */
  String getAdobeDigitalData(String sfId, String pageTitle, String contentType);

  /**
   * Returns the user context as json object.
   *
   * @param sfId SFID.
   * @return User context json object.
   */
  JsonObject getUserContext(String sfId);

  /**
   * Searches the given user email.
   *
   * @param searchText User email.
   * @return User data as json object.
   * @throws DrupalException DrupalException.
   */
  JsonObject searchOurmUserList(String searchText) throws DrupalException;
}
