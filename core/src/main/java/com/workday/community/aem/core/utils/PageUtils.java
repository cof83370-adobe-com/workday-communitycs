package com.workday.community.aem.core.utils;

import static com.workday.community.aem.core.constants.GlobalConstants.PUBLISH;
import static java.util.Objects.requireNonNull;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.RunModeConfigService;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 * The Utility class for all Page related Utilities.
 */
@Slf4j
public class PageUtils {

  /**
   * Get the Tags Title list attached to the page.
   *
   * @param pagePath         The Requested page path.
   * @param resourceResolver The Resource Resolver Object.
   * @return tagTitlesList.
   * @throws RepositoryException the repository exception
   */
  public static List<String> getPageTagsTitleList(String pagePath,
                                                  ResourceResolver resourceResolver)
      throws RepositoryException {
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
    } else {
      log.debug("Page doesn't exist under the path {}", pagePath);
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
   * @throws RepositoryException the repository exception
   */
  public static List<String> getPageTagPropertyList(ResourceResolver resourceResolver,
                                                    String pagePath, String tagName,
                                                    String pagePropertyName)
      throws RepositoryException {
    final List<String> accessControlList = new ArrayList<>();
    Session session = resourceResolver.adaptTo(Session.class);
    if (requireNonNull(session).itemExists(pagePath)) {
      PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
      Page pageObject = requireNonNull(pageManager).getPage(pagePath);
      if (null != pageObject) {
        ValueMap data = pageObject.getProperties();
        String[] accessControlTags = data.get(pagePropertyName, String[].class);
        if (accessControlTags != null && accessControlTags.length != 0) {
          for (String tagIdString : requireNonNull(accessControlTags)) {
            accessControlList.add(tagIdString.replace(tagName.concat(":"), ""));
          }
        }
      }
    } else {
      log.debug("Page doesn't exist under the path {}.", pagePath);
    }
    return accessControlList;
  }

  /**
   * Checks if is publish instance.
   *
   * @param runModeConfigService the run mode config service
   * @return true, if is publish instance
   */
  public static boolean isPublishInstance(RunModeConfigService runModeConfigService) {
    final String instance = runModeConfigService.getInstance();
    return StringUtils.isNotBlank(instance) && instance.equals(PUBLISH);
  }

  /**
   * Append extension.
   *
   * @param pagePath the page path
   * @return the string
   */
  public static String appendExtension(String pagePath) {
    if (StringUtils.isNotBlank(pagePath)
        && pagePath.startsWith(String.format("%s%s", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH, "/"))) {
      return String.format("%s%s", pagePath, GlobalConstants.HTML_EXTENSION);
    }
    return pagePath;
  }

  /**
   * Gets the book page title.
   *
   * @param resourceResolver the resource resolver
   * @param pagePath         the page path
   * @return the book page title
   */
  public static String getPageTitleFromPath(ResourceResolver resourceResolver, final String pagePath) {
    if (StringUtils.isNotBlank(pagePath) && pagePath.startsWith(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH)) {
      PageManager pm = resourceResolver.adaptTo(PageManager.class);
      Page page = pm.getPage(pagePath);
      return page.getTitle();
    }
    return StringUtils.EMPTY;
  }
}