package com.workday.community.aem.core.pojos;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Class for wrapping event type responses from Coveo.
 */
@Getter
public class EventTypes {

  private List<EventTypeValue> values = new ArrayList<>();

}
