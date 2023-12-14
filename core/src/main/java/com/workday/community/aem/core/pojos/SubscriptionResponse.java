package com.workday.community.aem.core.pojos;

import lombok.Getter;

/**
 * The subscription response object.
 */
@Getter
public class SubscriptionResponse {
  private final boolean subscribed;

  public SubscriptionResponse(boolean subscribed) {
    this.subscribed = subscribed;
  }
}
