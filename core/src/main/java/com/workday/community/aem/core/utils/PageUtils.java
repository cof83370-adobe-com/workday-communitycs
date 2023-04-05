package com.workday.community.aem.core.utils;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.ValueMap;

public class PageUtils {

    public static String getPageProperty(PageManager pm, String pagePath, String property) {
        Page page = pm.getPage(pagePath);
        if (page != null && page.hasContent()) {
            ValueMap properties = pm.getPage(pagePath).getContentResource().adaptTo(ValueMap.class);
             return properties.get(property, String.class);
        }
        return pagePath;
    }
}
