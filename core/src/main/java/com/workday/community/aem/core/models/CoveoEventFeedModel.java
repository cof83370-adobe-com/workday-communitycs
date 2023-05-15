package com.workday.community.aem.core.models;

import java.util.Map;

/**
 * The Sling model for Coveo Event Feed component
 */
public interface CoveoEventFeedModel extends CoveoCommonModel {
   /**
    * Get the FeatureEvent details.
    * @return
    */
   Map<String, String> getFeatureEvent();

   String getSortCriteria();

   /**
    * Get Event Criteria
    * @return the Event Criteria
    */
   String getEventCriteria();
}
