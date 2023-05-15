package com.workday.community.aem.core.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.CoveoSearchConfig;

/**
 * The sling model for Coveo tab list component.
 */
public interface CoveoTabListModel extends CoveoCommonModel {
   /**
    * Get component properties.
    * @return compponent properties as a JSON object.
    */
   // TODO this should come from component editor
   JsonObject getCompConfig();

   /**
    * Get field
    * @return fields as string.
    */
   JsonArray getFields();

   /**
    * Get product criteria portion.
    * @return product criteria portion as a search criteria string.
    */
   String getProductCriteria();
}
