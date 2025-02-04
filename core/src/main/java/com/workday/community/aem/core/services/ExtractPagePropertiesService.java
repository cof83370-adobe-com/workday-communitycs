package com.workday.community.aem.core.services;

import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import java.util.List;
import java.util.Map;
import javax.jcr.NodeIterator;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ValueMap;

/**
 * The ExtractPagePropertiesService interface.
 */
public interface ExtractPagePropertiesService {

  /**
   * Get page properties.
   *
   * @param path The page path
   * @return The page properties
   */
  Map<String, Object> extractPageProperties(String path);

  /**
   * Process date fields.
   *
   * @param data       The value map data
   * @param properties The extracted page properties
   */
  void processDateFields(ValueMap data, Map<String, Object> properties);

  /**
   * Process page access permission.
   *
   * @param data       The value map data
   * @param properties The extracted page properties
   * @param email      Page author's email
   * @param path       of the current page
   * @see <a href="https://docs.coveo.com/en/107/cloud-v2-developers/simple-permission-model-definition-examples">Coveo permission example.</a>
   */
  void processPermission(ValueMap data, Map<String, Object> properties, String email,
                         String path);

  /**
   * Process string fields.
   *
   * @param data       The value map data
   * @param properties The extracted page properties
   */
  void processStringFields(ValueMap data, Map<String, Object> properties);

  /**
   * Process taxonomy field.
   *
   * @param tagManager     The tag manager
   * @param taxonomyTagIds Taxonomy ids
   * @param taxonomyField  Taxonomy field name
   * @return The processed taxonomy values
   */
  List<String> processTaxonomyFields(TagManager tagManager, String[] taxonomyTagIds,
                                          String taxonomyField);

  /**
   * Process taxonomy field Hierarchy.
   *
   * @param tagManager     The tag manager
   * @param taxonomyTagIds Taxonomy ids
   * @param taxonomyField  Taxonomy field name
   * @return The processed taxonomy values
   */
  List<String> processHierarchyTaxonomyFields(TagManager tagManager, String[] taxonomyTagIds,
                                              String taxonomyField);

  /**
   * Process text field.
   *
   * @param it       The node iterator
   * @param textlist List of text value
   */
  void processTextComponent(NodeIterator it, List<String> textlist);

  /**
   * Process user fields.
   *
   * @param data        The value map data
   * @param userManager The user manager
   * @param properties  The extracted page properties
   * @return The user's email
   */
  String processUserFields(ValueMap data, UserManager userManager,
                           Map<String, Object> properties);

  /**
   * Process custom components and extract values.
   *
   * @param page       page we are indexing.
   * @param properties Collected values.
   */
  void processCustomComponents(Page page, Map<String, Object> properties);

  /**
   * Process the page tags.
   *
   * @param page       the pass-in page object.
   * @param properties page properties as a map.
   */
  void processPageTags(Page page, Map<String, Object> properties);

}
