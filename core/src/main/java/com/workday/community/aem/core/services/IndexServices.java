package com.workday.community.aem.core.services;

import java.util.List;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Defines an interface for indexing.
 */
@ProviderType
public interface IndexServices {

  /**
   * Create Index jobs for the page, and its child pages.
   *
   * @param paths the page paths.
   */
  void indexPages(List<String> paths);

}
