package com.workday.community.aem.core.exceptions;

/**
 * Exception is thrown for any error reading json file from DAM.
 */
public class OurmException extends Exception {
  public OurmException() {
    super();
  }

  public OurmException(String message) {
    super(message);
  }
}
