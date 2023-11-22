package com.workday.community.aem.core.services.impl;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CREATED;
import static com.day.cq.commons.jcr.JcrConstants.JCR_TITLE;
import static com.day.cq.wcm.api.constants.NameConstants.PN_PAGE_LAST_MOD_BY;
import static com.workday.community.aem.core.constants.GlobalConstants.JCR_CONTENT_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static com.workday.community.aem.core.constants.WccConstants.WORKDAY_PUBLIC_PAGE_PATH;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import com.workday.community.aem.core.services.RunModeConfigService;
<<<<<<< Updated upstream
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
=======
import com.workday.community.aem.core.utils.DamUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
>>>>>>> Stashed changes
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class ExtractPagePropertiesServiceImpl.
 */
@Slf4j
@Component(service = ExtractPagePropertiesService.class, immediate = true)
public class ExtractPagePropertiesServiceImpl implements ExtractPagePropertiesService {

  /**
   * The TEXT_COMPONENT.
   */
  protected static final String TEXT_COMPONENT = "workday-community/components/core/text";

  /**
   * The IDENTITY_TYPE_GROUP.
   */
  private static final String IDENTITY_TYPE_GROUP = "GROUP";

  /**
   * The IDENTITY_TYPE_USER.
   */
  private static final String IDENTITY_TYPE_USER = "USER";

  /**
   * The page tags.
   */
  private static final Map<String, String> pageTagMap = new HashMap<>() {
    {
      put("product", "productTags");
      put("using-workday", "usingWorkdayTags");
      put("programs-and-tools", "programsToolsTags");
      put("release", "releaseTags");
      put("industry", "industryTags");
      put("user", "userTags");
      put("region-and-country", "regionCountryTags");
      put("training", "trainingTags");
      put("release-notes", "releaseNotesTags");
      put("event", "eventTags");
      put("content-types", "contentType");
    }
  };

  /**
   * The custom components.
   */
  private static final Map<String, String> customComponents =
      Map.of("root/container/eventregistration/button", "registrationLink");

  /**
   * The SECURITY_IDENTITY_PROVIDER.
   */
  private static final String SECURITY_IDENTITY_PROVIDER = "Community_Secured_Identity_Provider";

  /**
   * The EXCLUDE.
   */
  private static final String EXCLUDE = "exclude";

  /**
   * The drupal role mapping.
   *
   * @see <a href="https://docs.google.com/spreadsheets/d/1h0aPEBm-513U1p8taSSJD4MgaxAdZ5j7MtMGIA4IoV8/edit#gid=625583643">AEM and drupal role mapping doc</a>
   */
  private static final Map<String, String> DRUPAL_ROLE_MAPPING = new HashMap<>() {
    {
      put("access-control:authenticated", "authenticated");
      put("access-control:customer_all", "customer");
      put("access-control:customer_named_support_contact", "customer_named_support_contact");
      put("access-control:customer_training_coordinator", "customer_training_coordinator");
      put("access-control:customer_touchpoint_pro", "customer_touchpoint_pro");
      put("access-control:customer_workday_pro", "customer_workday_pro");
      put("access-control:customer_adaptive", "customer_adaptive");
      put("access-control:customer_peakon", "customer_peakon");
      put("access-control:customer_scout", "customer_scout");
      put("access-control:customer_vndly", "customer_vndly");
      put("access-control:customer_wsp_accelerate", "customer_wsp_accelerate");
      put("access-control:customer_wsp_accelerate_plus", "customer_wsp_accelerate_plus");
      put("access-control:customer_wsp_enhanced", "customer_wsp_enhanced");
      put("access-control:customer_wsp_guided", "customer_wsp_guided");
      put("access-control:partner_all", "partner_all");
      put("access-control:partner_innovation_track", "partner_innovation_track");
      put("access-control:partner_sales_track", "partner_sales_track");
      put("access-control:partner_services_track", "partner_services_track");
      put("access-control:internal_workmates", "workday");
    }
  };

  /**
   * The taxonomyFields.
   */
  private final List<String> taxonomyFields = new ArrayList<>(
      Arrays.asList("productTags", "usingWorkdayTags", "programsToolsTags", "releaseTags",
          "industryTags", "userTags", "regionCountryTags", "trainingTags", "contentType",
          "eventAudience", "eventFormat")
  );

  /**
   * The dateFields.
   */
  private final List<String> dateFields =
      new ArrayList<>(Arrays.asList("eventStartDate", "eventEndDate", "postedDate", "updatedDate"));

  /**
   * The hierarchyFields.
   */
  private final List<String> hierarchyFields = new ArrayList<>(
      Arrays.asList("productTags", "usingWorkdayTags", "programsToolsTags", "releaseTags",
          "industryTags", "userTags", "regionCountryTags", "trainingTags", "contentType"));

  /**
   * The stringFields.
   */
  private final List<String> stringFields =
      new ArrayList<>(Arrays.asList("pageTitle", "eventHost", "eventLocation", "retirementStatus"));

  /**
   * The cache manager.
   **/
  @Reference
  private CacheManagerService cacheManager;

  /**
   * The run mode config service.
   */
  @Reference
  private RunModeConfigService runModeConfigService;

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> extractPageProperties(String path) {
    Map<String, Object> properties = new HashMap<>();

    try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(READ_SERVICE_USER)) {
      PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
      Page page = null;
      if (pageManager != null) {
        page = pageManager.getPage(path);
      }
      if (page == null) {
        throw new ResourceNotFoundException("Page not found");
      }
      ValueMap data = page.getProperties();
      if (data == null || data.isEmpty()) {
        throw new ResourceNotFoundException("Page data not found");
      }
      TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
      String pageLink =
          runModeConfigService.getPublishInstanceDomain().concat(path).concat(".html");
      properties.put("documentId", pageLink);
      properties.put("aemPageLink", pageLink);
      properties.put("isAem", true);
      processDateFields(data, properties);
      processStringFields(data, properties);
      processCustomComponents(page, properties);

      UserManager userManager = resourceResolver.adaptTo(UserManager.class);
      String email = processUserFields(data, userManager, properties);
      for (String taxonomyField : taxonomyFields) {
        String[] taxonomyIds = data.get(taxonomyField, String[].class);
        if (taxonomyIds != null && taxonomyIds.length > 0) {
          List<String> value = processTaxonomyFields(tagManager, taxonomyIds, taxonomyField);
          properties.put(taxonomyField, value);
          if (hierarchyFields.contains(taxonomyField)) {
            value = processHierarchyTaxonomyFields(tagManager, taxonomyIds, taxonomyField);
            properties.put(taxonomyField + "Hierarchy", value);
          }
          if (taxonomyField.equals("eventAudience") || taxonomyField.equals("eventFormat")) {
            String fieldName = "eventTags";
            value = processHierarchyTaxonomyFields(tagManager, taxonomyIds, fieldName);
            properties.put(fieldName + "Hierarchy", value);
          }
          if (taxonomyField.equals("contentType")) {
            List<String> types = new ArrayList<>();
            value.stream().map(type -> type.replaceAll("\\W", "_").toLowerCase())
                .forEach(types::add);
            properties.put("com_content_type", types);
          }
        }
      }

      processPageTags(page, properties);

      processPermission(data, properties, email, path);

      Resource resource = resourceResolver.getResource(path.concat(JCR_CONTENT_PATH));
      Node node = null;
      if (resource != null) {
        node = resource.adaptTo(Node.class);
      }

      List<String> textList = new ArrayList<>();
      if (node != null) {
        NodeIterator it = node.getNodes();
        processTextComponent(it, textList);
      }

      if (!textList.isEmpty()) {
        String description = String.join(" ", textList);
        properties.put(org.apache.jackrabbit.vault.packaging.JcrPackageDefinition.NAME_DESCRIPTION,
            description);
      }
    } catch (CacheException | RepositoryException | SlingException e) {
      log.error("Extract page properties path: {} message: {}", path, e.getMessage());
      return properties;
    }
    return properties;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processDateFields(ValueMap data, Map<String, Object> properties) {
    for (String dateField : dateFields) {
      GregorianCalendar value = data.get(dateField, GregorianCalendar.class);
      if (value == null && dateField.equals("postedDate")) {
        value = data.get(JCR_CREATED, GregorianCalendar.class);
      }
      if (value != null) {
        long time = value.getTimeInMillis() / 1000;
        properties.put(dateField, time);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processPermission(ValueMap data, Map<String, Object> properties, String email,
                                String path) {
    ArrayList<Object> permissionGroupAllowedPermissions = new ArrayList<>();
    String[] accessControlValues = data
        .get(GlobalConstants.TAG_PROPERTY_ACCESS_CONTROL, String[].class);

    boolean allowAnonymous = false;
    if (accessControlValues != null) {
      for (String accessControlValue : accessControlValues) {
        if (DRUPAL_ROLE_MAPPING.containsKey(accessControlValue)) {
          String[] drupalRoles = DRUPAL_ROLE_MAPPING.get(accessControlValue).split(";");

          for (String drupalRole : drupalRoles) {
            Map<String, Object> permissionGroup = new HashMap<>();
            permissionGroup.put("identity", drupalRole);
            permissionGroup.put("identityType", IDENTITY_TYPE_GROUP);
            permissionGroup.put("securityProvider", SECURITY_IDENTITY_PROVIDER);
            permissionGroupAllowedPermissions.add(permissionGroup);
          }
        } else if (accessControlValue.equals("access-control:unauthenticated")) {
          allowAnonymous = true;
        } else {
          log.info("Coveo indexing: Access control value {} missing in the map for the page {}",
              accessControlValue, properties.get("aemPageLink"));
        }
      }
    }
    if (permissionGroupAllowedPermissions.isEmpty()) {
      // If access control field is empty, we need to pass a value, or else it will
      // get permission error during coveo indexing.
      Map<String, Object> permissionGroup = new HashMap<>();
      permissionGroup.put("identity", EXCLUDE);
      permissionGroup.put("identityType", IDENTITY_TYPE_GROUP);
      permissionGroup.put("securityProvider", SECURITY_IDENTITY_PROVIDER);
      permissionGroupAllowedPermissions.add(permissionGroup);
    }
    if (email != null && !email.trim().isEmpty()) {
      Map<String, Object> permissionGroup = new HashMap<>();
      permissionGroup.put("identity", email);
      permissionGroup.put("identityType", IDENTITY_TYPE_USER);
      permissionGroup.put("securityProvider", SECURITY_IDENTITY_PROVIDER);
      permissionGroupAllowedPermissions.add(permissionGroup);
    }

    Map<String, Object> permissionGroup = new HashMap<>();
    if (!allowAnonymous && path.contains(WORKDAY_PUBLIC_PAGE_PATH)) {
      allowAnonymous = true;
    }
    permissionGroup.put("allowAnonymous", allowAnonymous);

    permissionGroup.put("allowedPermissions", permissionGroupAllowedPermissions);
    ArrayList<Object> permission = new ArrayList<>();
    permission.add(permissionGroup);
    properties.put("permissions", permission);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processStringFields(ValueMap data, Map<String, Object> properties) {
    for (String stringField : stringFields) {
      String value = data.get(stringField, String.class);
      if (stringField.equals("pageTitle") && value == null) {
        value = data.get(JCR_TITLE, String.class);
      } else if (stringField.equals("retirementStatus")) {
        if (StringUtils.isNotBlank(value) && !value.equals("retired")) {
          value = "published";
        }
      }
      if (value != null) {
        properties.put(stringField, value);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> processTaxonomyFields(TagManager tagManager, String[] taxonomyTagIds,
                                                 String taxonomyField) {
    List<String> processedTags = new ArrayList<>();
    for (String tagIdString : taxonomyTagIds) {
      Tag tag = tagManager.resolve(tagIdString);
      if (tag != null) {
        processedTags.add(tag.getTitle());
      }
    }
    return processedTags;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> processHierarchyTaxonomyFields(TagManager tagManager,
                                                          String[] taxonomyTagIds,
                                                          String taxonomyField) {
    List<String> processedTags = new ArrayList<>();
    for (String tagIdString : taxonomyTagIds) {
      int index = tagIdString.indexOf("/");
      List<String> tagIdsList = new ArrayList<>();
      while (index >= 0) {
        tagIdsList.add(tagIdString.substring(0, index));
        index = tagIdString.indexOf("/", index + 1);
      }
      tagIdsList.add(tagIdString);
      List<String> tagString = new ArrayList<>();
      for (String tagId : tagIdsList) {
        Tag tag = tagManager.resolve(tagId);
        if (tag != null) {
          tagString.add(tag.getTitle());
          String tagPath = String.join("|", tagString);
          processedTags.add(tagPath);
        }
      }
    }
    return processedTags;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processTextComponent(NodeIterator it, List<String> textList) {
    while (it.hasNext()) {
      Node childNode = it.nextNode();
      try {
        if (childNode.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
          String resourceType =
              childNode.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getValue().getString();
          if (resourceType.equals(TEXT_COMPONENT)) {
            String text = childNode.getProperty("text").getValue().getString();
            textList.add(text);
          }
        }
        NodeIterator childIt = childNode.getNodes();
        if (childIt != null) {
          processTextComponent(childIt, textList);
        }
      } catch (RepositoryException e) {
        log.error("Iterator page jcr:content failed: {}", e.getMessage());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String processUserFields(ValueMap data, UserManager userManager,
                                  Map<String, Object> properties) {
    String userName = data.get(PN_PAGE_LAST_MOD_BY, String.class);
    String email = "";
    if (userName != null) {
      properties.put("author", userName);
      try {
        User user = (User) userManager.getAuthorizable(userName);
        email = (user != null && user.getProperty("./profile/email") != null)
            ? Objects.requireNonNull(user.getProperty("./profile/email"))[0].getString() : null;
        properties.put("authorLink",
                runModeConfigService.getDrupalInstanceDomain().concat("/profile/").concat(this.hashEmail(email)));
      } catch (RepositoryException e) {
        log.error("Extract user email and contact number failed: {}", e.getMessage());
        return email;
      }
    }
    return email;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processCustomComponents(Page page, Map<String, Object> properties) {
    for (Map.Entry<String, String> component : customComponents.entrySet()) {
      Resource res = page.getContentResource(component.getKey());
      if (res != null) {
        String url = (String) res.getValueMap().get("linkURL");
        if (url != null) {
          properties.put(component.getValue(), url);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processPageTags(Page page, Map<String, Object> properties) {
    Tag[] tags = page.getTags();
    for (Tag tag : tags) {
      String namespace = tag.getNamespace().getName();
      String field = pageTagMap.get(namespace);
      Object tagList;
      if (pageTagMap.get(namespace) != null) {
        tagList = properties.get(field);
        List<String> tagNameList = new ArrayList<>();
        tagNameList.add(tag.getTitle());
        if (tagList instanceof Collection) {
          tagNameList.addAll((Collection<? extends String>) tagList);
        }
        properties.put(field, tagNameList);

        if (hierarchyFields.contains(field)) {
          List<String> tagPaths = new ArrayList<>();
          while (tag != null && !tag.isNamespace()) {
            Tag finalTag = tag;
            tagPaths.replaceAll(path -> finalTag.getTitle() + "|" + path);
            tagPaths.add(tag.getTitle());
            tag = tag.getParent();
          }

          tagList = properties.get(field + "Hierarchy");
          if (tagList instanceof Collection) {
            tagPaths.addAll((Collection<? extends String>) tagList);
          }
          properties.put(field + "Hierarchy", tagPaths);
        }

      }
    }

  }

  /**
   * Returns hash value for email.
   *
   * @param email email of the author.
   * @return Hashed email.
   */
  protected String hashEmail(String email) {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      return "";
    }
    byte[] hash = digest.digest(email.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(hash);
  }

}
