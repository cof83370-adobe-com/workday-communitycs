package com.workday.community.aem.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BookPathModel {
    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @RequestAttribute
    private String pagePath;

    /** The resolver. */
    @Inject
    private ResourceResolver resourceResolver;

    /** The Page Title. */
    private String pageTitle;

    @PostConstruct
    protected void init() {
        logger.debug("Initializing BookPathModel ....");
        if (pagePath == null || pagePath.isEmpty())
            return;

        if (resourceResolver == null) {
            logger.error("ResourceResolver is not injected (null) in BookPathModel init method.");
            throw new RuntimeException();
        }
        Resource res = resourceResolver.getResource(pagePath + "/jcr:content");
        if (res != null) {
            ValueMap properties = res.adaptTo(ValueMap.class);
            pageTitle = properties.get("jcr:title", String.class);
            if (pageTitle == null) {
                pageTitle = pagePath;
            }
        }
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }
}
