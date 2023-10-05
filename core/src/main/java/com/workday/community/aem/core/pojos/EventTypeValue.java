package com.workday.community.aem.core.pojos;

public class EventTypeValue {
  String value;

  String lookupValue;

  int numberOfResults;

  public EventTypeValue() {
  }

  public EventTypeValue(String value, String lookupValue) {
    this.value = value;
    this.lookupValue = lookupValue;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLookupValue() {
    return lookupValue;
  }

  public void setLookupValue(String lookupValue) {
    this.lookupValue = lookupValue;
  }

  public int getNumberOfResults() {
    return numberOfResults;
  }

  public void setNumberOfResults(int numberOfResults) {
    this.numberOfResults = numberOfResults;
  }
}
