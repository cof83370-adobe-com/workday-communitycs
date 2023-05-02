package com.workday.community.aem.core.services;

import java.util.Set;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * The Interface BookOperationsService.
 */
public interface BookOperationsService {
    
    /**
     * Process book paths.
     *
     * @param resolver the resolver
     * @param bookResourcePath the book resource path
     * @param bookRequestJsonStr the book request json str
     * @return the sets the
     */
    Set<String> processBookPaths(ResourceResolver resolver, String bookResourcePath, String bookRequestJsonStr);
}
