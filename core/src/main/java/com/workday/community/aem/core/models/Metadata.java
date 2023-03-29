package com.workday.community.aem.core.models;

import java.util.Date;

/**
 * The Interface Metadata.
 */
public interface Metadata {

    /**
     * Gets the author name.
     *
     * @return the author name
     */
    String getAuthorName();

    /**
     * Gets the posted date.
     *
     * @return the posted date
     */
    Date getPostedDate();

    /**
     * Gets the updated date.
     *
     * @return the updated date
     */
    Date getUpdatedDate();
}
