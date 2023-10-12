package com.workday.community.aem.core.pojos;

import lombok.Getter;
import lombok.Setter;

/**
 * Class for wrapping a single event type.
 */
@Getter
@Setter
public class EventTypeValue {

  private String value;

  private String lookupValue;

  private int numberOfResults;

  public EventTypeValue() {
  }

  public EventTypeValue(String value, String lookupValue) {
    this.value = value;
    this.lookupValue = lookupValue;
  }

}
