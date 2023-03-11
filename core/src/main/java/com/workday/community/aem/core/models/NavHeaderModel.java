package com.workday.community.aem.core.models;

import java.io.IOException;

/**
 * The NavHeaderModel interface.
 */
public interface NavHeaderModel {

    /**
     * Gets the user navigation menu.
     *
     * @return The navigation menu data.
     * @throws IOException
     */
    String getUserNavigationHeaderMenu();
}