package com.workday.community.aem.core.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The sling model for Coveo tab list component.
 */
public interface CoveoTabListModel {
   /**
    * Get search configuration.
    *
    * @return search configuration as a Json object.
    */
   JsonObject getSearchConfig();

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

   /**
    * Get extra custom criteria portion
    * @return Extra custom criteria as string.
    */
   String getExtraCriteria();
}
