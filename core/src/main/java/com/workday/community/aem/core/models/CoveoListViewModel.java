package com.workday.community.aem.core.models;

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

}
