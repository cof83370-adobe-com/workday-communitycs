package com.workday.community.aem.core.exceptions;

/**
 * Exception is thrown for any error reading json file from DAM.
 */
public class DamException extends Exception {
  private static final String PREFIX = "DAM access exception: ";

  public DamException(String message) {
    super(message);
  }

  public DamException(String message, Object... rest) {
    super(PREFIX + String.format(message, rest));
  }
}
