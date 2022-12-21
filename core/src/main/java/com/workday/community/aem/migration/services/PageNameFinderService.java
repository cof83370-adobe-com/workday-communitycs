package com.workday.community.aem.migration.services;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;

import com.workday.community.aem.migration.models.PageNameBean;

public interface PageNameFinderService {
    public List<PageNameBean> getPageName(ResourceResolver resolver, final String nodeId);
}
