package com.workday.community.aem.core.models;

import com.workday.community.aem.core.constants.GlobalConstants;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * The Class RelatedInfoModel.
 * 
 * @author uttej.vardineni
 */
@Model(adaptables = { SlingHttpServletRequest.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RelatedInfoModel {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The rootPath. */
    protected static final String rootPath = String.format("%s%s", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH, "/");

    /**
     * Inits the RelatedInfoModel.
     */
    @PostConstruct
    protected void init() {
        logger.debug("Initializing RelatedInfoModel ....");
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