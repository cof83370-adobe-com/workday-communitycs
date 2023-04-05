package com.workday.community.aem.core.models;

import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.utils.PageUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BookModel {
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
        if (StringUtils.isEmpty(pagePath))
            return;

        PageManager pm = resourceResolver.adaptTo(PageManager.class);
        pageTitle = PageUtils.getPageProperty(pm, pagePath, "jcr:title");
    }

    public String getPageTitle() {
        return pageTitle;
    }
}
