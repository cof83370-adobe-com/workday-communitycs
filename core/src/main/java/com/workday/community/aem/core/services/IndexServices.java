package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface IndexServices {

    /**
     * Returns Coveo indexing enabled oe not.
     *
     * @return Coveo enabled.
     */
    boolean isCoveoEnabled();

    /**
     * Create Index jobs for the page, and it's child pages.
     *
     * @param paths
     */
    void indexPages(List<String> paths);
}
