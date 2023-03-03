package com.workday.community.aem.core.utils;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import java.util.HashMap;
import java.util.Map;

public class ResolverUtil {

    /** The service user. */
    public static final String SERVICE_USER = "queryserviceuser";
    
    /**
     * Resource resolver.
     * 
     * @param  resourceResolverFactory resourceResolverFactory.
     * @return New resource resolver for query service user. 
     * @throws LoginException If there is problem.
     */
    public static ResourceResolver newResolver(ResourceResolverFactory resourceResolverFactory) throws LoginException {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(ResourceResolverFactory.SUBSERVICE, SERVICE_USER );

        // Fetches the service resolver using service user.
        ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(paramMap);
        return resolver;
    }

}
