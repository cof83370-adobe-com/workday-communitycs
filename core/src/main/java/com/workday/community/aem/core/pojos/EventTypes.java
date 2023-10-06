package com.workday.community.aem.core.pojos;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for wrapping event type responses from Coveo.
 */
public class EventTypes {

  final List<EventTypeValue> values = new ArrayList<>();

  public List<EventTypeValue> getValues() {
    return values;
  }

}
