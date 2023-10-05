package com.workday.community.aem.core.services;

import java.util.List;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface IndexServices {

    /**
     * Create Index jobs for the page, and it's child pages.
     *
     * @param paths the page paths.
     */
    void indexPages(List<String> paths);
}
