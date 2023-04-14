package com.workday.community.aem.core.services;

import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;

public interface BookOperationsService {
    public Set<String> processBookPaths(SlingHttpServletRequest req);
}
