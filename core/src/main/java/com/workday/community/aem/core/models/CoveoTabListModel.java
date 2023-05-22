package com.workday.community.aem.core.models;

import com.google.gson.JsonArray;

/**
 * The sling model for Coveo tab list component.
 */
public interface CoveoTabListModel extends CoveoCommonModel {
   /**
    * Get field
    * @return fields as JasonArray object.
    */
   JsonArray getFields();


   /**
    * Get selected fields
    * @return Selected fields JasonArray object.
    */
   JsonArray getSelectedFields();

   /**
    * Get product criteria portion.
    * @return product criteria portion as a search criteria string.
    */
   String getProductCriteria();
}
