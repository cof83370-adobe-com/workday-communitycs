package com.workday.community.aem.core.exceptions;

/**
 * Thrown when there is an error with the cache manager.
 */
public class CacheException extends Exception {
  private static final String PREFIX = "Cache access exception: ";

  public CacheException() {
    super();
  }

  public CacheException(String msg) {
    super(msg);
  }

  public CacheException(String message, Object... rest) {
    super(PREFIX + String.format(message, rest));
  }
}
