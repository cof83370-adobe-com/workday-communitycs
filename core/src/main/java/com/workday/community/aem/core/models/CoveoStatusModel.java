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

    boolean getServerStatus();

    /**
     * Gets the page templates.
     *
     * @return List of templates.
     */
    List<String> getTemplates();

    boolean getServerHasError();
}
