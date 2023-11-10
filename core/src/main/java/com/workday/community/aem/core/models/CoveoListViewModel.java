package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;
import java.util.List;

/**
 * Interface for coveo list view model.
 */
public interface CoveoListViewModel extends CoveoCommonModel {
  /**
   * Category facet.
   *
   * @return The selected Categories.
   */
  List<CategoryFacetModel> getCategories();

  /**
   * Return Facet Search help text map.
   *
   * @return The map of help text vs facet label.
   */
  JsonObject getHelpTextMap();
}
