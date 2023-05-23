package com.workday.community.aem.core.models;

import javax.jcr.RepositoryException;
import java.util.Map;

/**
 * The Sling model for Coveo Event Feed component
 */
public interface CoveoEventFeedModel extends CoveoCommonModel {
   /**
    * Get the FeatureEvent details.
    * @return FeatureEvent details as a Map object.
    */
   Map<String, String> getFeatureEvent() throws RepositoryException;

   String getSortCriteria();

   /**
    * Get Event Criteria
    * @return the Event Criteria
    */
   String getEventCriteria();

   /**
    * Get all events Url.
    * @return all events Url.
    */
   String getAllEventsUrl();
}
