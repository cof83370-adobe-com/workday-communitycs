package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;

/**
 * Defines an interface for common Coveo functionality.
 */
public interface CoveoCommonModel {
  /**
   * Get search configuration.
   *
   * @return search configuration as a Json object.
   */
  JsonObject getSearchConfig();

  /**
   * Get extra custom criteria portion.
   *
   * @return Extra custom criteria as string.
   */
  String getExtraCriteria() throws DamException;

}
