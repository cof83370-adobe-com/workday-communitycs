package com.workday.community.aem.core.exceptions;

/**
 * Thrown when there is an issue making an API call.
 */
public class APIException extends Exception {

  public APIException() {
    super();
  }

  public APIException(String message) {
    super(message);
  }

}
