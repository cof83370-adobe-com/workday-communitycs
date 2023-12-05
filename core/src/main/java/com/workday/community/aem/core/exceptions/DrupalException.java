package com.workday.community.aem.core.exceptions;

/**
 * Exception is thrown for any error calling drupal Rest APIs.
 */
public class DrupalException extends Exception {
  private static final String PREFIX = "Drupal exception: ";

  public DrupalException(String message) {
    super(message);
  }

  public DrupalException(String message, Object... rest) {
    super(PREFIX + String.format(message, rest));
  }
}
