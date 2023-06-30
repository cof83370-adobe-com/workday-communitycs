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
import org.apache.commons.lang.ArrayUtils;
import static java.util.Objects.requireNonNull;

/**
 * The Utility class for all Page related Utilities.
 */
public class PageUtils {

    private  static final Logger LOGGER = LoggerFactory.getLogger(PageUtils.class);

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
            LOGGER.debug("Page doesn't exist under the path {}",pagePath);
        }
        return pageTagsTitlesList;
    }

    /**
     * Get specific tag property attached to the page.
     *
     * @param resourceResolver The Resource Resolver Object.
     * @param pagePath         The Requested page path.
     * @param tagName          The tage name.
     * @param pagePropertyName The page roperty name.
     * @return tagTitlesList.
     */
    public static List<String> getPageTagPropertyList(ResourceResolver resourceResolver, String pagePath, String tagName, String pagePropertyName) throws RepositoryException {
        final List<String> accessControlList = new ArrayList<>();
        Session session = resourceResolver.adaptTo(Session.class);
        if (requireNonNull(session).itemExists(pagePath)) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page pageObject = requireNonNull(pageManager).getPage(pagePath);
            if (null != pageObject) {
                ValueMap data = pageObject.getProperties();
                String[] accessControlTags = data.get(pagePropertyName, String[].class);
                if (!ArrayUtils.isEmpty(accessControlTags)) {
                    for (String tagIdString: requireNonNull(accessControlTags)) {
                        accessControlList.add(tagIdString.replace(tagName.concat(":"), ""));
                    }
                }
            }
        }
        else {
            LOGGER.debug("Page doesn't exist under the path {}.",pagePath);
        }
        return accessControlList;
    }

}
