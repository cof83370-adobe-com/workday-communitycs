package com.workday.community.aem.core.models;

import java.util.Date;

/**
 * The Interface Metadata.
 */
public interface Metadata {

  /**
   * Gets the user name.
   *
   * @return the user name
   */
  String getUserName();

  /**
   * Gets the posted date.
   *
   * @return the posted date
   */
  Date getPostedDate();

  /**
   * Gets the updated date.
   *
   * @return the updated date
   */
  Date getUpdatedDate();

}
