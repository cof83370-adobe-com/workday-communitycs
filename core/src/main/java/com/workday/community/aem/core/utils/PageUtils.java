package com.workday.community.aem.core.utils;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * The Utility class for all Page related Utilities.
 */
public class PageUtils {

    private  static final Logger logger = LoggerFactory.getLogger(PageUtils.class);

    /**
     * Get the Tags Title list attached to the page.
     *
     * @param pagePath         The Requested page path.
     * @param resourceResolver The Resource Resolver Object.
     * @return tagTitlesList.
     */
    public static List<String> getPageTagsTitleList(String pagePath, ResourceResolver resourceResolver) throws RepositoryException {
        final List<String> pageTagsTitlesList = new ArrayList<>();
        Session session = resourceResolver.adaptTo(Session.class);
        if (session.itemExists(pagePath)) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page pageObject = pageManager.getPage(pagePath);
            if (null != pageObject) {
                for (Tag tag : pageObject.getTags()) {
                    pageTagsTitlesList.add(tag.getTitle());
                }
            }
        }
        else {
            logger.debug("Page doesn't exist under the path {}",pagePath);
        }
        return pageTagsTitlesList;
    }

    /**
     * Get specific tag property attached to the page.
     *
     * @param pagePath         The Requested page path.
     * @param resourceResolver The Resource Resolver Object.
     * @param tagName          The tage name.
     * @param pagePropertyName The page roperty name.
     * @return tagTitlesList.
     */
    public static List<String> getPageTagPropertyList(String pagePath, ResourceResolver resourceResolver, String tagName, String pagePropertyName) throws RepositoryException {
        final List<String> accessControlList = new ArrayList<>();
        Session session = resourceResolver.adaptTo(Session.class);
        if (session.itemExists(pagePath)) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page pageObject = pageManager.getPage(pagePath);
            if (null != pageObject) {
                ValueMap data = pageObject.getProperties();
                String[] accessControlTags = data.get(pagePropertyName, String[].class);
                for (String tagIdString: accessControlTags) {
                    accessControlList.add(tagIdString.replace(tagName.concat(":"), ""));
                }
            }
        }
        else {
            logger.debug("Page doesn't exist under the path {}.",pagePath);
        }
        return accessControlList;
    }

}
