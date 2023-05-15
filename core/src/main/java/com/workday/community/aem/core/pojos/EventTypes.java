package com.workday.community.aem.core.pojos;

import com.workday.community.aem.core.pojos.restclient.EventTypeValue;

import java.util.ArrayList;
import java.util.List;

public class EventTypes {
  List<EventTypeValue> values = new ArrayList<>();

  public List<EventTypeValue> getValues() {
    return values;
  }
}
