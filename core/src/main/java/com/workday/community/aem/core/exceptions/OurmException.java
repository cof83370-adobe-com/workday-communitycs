package com.workday.community.aem.core.exceptions;

/**
 * Exception is thrown for any error reading json file from DAM.
 */
public class OurmException extends Exception {
  private static final String PREFIX = "Ourm api call exception: ";

  public OurmException(String message) {
    super(message);
  }

  public OurmException(String message, Object... rest) {
    super(PREFIX + String.format(message, rest));
  }
}
