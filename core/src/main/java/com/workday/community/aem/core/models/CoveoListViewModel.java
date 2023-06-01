package com.workday.community.aem.core.models;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Interface for coveo list view model.
 */
public interface CoveoListViewModel {
    /**
     * Category facet.
     * @return
     */
    List<CategoryFacetModel> getCategories();

    /**
     * Returns search config.
     *
     * @return
     */
    public JsonObject getSearchConfig();
}