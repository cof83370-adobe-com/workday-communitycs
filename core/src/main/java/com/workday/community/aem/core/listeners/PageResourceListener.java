package com.workday.community.aem.core.listeners;

import static com.workday.community.aem.core.constants.GlobalConstants.ADMIN_SERVICE_USER;
import static com.workday.community.aem.core.constants.GlobalConstants.PROP_AUTHOR;
import static com.workday.community.aem.core.constants.GlobalConstants.PROP_USER_NAME;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.QueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.JcrConstants;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

/**
 * Listens for content changes in the Community content root.
 */
@Slf4j
@Component(service = ResourceChangeListener.class, immediate = true, property = {
    ResourceChangeListener.PATHS + "=" + GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH,
    ResourceChangeListener.CHANGES + "=" + "REMOVED", ResourceChangeListener.CHANGES + "=" + "ADDED", })
@ServiceDescription("PageResourceListener")
public class PageResourceListener implements ResourceChangeListener {

  /**
   * Access control ID for Workday users.
   */
  private static final String TAG_INTERNAL_WORKMATE = "access-control:internal_workmates";

  /**
   * The cache manager.
   */
  @Reference
  private CacheManagerService cacheManager;

  /**
   * The query service.
   */
  @Reference
  private QueryService queryService;

  /**
   * The drupal service.
   */
  @Reference
  private DrupalService drupalService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void onChange(List<ResourceChange> changes) {
    if (changes.size() == 1 && changes.get(0).getType().equals(ResourceChange.ChangeType.REMOVED)) {
      removeBookNodes(changes.get(0).getPath());
    }

    changes.stream()
        .filter(item -> item.getType().equals(ResourceChange.ChangeType.ADDED)
            && item.getPath().endsWith(GlobalConstants.JCR_CONTENT_PATH))
        .forEach(change -> handleNewPage(change.getPath()));
  }

  /**
   * Handles New Page.
   *
   * @param pagePath The path to the resource.
   */
  private void handleNewPage(String pagePath) {
    try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      if (resourceResolver.getResource(pagePath) != null) {
        addInternalWorkmatesTag(pagePath, resourceResolver);
        addAuthorPropertyToContentNode(pagePath, resourceResolver);
      }
      if (resourceResolver.hasChanges()) {
        resourceResolver.commit();
      }
    } catch (PersistenceException | CacheException e) {
      log.error("Exception occurred when adding author property to page {} ", e.getMessage());
    }
  }

  /**
   * Adds the Internal Workmates tags to page.
   *
   * @param pagePath         The path to the resource.
   * @param resourceResolver A resource resolver object.
   */
  public void addInternalWorkmatesTag(String pagePath, ResourceResolver resourceResolver) {
    PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
    Page page = Objects.requireNonNull(pageManager).getContainingPage(pagePath);
    if (page == null) {
      return;
    }

    Node pageNode = page.getContentResource().adaptTo(Node.class);

    String[] aclTags = Optional
        .ofNullable(page.getProperties().get(GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, String[].class))
        .orElse(new String[0]);
    List<String> aclTagList = new ArrayList<>(Arrays.asList(aclTags));

    if (pageNode != null && !aclTagList.contains(TAG_INTERNAL_WORKMATE)) {
      aclTagList.add(TAG_INTERNAL_WORKMATE);
      try {
        pageNode.setProperty(GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, aclTagList.toArray(String[]::new));
      } catch (RepositoryException exception) {
        log.error("Exception occurred when adding Internal Workmates Tag property to page {} ", exception.getMessage());
      }
    }
  }

  /**
   * Adds the author property to a content node.
   *
   * @param path             The path to the resource.
   * @param resourceResolver A resource resolver object.
   */
  public void addAuthorPropertyToContentNode(String path, ResourceResolver resourceResolver) {
    if (resourceResolver.getResource(path) != null) {
      try {
        Node root = resourceResolver.getResource(path).adaptTo(Node.class);
        if (root != null) {

          String createdUserId = root.getProperty(JcrConstants.JCR_CREATED_BY).getString();
          root.setProperty(PROP_AUTHOR, createdUserId);

          JsonObject jsonObject = drupalService.searchOurmUserList(createdUserId);

          if (jsonObject != null && !jsonObject.isJsonNull()) {
            final String userName = getUserName(jsonObject);
            if (StringUtils.isNotBlank(userName)) {
              root.setProperty(PROP_USER_NAME, userName);
            }
          }
        }
      } catch (RepositoryException | DrupalException ex) {
        log.error("Exception occurred when adding author property to page {} ", ex);
      }
    }
  }

  /**
   * Gets the user name.
   *
   * @param jsonObject the json object
   * @return the user name
   */
  private String getUserName(JsonObject jsonObject) {
    JsonArray usersArray = jsonObject.getAsJsonArray("users");
    if (usersArray.size() > 0) {
      JsonObject userObject = usersArray.get(0).getAsJsonObject();
      return userObject.get("username").getAsString();
    }
    return StringUtils.EMPTY;
  }

  /**
   * Removes the book nodes.
   *
   * @param pagePath The path to the resource.
   */
  private void removeBookNodes(String pagePath) {
    try (ResourceResolver resolver = cacheManager.getServiceResolver(ADMIN_SERVICE_USER)) {
      if (!pagePath.contains(GlobalConstants.JCR_CONTENT_PATH)) {
        List<String> paths = queryService.getBookNodesByPath(pagePath, null);
        for (String path : paths) {
          if (resolver.getResource(path) != null) {
            Node root = Objects.requireNonNull(resolver.getResource(path)).adaptTo(Node.class);
            if (root != null) {
              root.remove();
            }
          }
        }
        if (resolver.hasChanges()) {
          resolver.commit();
          log.debug("Removed node for page {}", pagePath);
        }
      }
    } catch (PersistenceException | RepositoryException | CacheException e) {
      log.error("Can't remove found nodes for page {}", pagePath);
    }
  }
}