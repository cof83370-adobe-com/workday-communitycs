package com.workday.community.aem.core.models;

import java.util.List;

/**
 * The Interface TaxonomyBadge.
 */
public interface TaxonomyBadge {

    /**
     * Gets the badge list.
     *
     * @return the badge list
     */
    List<String> getBadgeList();

    /**
     * Gets the retired.
     *
     * @return the retired
     */
    boolean getRetired();
    
}