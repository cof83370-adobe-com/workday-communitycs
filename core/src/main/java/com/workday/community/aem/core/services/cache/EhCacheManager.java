package com.workday.community.aem.core.services.cache;

import com.workday.community.aem.core.exceptions.CacheException;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;

/**
 * The EhCache manager definition interface.
 */

@ProviderType
public interface EhCacheManager {
  /**
   * Get a service Resource resolver from Cache.
   * @return the Service resource resolver from Cache.
   * @throws CacheException
   */
  ResourceResolver getServiceResolver() throws CacheException;
}
