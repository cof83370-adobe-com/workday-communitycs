package com.workday.community.aem.core.models;

import com.google.gson.JsonArray;
import com.workday.community.aem.core.exceptions.DamException;
import java.util.List;

/**
 * Interface for tabular list view model.
 */
public interface TabularListViewModel extends CoveoCommonModel {
  /**
   * Feed tabs.
   *
   * @return The list of feed tabs
   */
  List<FeedTabModel> getSearches();

  /**
   * Get field names.
   *
   * @return fields as JasonArray object
   */
  JsonArray getFields() throws DamException;

  /**
   * Get selected fields.
   *
   * @return Selected fields JasonArray object
   */
  JsonArray getSelectedFields() throws DamException;

  /**
   * Get all Url base for each feed field.
   *
   * @return all Url base for each feed field
   */
  String getFeedUrlBase() throws DamException;
}