package com.workday.community.aem.core.exceptions;

/**
 * Thrown when there is an issue making an API call.
 */
public class ApiException extends Exception {

  public ApiException() {
    super();
  }

  public ApiException(String message) {
    super(message);
  }

}
