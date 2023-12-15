package com.workday.community.aem.core.services;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.DrupalConfig;
import com.workday.community.aem.core.dto.AemContentDto;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;

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

  /**
   * Returns createOrUpdateEntity API Response as json object.
   *
   * @param aemContentDto AEM Page data bean.
   * @return API Response as json object.
   *
   * @throws DrupalException throes custom exception.
   */
  ApiResponse createOrUpdateEntity(AemContentDto aemContentDto) throws DrupalException;

  /**
   * Returns createOrUpdateEntity API Response as json object.
   *
   * @param pagePath AEM Page url.
   * @return API Response as json object.
   * @throws DrupalException  throes custom exception.
   */
  ApiResponse deleteEntity(String  pagePath) throws DrupalException;

  /**
   * Is AEM to Drupal content sync enabled or not.
   *
   * @return true or false depends on Configurations.
   */
  boolean isContentSyncEnabled();


  /**
   * Check if current user is subscribed to the page or question.
   *
   * @param id  pass-in page id or question id.
   * @param email  the user's email
   * @return true if subscribed, false not.
   */
  boolean isSubscribed(String id, String email) throws DrupalException;

  /**
   * Create user subscription to specific page or question.
   *
   * @param id pass-in page id or question id.
   * @param email the user's email
   *  @return true if subscription is created successfully, false not.
   */
  boolean subscribe(String id, String email) throws DrupalException;

  /**
   * Get Configuration.
   *
   * @return the drupal configuration
   */
  DrupalConfig getConfig();
}
