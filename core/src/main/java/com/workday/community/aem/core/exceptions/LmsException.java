package com.workday.community.aem.core.exceptions;

/**
 * Thrown when there is error making a request to the LMS service.
 */
public class LmsException extends Exception {
  private static final String PREFIX = "LMS access exception: ";

  public LmsException(String message) {
    super(message);
  }

  public LmsException(String message, Object... rest) {
    super(PREFIX + String.format(message, rest));
  }
}
