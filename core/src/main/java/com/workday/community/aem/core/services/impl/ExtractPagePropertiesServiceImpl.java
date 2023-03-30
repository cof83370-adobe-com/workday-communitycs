package com.workday.community.aem.core.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.NodeIterator;
import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
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
import static com.workday.community.aem.core.services.impl.QueryServiceImpl.SERVICE_USER;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;

/**
 * The Class ExtractPagePropertiesServiceImpl.
 */
@Component(
    service = ExtractPagePropertiesService.class,
    immediate = true
)
public class ExtractPagePropertiesServiceImpl implements ExtractPagePropertiesService {

    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The resource resolver factory. */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** The externalizer service. */
    @Reference
    private Externalizer externalizer;

    // @Todo Once Tek system unifies the tag format, we can remove duplicate tags here.
    /** The taxonomyFields. */
    private ArrayList<String> taxonomyFields = new ArrayList<>(
        Arrays.asList("productTags", "usingWorkday", "usingWorkdayTags", "programsTools", "programsToolsTags", "releaseTags", "industryTags", "userTags", "regionCountry", "eventAudience", "eventFormat")
    );

    /** The dateFields. */
    private ArrayList<String> dateFields = new ArrayList<>(Arrays.asList("startDate", "endDate", "postedDate", "updatedDate"));

    /** The hierarchyFields. */
    private ArrayList<String> hierarchyFields = new ArrayList<>(Arrays.asList("productTags", "usingWorkdayTags", "usingWorkday"));

    /** The stringFields. */
    private ArrayList<String> stringFields = new ArrayList<>(Arrays.asList("pageTitle", "cq:template", "eventHost", "eventLocation"));

    /** The contentTypeMapping. */
    private Map<String,String> contentTypeMapping = Map.of(
        "/conf/community/settings/wcm/templates/event-page-template", "Calendar Event", 
        "/conf/community/settings/wcm/templates/faq", "FAQ", 
        "/conf/community/settings/wcm/templates/kits-and-tools", "Kits and Tools", 
        "/conf/community/settings/wcm/templates/reference", "Reference");

    /** The TEXT_COMPONENT. */
    public static final String TEXT_COMPONENT = "community/components/text";

    /** The IDENTITY_TYPE_GROU. */
    public static final String IDENTITY_TYPE_GROUP = "GROUP";

    /** The IDENTITY_TYPE_USER. */
    public static final String IDENTITY_TYPE_USER = "USER";

    /** The SECURITY_INDETITY_PROVIDER. */
    private static final String SECURITY_INDETITY_PROVIDER = "Community_Secured_Identity_Provider";

    @Override
    public HashMap<String, Object> extractPageProperties(String path) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, SERVICE_USER)) {
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            Page page = pageManager.getPage(path);
            TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
            UserManager userManager = resourceResolver.adaptTo(UserManager.class);
            ValueMap data = page.getProperties();

            String documentId = externalizer.publishLink(resourceResolverFactory.getThreadResourceResolver(), path) + ".html";
            properties.put("documentId", documentId);
            properties.put("isAem", true);
            processDateFields(data, properties);
            processStringFields(data, properties);
            String email = processUserFields(data, userManager, properties);
               
            for (String taxonomyField: taxonomyFields) {
                String[] taxonomyIds = data.get(taxonomyField, String[].class);
                if (taxonomyIds != null && taxonomyIds.length > 0) {
                    ArrayList<String> value = processTaxonomyFields(tagManager, taxonomyIds, taxonomyField);
                    if (taxonomyField == "usingWorkday") {
                        properties.put("usingWorkdayTags", value);
                    }
                    else if (taxonomyField == "programsTools") {
                        properties.put("programsToolsTags", value);
                    }
                    else {
                        properties.put(taxonomyField, value);
                    }
                }
            }
            processPermission(data, properties, email);

            Resource resource = resourceResolver.getResource( path + "/jcr:content");
            Node node = resource.adaptTo(Node.class);
            NodeIterator it = node.getNodes();
            ArrayList<String> textlist = new ArrayList<>();
            processTextComponnet(it, textlist);
            if (textlist.size() > 0) {
                String description = String.join(" ", textlist);
                properties.put("description", description);
            }
        }
        catch (Exception e){
            logger.error("Extract page properties {} failed: {}", path, e.getMessage());
            return properties;
        }
        return properties;
    }

    @Override
    public void processDateFields(ValueMap data, HashMap<String, Object> properties) {
        for (String dateField: dateFields) {
            GregorianCalendar value = data.get(dateField, null);
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
        HashMap<String, Object> permissionGroup21 = new HashMap<String, Object>();
        permissionGroup21.put("identity", "authenticated");
        permissionGroup21.put("identityType", IDENTITY_TYPE_GROUP);
        permissionGroup21.put("securityProvider", SECURITY_INDETITY_PROVIDER);

        ArrayList<Object> permissionGroup2 = new ArrayList<Object>();
        permissionGroup2.add(permissionGroup21);
        if (email.trim().length() > 0) {
            HashMap<String, Object> permissionGroup22 = new HashMap<String, Object>();
            permissionGroup22.put("identity", email);
            permissionGroup22.put("identityType", IDENTITY_TYPE_USER); 
            permissionGroup22.put("securityProvider", SECURITY_INDETITY_PROVIDER);
            permissionGroup2.add(permissionGroup22);
        }
        
        HashMap<String, Object> permissionGroup = new HashMap<String, Object>();
        permissionGroup.put("allowAnonymous", false);
        permissionGroup.put("allowedPermissions", permissionGroup2);
        ArrayList<Object> permission = new ArrayList<>();
        permission.add(permissionGroup);
        properties.put("permissions", permission);
    }

    @Override
    public void processStringFields(ValueMap data, HashMap<String, Object> properties) {
        for (String stringField: stringFields) {
            String value = data.get(stringField, null);
            if (stringField == "pageTitle" && value == null) {
                value = data.get("jcr:title", null);
            }
            if (value != null) {
                if (stringField == "cq:template") {
                    properties.put("contentType", contentTypeMapping.get(value));
                }
                else {
                    properties.put(stringField, value);
                }
            }
        }
    }

    @Override
    public ArrayList<String> processTaxonomyFields (TagManager tagManager, String[] taxonomyTagIds, String taxonomyField) {
        ArrayList<String> processedTags = new ArrayList<String>();
        for (String tagIdString: taxonomyTagIds) {
            if (hierarchyFields.contains(taxonomyField)) {
                int index = tagIdString.indexOf("/");
                ArrayList<String> tagIdsList = new ArrayList<String>();
                while(index >= 0) {
                    tagIdsList.add(tagIdString.substring(0, index));
                    index = tagIdString.indexOf("/", index + 1);
                }
                tagIdsList.add(tagIdString);
                ArrayList<String> tagString = new ArrayList<String>();
                for(String tagId: tagIdsList) {
                    Tag tag = tagManager.resolve(tagId);
                    if (tag != null) {
                        tagString.add(tag.getTitle());  
                        String record = String.join("|", tagString);
                        processedTags.add(record);
                    }
                }
            }
            else {
                Tag tag = tagManager.resolve(tagIdString); 
                if (tag != null) {
                    processedTags.add(tag.getTitle());
                }
            }
        }
        return processedTags;
    }

    @Override
    public void processTextComponnet(NodeIterator it, ArrayList<String> textlist) {
        while(it.hasNext()) {
            Node childNode = it.nextNode();
            try {
                if (childNode.hasProperty("sling:resourceType")) {
                    String resourceType = childNode.getProperty("sling:resourceType").getValue().getString();
                    if (resourceType.equals(TEXT_COMPONENT)) {
                        String text = childNode.getProperty("text").getValue().getString();
                        textlist.add(text);
                    }
                }
                NodeIterator childIt = childNode.getNodes();
                processTextComponnet(childIt, textlist);
            }
            catch(Exception e) {
                logger.error("Iterator page jcr:content failed: {}", e.getMessage());
            }
        }
    }

    @Override
    public String processUserFields(ValueMap data, UserManager userManager, HashMap<String, Object> properties) {
        String userName = data.get("cq:lastModifiedBy", String.class);
        String email = "";
        if (userName != null) {
            properties.put("author", userName);
            try {
                User user = (User) userManager.getAuthorizable(userName);
                // @Todo When we do the author migration, need to pass author profile link, contact id is needed.
                // Example link: https://dev-resourcecenter.workday.com/en-us/wrc/public-profile.html?id=5222115.
                email = user.getProperty("./profile/email") !=null ? user.getProperty("./profile/email")[0].getString() : null;
                properties.put("authorLink", "https://dev-resourcecenter.workday.com/en-us/wrc/public-profile.html?id=5222115");
            }
            catch(Exception e) {
                logger.error("Extract user email and contact number failed: {}", e.getMessage());
                return email;
            }
        }
        return email;
    }
    
}
