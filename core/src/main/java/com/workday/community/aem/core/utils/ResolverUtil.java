package com.workday.community.aem.core.utils;

import java.util.HashMap;
import java.util.Map;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

/**
 * ResolverUtil class.
 */
public class ResolverUtil {

    /**
     * Resource resolver.
     *
     * @param resourceResolverFactory resourceResolverFactory.
     * @return New resource resolver for query service user.
     * @throws LoginException If there is problem.
     */
    public static ResourceResolver newResolver(ResourceResolverFactory resourceResolverFactory, String serviceUser)
            throws LoginException {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(ResourceResolverFactory.SUBSERVICE, serviceUser);

        // Fetches the service resolver using service user.
        return resourceResolverFactory.getServiceResourceResolver(paramMap);
    }

}
