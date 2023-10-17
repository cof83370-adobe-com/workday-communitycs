package com.workday.community.aem.core.exceptions;

/**
 * Thrown when there is error making a request to the LMS service.
 */
public class LmsException extends Exception {

  public LmsException() {
    super();
  }

  public LmsException(String message) {
    super(message);
  }

}
