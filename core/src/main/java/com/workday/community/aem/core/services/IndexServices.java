package com.workday.community.aem.core.services;

import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface IndexServices {

    /**
     * Create Index jobs for the page, and it's child pages.
     *
     * @param paths the page paths.
     */
    void indexPages(List<String> paths);
}
