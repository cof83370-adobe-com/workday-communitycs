package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface IndexServices {
    /** The Constant index BATCH_SIZE. */
    public static final Integer BATCH_SIZE = 20;

    /**
     * Create Index jobs for the page, and it's child pages.
     *
     * @param paths
     */
    void indexPages(List<String> paths);
}
