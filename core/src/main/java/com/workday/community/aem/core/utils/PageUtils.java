package com.workday.community.aem.core.utils;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.ValueMap;

/**
 * The Class PageUtils.
 *  @author uttej.vardineni
 */
public class PageUtils {

    /**
     * Gets the page property.
     *
     * @param pm the pm
     * @param pagePath the page path
     * @param property the property
     * @return the page property
     */
    public static String getPageProperty(PageManager pm, String pagePath, String property) {
        Page page = pm.getPage(pagePath);
        if (page != null && page.hasContent()) {
            ValueMap properties = pm.getPage(pagePath).getContentResource().adaptTo(ValueMap.class);
             return properties.get(property, String.class);
        }
        return pagePath;
    }
}