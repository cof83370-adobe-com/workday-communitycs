package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;

public interface CoveoTabListModel {
   /**
    * Get search configuration.
    *
    * @return search configuration as a Json object.
    */
   JsonObject searchConfig();

   /**
    * Get component properties.
    * @return compponent properties as a JSON object.
    */
   // TODO this should come from component editor
   JsonObject compConfig();

   JsonObject fieldCriteria();

   /**
    * Get product criteria portion.
    * @return product criteria portion as a search criteria string.
    */
   String productCriteria();
}
