package com.workday.community.aem.core.models;

import com.workday.community.aem.core.exceptions.DamException;
import java.util.Map;
import javax.jcr.RepositoryException;

/**
 * The Sling model for Coveo Event Feed component
 */
public interface CoveoEventFeedModel extends CoveoCommonModel {
   /**
    * Get the FeatureEvent details.
    * @return FeatureEvent details as a Map object.
    */
   Map<String, String> getFeatureEvent() throws RepositoryException;

   String getSortCriteria() throws DamException;

   /**
    * Get Event Criteria
    * @return the Event Criteria
    */
   String getEventCriteria() throws DamException;

   /**
    * Get all events Url.
    * @return all events Url.
    */
   String getAllEventsUrl() throws DamException;
}
