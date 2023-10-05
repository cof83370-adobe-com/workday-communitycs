package com.workday.community.aem.core.exceptions;

/**
 * Exception is thrown for any error reading json file from DAM.
 */
public class DamException extends Exception {
  public DamException() {
    super();
  }

  public DamException(String message) {
    super(message);
  }
}
