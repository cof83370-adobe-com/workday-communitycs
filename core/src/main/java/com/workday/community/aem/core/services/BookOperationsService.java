package com.workday.community.aem.core.services;

import java.util.Set;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Defines an interface for performing Book operations.
 */
public interface BookOperationsService {

  /**
   * Process book paths.
   *
   * @param resolver           The resource resolver service.
   * @param bookResourcePath   The book resource path.
   * @param bookRequestJsonStr the book request JSON string.
   * @return A set of paths that have been removed from the book.
   */
  Set<String> processBookPaths(ResourceResolver resolver, String bookResourcePath,
                               String bookRequestJsonStr);
}
