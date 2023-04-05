package com.workday.community.aem.core.utils;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PageUtils.
 * 
 * @author uttej.vardineni
 */
public class PageUtils {
    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(PageUtils.class);

    /**
     * Gets the page property.
     *
     * @param page     the page
     * @param pagePath the page path
     * @param property the property
     * @return the page property
     */
    public static String getPageProperty(Page page, String pagePath, String property) {
        String pageProperty = null;
        if (page != null && page.hasContent()) {
            ValueMap properties = page.getContentResource().adaptTo(ValueMap.class);
            pageProperty = properties.get(property, String.class);
        } else {
            logger.error("\n Error occured while getting Page object - {} ", pagePath);
        }
        return pageProperty;
    }
}