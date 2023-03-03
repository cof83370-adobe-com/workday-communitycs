package com.workday.community.aem.core.models;

public interface CoveoModel {

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
     * @return coveo server status.
     */
    boolean getServerStatus();
}
