package com.workday.community.aem.core.pojos;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Speakers.
 */
public class Speakers {
  
  /** The users. */
  final List<SpeakerPojo> users = new ArrayList<>();

  /**
   * Gets the users.
   *
   * @return the users
   */
  public List<SpeakerPojo> getUsers() {
    return users;
  }
}
