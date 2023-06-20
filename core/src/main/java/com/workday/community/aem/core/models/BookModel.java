package com.workday.community.aem.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;

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

    /** The Page Object */
    private Page bookPage;

    /** The rootPath. */
    protected static final String rootPath = String.format("%s%s", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH, "/");

    /**
     * Inits the BookModel.
     */
    @PostConstruct
    protected void init() {
        logger.debug("Initializing BookModel ....");
    }

    /**
     * Gets the book page object.
     *
     * @return the page
     */
    public Page getBookPage() {
        PageManager pm = resourceResolver.adaptTo(PageManager.class);
        return pm.getPage(pagePath);
    }

    /**
     * Gets the root path
     *
     * @return the String
     */
    public String getRootPath() {
        return rootPath;
    }
}