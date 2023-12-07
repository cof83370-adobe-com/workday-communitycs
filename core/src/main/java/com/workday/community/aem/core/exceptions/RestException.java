package com.workday.community.aem.core.exceptions;

/**
 * Thrown when a Rest API call doesn't return successfully.
 */
public class RestException extends Exception {
  private static final String PREFIX = "Rest api exception: ";

  public RestException(String message, Object... rest) {
    super(PREFIX + String.format(message, rest));
  }
}
