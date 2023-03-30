package com.workday.community.aem.core.models;

import java.util.List;

public interface CoveoStatusModel {

    /**
     * Gets the total number of pages.
     *
     * @return The number of pages.
     */
    long getTotalPages();

    /**
     * Gets the indexed number of pages.
     *
     * @return The number of indexed pages.
     */
    long getIndexedPages();

    /**
     * Gets the indexed percentage.
     *
     * @return The number of indexed pages.
     */
    float getPercentage();

    /**
     * Gets the coveo server status.
     *
     * @return Cover server has error or not.
     */

    /**
     * Gets the page templates.
     *
     * @return List of templates.
     */
    List<String> getTemplates();

    /**
     * Get server status.
     *
     * @return server status.
     */
    boolean getServerHasError();

    /**
     * Check Coveo enabled or not.
     *
     * @return Coveo indexing status.
     */
    boolean isCoveoEnabled();
}
