package com.workday.community.aem.core.models;

import java.util.Map;

/**
 * The Sling model for Coveo Event Feed component.
 */
public interface CoveoEventFeedModel extends CoveoCommonModel {

  /**
   * Get the FeatureEvent details.
   *
   * @return FeatureEvent details as a Map object.
   */
  Map<String, String> getFeatureEvent();

  /**
   * Gets the sort criteria.
   *
   * @return The sort criteria.
   *
   */
  String getSortCriteria();

  /**
   * Get Event Criteria.
   *
   * @return the Event Criteria
   */
  String getEventCriteria();

  /**
   * Get all events Url.
   *
   * @return all events Url.
   */
  String getAllEventsUrl();

}
