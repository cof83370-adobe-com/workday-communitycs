package com.workday.community.aem.core.pojos;

import com.adobe.xfa.ut.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * The subscription request object.
 */
@Getter
@Setter
public class SubscriptionRequest {
  private String id;
  private String email;

  public SubscriptionRequest() {
  }

  public SubscriptionRequest(String id, String email) {
    this.id = id;
    this.email = email;
  }

  public boolean isEmpty() {
    return StringUtils.isEmpty(id) || StringUtils.isEmpty(email);
  }
}
