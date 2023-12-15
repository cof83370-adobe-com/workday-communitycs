package com.workday.community.aem.core.models;

/**
 * Interface for the subscribe model.
 */
public interface SubscribeModel {
  /**
   * Indicated if subscribe enabled.
   *
   * @return true if it is, false not.
   */
  boolean enabled();

  /**
   * Indicated if subscribe is read only (in author environment).
   *
   * @return true if it is, false not.
   */
  boolean readOnly();
}
