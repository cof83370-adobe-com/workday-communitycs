package com.workday.community.aem.core.exceptions;

/**
 * Thrown when there is error making a request to Snap logic.
 */
public class SnapException extends Exception {

  public SnapException() {
    super();
  }

  public SnapException(String message) {
    super(message);
  }

}
