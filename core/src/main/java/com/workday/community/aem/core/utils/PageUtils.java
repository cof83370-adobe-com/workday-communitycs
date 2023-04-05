package com.workday.community.aem.core.utils;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.ValueMap;

/**
 * The Class PageUtils.
 *  @author uttej.vardineni
 */
public class PageUtils {

    /**
     * Gets the page property.
     *
     * @param page the page
     * @param pagePath the page path
     * @param property the property
     * @return the page property
     */
    public static String getPageProperty(Page page, String pagePath, String property) {
        if (page != null && page.hasContent()) {
            ValueMap properties = page.getContentResource().adaptTo(ValueMap.class);
             return properties.get(property, String.class);
        }
        return pagePath;
    }
}