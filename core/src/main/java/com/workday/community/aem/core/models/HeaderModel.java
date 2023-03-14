package com.workday.community.aem.core.models;

import java.io.IOException;

/**
 * The NavHeaderModel interface.
 */
public interface HeaderModel {

    /**
     * Gets the user navigation menu.
     *
     * @return The navigation menu data.
     * @throws IOException
     */
    String getUserHeaderMenus();

    String getUserAvatarUrl();
}