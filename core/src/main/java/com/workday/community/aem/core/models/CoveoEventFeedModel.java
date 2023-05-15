package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;

/**
 * The Sling model for Coveo Event Feed component
 */
public interface CoveoEventFeedModel extends CoveoCommonModel {
   /**
    * Get the FeatureEvent details.
    * @return
    */
   JsonObject getFeatureEvent();

   String getSortCriteria();

   /**
    * Get Event Criteria
    * @return the Event Criteria
    */
   String getEventCriteria();
}
