package com.workday.community.aem.core.exceptions;

/**
 * Thrown when there is error making a request to Snap logic.
 */
public class SnapException extends Exception {
  private static final String PREFIX = "Snaplogic api exception: ";

  public SnapException(String message) {
    super(message);
  }

  public SnapException(String message, Object... rest) {
    super(PREFIX + String.format(message, rest));
  }
}
