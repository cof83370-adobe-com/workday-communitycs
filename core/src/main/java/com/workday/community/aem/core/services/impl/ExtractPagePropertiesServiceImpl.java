package com.workday.community.aem.core.services.impl;

import java.util.*;

import javax.jcr.NodeIterator;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.SlingException;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.services.ExtractPagePropertiesService;
import com.workday.community.aem.core.utils.ResolverUtil;

import static com.adobe.acs.commons.mcp.form.ContainerComponent.JCR_TITLE;
import static com.day.cq.wcm.api.constants.NameConstants.NN_TEMPLATE;
import static com.day.cq.wcm.api.constants.NameConstants.PN_PAGE_LAST_MOD_BY;
import static com.workday.community.aem.core.services.impl.QueryServiceImpl.SERVICE_USER;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;

import static com.workday.community.aem.core.constants.GlobalConstants.CONTENT_TYPE_MAPPING;

/**
 * The Class ExtractPagePropertiesServiceImpl.
 */
@Component(
    service = ExtractPagePropertiesService.class,
    immediate = true
)
public class ExtractPagePropertiesServiceImpl implements ExtractPagePropertiesService {
    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(ExtractPagePropertiesServiceImpl.class);

    /** The resource resolver factory. */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** The externalizer service. */
    @Reference
    private Externalizer externalizer;

    // @Todo Once Tek system unifies the tag format, we can remove duplicate tags here.
    /** The taxonomyFields. */
    private final ArrayList<String> taxonomyFields = new ArrayList<>(
        Arrays.asList("productTags", "usingWorkday", "usingWorkdayTags", "programsTools", "programsToolsTags", "releaseTags", "industryTags", "userTags", "regionCountry", "eventAudience", "eventFormat")
    );

    /** The dateFields. */
    private final ArrayList<String> dateFields = new ArrayList<>(Arrays.asList("startDate", "endDate", "postedDate", "updatedDate"));

    /** The hierarchyFields. */
    private final ArrayList<String> hierarchyFields = new ArrayList<>(Arrays.asList("productTags", "usingWorkdayTags", "usingWorkday", "industryTags", "userTags", "programsTools", "programsToolsTags", "regionCountry", "trainingTags"));

    /** The stringFields. */
    private final ArrayList<String> stringFields = new ArrayList<>(Arrays.asList("pageTitle", NN_TEMPLATE, "eventHost", "eventLocation"));

    /** The page tags. */
    private static final Map<String, String> pageTagMap = Map.of("product", "productTags",
            "using-workday", "usingWorkdayTags", "programs-and-tools", "programsTools",
            "release", "releaseTags", "industry", "industryTags", "user", "userTags",
            "region-and-country", "regionCountry", "training", "trainingTags");

    /** The custom components. */
    private static final Map<String, String> customComponents =  Map.of("root/container/eventregistration/button", "registrationLink");

    /** The TEXT_COMPONENT. */
    public static final String TEXT_COMPONENT = "workday-community/components/core/text";

    /** The IDENTITY_TYPE_GROUP. */
    public static final String IDENTITY_TYPE_GROUP = "GROUP";

    /** The IDENTITY_TYPE_USER. */
    public static final String IDENTITY_TYPE_USER = "USER";

    /** The SECURITY_IDENTITY_PROVIDER. */
    private static final String SECURITY_IDENTITY_PROVIDER = "Community_Secured_Identity_Provider";

    @Override
    public HashMap<String, Object> extractPageProperties(String path) {
        HashMap<String, Object> properties = new HashMap<>();
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, SERVICE_USER)) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page page = null;
            if (pageManager != null) {
                page = pageManager.getPage(path);
            }
            if (page == null) {
                throw new ResourceNotFoundException("Page not found");
            }
            TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            ValueMap data = page.getProperties();
            String documentId = externalizer.publishLink(resourceResolverFactory.getThreadResourceResolver(), path) + ".html";
            properties.put("documentId", documentId);
            properties.put("isAem", true);
            processDateFields(data, properties);
            processStringFields(data, properties);
            processCustomComponents(page, properties);
            String email = processUserFields(data, userManager, properties);
            for (String taxonomyField: taxonomyFields) {
                String[] taxonomyIds = data.get(taxonomyField, String[].class);
                if (taxonomyIds != null && taxonomyIds.length > 0) {
                    ArrayList<String> value = processTaxonomyFields(tagManager, taxonomyIds, taxonomyField);
                    if (taxonomyField.equals("usingWorkday")) {
                        properties.put("usingWorkdayTags", value);
                    }
                    else if (taxonomyField.equals("programsTools")) {
                        properties.put("programsToolsTags", value);
                    }
                    else {
                        properties.put(taxonomyField, value);
                    }

                    if (hierarchyFields.contains(taxonomyField)) {
                        value = processHierarchyTaxonomyFields(tagManager, taxonomyIds, taxonomyField);
                        if (taxonomyField.equals("usingWorkday")) {
                            properties.put("usingWorkdayTagsHierarchy", value);
                        }
                        else if (taxonomyField.equals("programsTools")) {
                            properties.put("programsToolsTagsHierarchy", value);
                        }
                        else {
                            properties.put(taxonomyField + "Hierarchy", value);
                        }
                    }
                }
            }

            processPageTags(page, properties);

            processPermission(data, properties, email);

            Resource resource = resourceResolver.getResource( path + "/jcr:content");
            Node node = null;
            if (resource != null) {
                node = resource.adaptTo(Node.class);
            }

            ArrayList<String> textList = new ArrayList<>();
            if (node != null) {
                NodeIterator it = node.getNodes();
                processTextComponent(it, textList);
            }

            if (!textList.isEmpty()) {
                String description = String.join(" ", textList);
                properties.put("description", description);
            }
        }
        catch (LoginException | RepositoryException | SlingException e){
            logger.error("Extract page properties {} failed: {}", path, e.getMessage());
            return properties;
        }
        return properties;
    }

    @Override
    public void processDateFields(ValueMap data, HashMap<String, Object> properties) {
        for (String dateField: dateFields) {
            GregorianCalendar value = data.get(dateField, GregorianCalendar.class);
            if (value == null && dateField.equals("postedDate")) {
                value = data.get("jcr:created", GregorianCalendar.class);
            }
            if (value != null) {
                long time = value.getTimeInMillis() / 1000;
                properties.put(dateField, time);
            }
        }
    }

    @Override
    public void processPermission(ValueMap data, HashMap<String, Object> properties, String email) {
        // Coveo permission example: https://docs.coveo.com/en/107/cloud-v2-developers/simple-permission-model-definition-examples.
        // @Todo Once we set the page access(it will be the same as drupal, using access control tag
        // map it to AME groups) rewrite this function, right now it is hard coded,
        // allow authenticated user to view.
        HashMap<String, Object> permissionGroup21 = new HashMap<>();
        permissionGroup21.put("identity", "authenticated");
        permissionGroup21.put("identityType", IDENTITY_TYPE_GROUP);
        permissionGroup21.put("securityProvider", SECURITY_IDENTITY_PROVIDER);

        ArrayList<Object> permissionGroup2 = new ArrayList<>();
        permissionGroup2.add(permissionGroup21);
        if (email != null && email.trim().length() > 0) {
            HashMap<String, Object> permissionGroup22 = new HashMap<>();
            permissionGroup22.put("identity", email);
            permissionGroup22.put("identityType", IDENTITY_TYPE_USER);
            permissionGroup22.put("securityProvider", SECURITY_IDENTITY_PROVIDER);
            permissionGroup2.add(permissionGroup22);
        }

        HashMap<String, Object> permissionGroup = new HashMap<>();
        permissionGroup.put("allowAnonymous", false);
        permissionGroup.put("allowedPermissions", permissionGroup2);
        ArrayList<Object> permission = new ArrayList<>();
        permission.add(permissionGroup);
        properties.put("permissions", permission);
    }

    @Override
    public void processStringFields(ValueMap data, HashMap<String, Object> properties) {
        for (String stringField: stringFields) {
            String value = data.get(stringField, String.class);
            if (stringField.equals("pageTitle") && value == null) {
                value = data.get(JCR_TITLE, String.class);
            }
            if (value != null) {
                if (stringField.equals(NN_TEMPLATE)) {
                    properties.put("contentType", CONTENT_TYPE_MAPPING.get(value));
                }
                else {
                    properties.put(stringField, value);
                }
            }
        }
    }

    @Override
    public ArrayList<String> processTaxonomyFields(TagManager tagManager, String[] taxonomyTagIds, String taxonomyField) {
        ArrayList<String> processedTags = new ArrayList<>();
        for (String tagIdString: taxonomyTagIds) {
            Tag tag = tagManager.resolve(tagIdString);
            if (tag != null) {
                processedTags.add(tag.getTitle());
            }
        }
        return processedTags;
    }

    @Override
    public ArrayList<String> processHierarchyTaxonomyFields(TagManager tagManager, String[] taxonomyTagIds, String taxonomyField) {
        ArrayList<String> processedTags = new ArrayList<>();
        for (String tagIdString: taxonomyTagIds) {
            int index = tagIdString.indexOf("/");
            ArrayList<String> tagIdsList = new ArrayList<>();
            while(index >= 0) {
                tagIdsList.add(tagIdString.substring(0, index));
                index = tagIdString.indexOf("/", index + 1);
            }
            tagIdsList.add(tagIdString);
            ArrayList<String> tagString = new ArrayList<>();
            for(String tagId: tagIdsList) {
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

    @Override
    public void processTextComponent(NodeIterator it, ArrayList<String> textList) {
        while(it.hasNext()) {
            Node childNode = it.nextNode();
            try {
                if (childNode.hasProperty(SLING_RESOURCE_TYPE_PROPERTY)) {
                    String resourceType = childNode.getProperty(SLING_RESOURCE_TYPE_PROPERTY).getValue().getString();
                    if (resourceType.equals(TEXT_COMPONENT)) {
                        String text = childNode.getProperty("text").getValue().getString();
                        textList.add(text);
                    }
                }
                NodeIterator childIt = childNode.getNodes();
                processTextComponent(childIt, textList);
            }
            catch(RepositoryException e) {
                logger.error("Iterator page jcr:content failed: {}", e.getMessage());
            }
        }
    }

    @Override
    public String processUserFields(ValueMap data, UserManager userManager, HashMap<String, Object> properties) {
        String userName = data.get(PN_PAGE_LAST_MOD_BY, String.class);
        String email = "";
        if (userName != null) {
            properties.put("author", userName);
            try {
                User user = (User) userManager.getAuthorizable(userName);
                // @Todo When we do the author migration, need to pass author profile link, contact id is needed.
                // Example link: https://dev-resourcecenter.workday.com/en-us/wrc/public-profile.html?id=5222115.
                email = (user != null && user.getProperty("./profile/email") != null) ? Objects.requireNonNull(user.getProperty("./profile/email"))[0].getString() : null;
                properties.put("authorLink", "https://dev-resourcecenter.workday.com/en-us/wrc/public-profile.html?id=5222115");
            }
            catch(RepositoryException e) {
                logger.error("Extract user email and contact number failed: {}", e.getMessage());
                return email;
            }
        }
        return email;
    }

    @Override
    public void processCustomComponents(Page page, HashMap<String, Object> properties) {
        for(Map.Entry<String, String> component : customComponents.entrySet()) {
            Resource res = page.getContentResource(component.getKey());
            if (res != null) {
                String url = (String) res.getValueMap().get("linkURL");
                if (url != null) {
                    properties.put(component.getValue(), url);
                }
            }
        }
    }

    @Override
    public void processPageTags(Page page, Map<String, Object> properties) {
        Tag[] tags = page.getTags();
        for (Tag tag: tags) {
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
                   while(!tag.isNamespace()) {
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

}
