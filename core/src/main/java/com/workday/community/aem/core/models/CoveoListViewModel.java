package com.workday.community.aem.core.models;
import java.util.List;

/**
 * Interface for coveo list view model.
 */
public interface CoveoListViewModel {
    /**
     * Display tags flag.
     *
     * @return
     */
    boolean getDisplayTags();

    /**
     * Display metadata flag.
     *
     * @return
     */
    boolean getDisplayMetadata();

    /**
     * Category facet.
     * @return
     */
    List<CategoryFacetModel> getCategories();

    /**
     * Returns search hub.
     *
     * @return
     */
    String getSearchHub();

    /**
     * Returns coveo orgid
     *
     * @return
     */
    String getOrgId();
}