package com.workday.community.aem.core.models;

import com.google.gson.JsonArray;
import com.workday.community.aem.core.exceptions.DamException;

/**
 * The sling model for Coveo tab list component.
 */
public interface CoveoTabListModel extends CoveoCommonModel {

  /**
   * Get field.
   *
   * @return fields as JasonArray object.
   */
  JsonArray getFields() throws DamException;


  /**
   * Get selected fields.
   *
   * @return Selected fields JasonArray object.
   */
  JsonArray getSelectedFields() throws DamException;

  /**
   * Get product criteria portion.
   *
   * @return product criteria portion as a search criteria string.
   */
  String getProductCriteria();

  /**
   * Get all Url base for each feed field.
   *
   * @return all Url base for each feed field.
   */
  String getFeedUrlBase() throws DamException;
}
