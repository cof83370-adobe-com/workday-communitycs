package com.workday.community.aem.core.models;

import com.day.cq.wcm.api.Page;
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

/**
 * The Class BookModel.
 * 
 * @author uttej.vardineni
 */
@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class BookModel {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The page path. */
    @RequestAttribute
    private String pagePath;

    /** The resource resolver. */
    @Inject
    private ResourceResolver resourceResolver;

    /** The page title. */
    private String pageTitle;

    /**
     * Inits the BookModel.
     */
    @PostConstruct
    protected void init() {
        logger.debug("Initializing BookModel ....");
        if (StringUtils.isEmpty(pagePath))
            return;

        PageManager pm = resourceResolver.adaptTo(PageManager.class);
        Page page = pm.getPage(pagePath);

        pageTitle = PageUtils.getPageProperty(page, "jcr:title");

        if (pageTitle == null) {
            pageTitle = pagePath;
        }

    }

    /**
     * Gets the page title.
     *
     * @return the page title
     */
    public String getPageTitle() {
        return pageTitle;
    }
}