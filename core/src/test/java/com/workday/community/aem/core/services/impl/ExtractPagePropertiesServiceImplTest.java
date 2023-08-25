package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.services.impl.ExtractPagePropertiesServiceImpl.TEXT_COMPONENT;
import static java.util.Calendar.*;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;


import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.ImmutableMap;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.utils.ResolverUtil;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;

/**
 * The Class ExtractPagePropertiesServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
public class ExtractPagePropertiesServiceImplTest {

    /** The service HttpsURLConnectionService. */
    @Mock
    ResourceResolverFactory resourceResolverFactory;

    @Mock
    RunModeConfigService runModeConfigService;

    @Mock
    CacheManagerService cacheManager;

    /** The service ExtractPagePropertiesServiceImpl. */
    @InjectMocks
    private ExtractPagePropertiesServiceImpl extract;

    /**
     * Test process taxonomy field product.
     */
    @Test
    public void testProcessTaxonomyFieldProduct() {
        TagManager tagManager = mock(TagManager.class);
        Tag tagOne = mock(Tag.class);
        Tag tagTwo = mock(Tag.class);
        Tag tagThree = mock(Tag.class);
        String[] taxonomyTagIds = {"product:1/2", "product:2"};
        doReturn(tagOne).when(tagManager).resolve("product:1");
        doReturn(tagTwo).when(tagManager).resolve("product:1/2");
        doReturn(tagThree).when(tagManager).resolve("product:2");

        doReturn("Product 1").when(tagOne).getTitle();
        doReturn("Product 1.2").when(tagTwo).getTitle();
        doReturn("Product 2").when(tagThree).getTitle();

        ArrayList<String> values = extract.processHierarchyTaxonomyFields(tagManager, taxonomyTagIds, "productTags");
        assertTrue(values.contains("Product 1"));
        assertTrue(values.contains("Product 2"));
        assertTrue(values.contains("Product 1|Product 1.2"));
        assertFalse(values.contains("Product 1.2"));

        values = extract.processTaxonomyFields(tagManager, taxonomyTagIds, "productTags");
        assertTrue(values.contains("Product 2"));
        assertTrue(values.contains("Product 1.2"));
        assertFalse(values.contains("Product 1"));
    }

    /**
     * Test process taxonomy field release.
     */
    @Test
    public void testProcessTaxonomyFieldRelease() {
        TagManager tagManager = mock(TagManager.class);
        Tag tagOne = mock(Tag.class);
        Tag tagTwo = mock(Tag.class);
        String[] taxonomyTagIds = {"release:1/2", "release:2"};
        doReturn(tagOne).when(tagManager).resolve("release:1/2");
        doReturn(tagTwo).when(tagManager).resolve("release:2");

        doReturn("Release 1").when(tagOne).getTitle();
        doReturn("Release 1.2").when(tagTwo).getTitle();

        ArrayList<String> values = extract.processTaxonomyFields(tagManager, taxonomyTagIds, "releaseTags");
        assertTrue(values.contains("Release 1"));
        assertTrue(values.contains("Release 1.2"));
        assertFalse(values.contains("Release 1|Release 1.2"));
    }

    /**
     * Test process page permission.
     */
    @Test
    public void testProcessPermission() {
        ValueMap data = mock(ValueMap.class);
        String [] accessControlValues = {"access-control:customer_all", "access-control:customer_named_support_contact"};
        doReturn(accessControlValues).when(data).get("accessControlTags", String[].class);

        HashMap<String, Object> properties = new HashMap<>();
        extract.processPermission(data, properties, "test@gmail.com");
        String permissions = properties.toString();
        assertTrue(permissions.contains("customer"));
        assertTrue(permissions.contains("community_customer"));
        assertTrue(permissions.contains("customer_named_support_contact"));
        assertTrue(permissions.contains("test@gmail.com"));

        doReturn(new String[0]).when(data).get("accessControlTags", String[].class);
        HashMap<String, Object> emptyAccessProperties = new HashMap<>();
        extract.processPermission(data, emptyAccessProperties, "test@gmail.com");
        String emptyAccessPermissions = emptyAccessProperties.toString();
        assertTrue(emptyAccessPermissions.contains("exclude"));
        assertTrue(emptyAccessPermissions.contains("test@gmail.com"));
    }

    /**
     * Test process string fields.
     */
    @Test
    public void testPorcessStringFields() {
        ValueMap data = mock(ValueMap.class);
        HashMap<String, Object> properties = new HashMap<>();
        doReturn("Page title").when(data).get("jcr:title", String.class);
        doReturn(null).when(data).get("pageTitle", String.class);
        extract.processStringFields(data, properties);
        assertEquals(properties.get("pageTitle"), "Page title");
    }

    /**
     * Test process date fields.
     */
    @Test
    public void testPorcessDateFields() {
        ValueMap data = mock(ValueMap.class);
        HashMap<String, Object> properties = new HashMap<>();
        GregorianCalendar value = new GregorianCalendar();
        doReturn(value).when(data).get("eventStartDate", GregorianCalendar.class);
        extract.processDateFields(data, properties);
        assertEquals(properties.get("eventStartDate"), value.getTimeInMillis() / 1000);
    }

    /**
     * Test process user fields.
     * @throws RepositoryException repository Exception.
     */
    @Test
    public void testPorcessUserFields() throws RepositoryException {
        ValueMap data = mock(ValueMap.class);
        HashMap<String, Object> properties = new HashMap<>();
        UserManager userManager = mock(UserManager.class);
        User user = mock(User.class);
        String userName = "admin";
        String authorLink = "https://dev-resourcecenter.workday.com/en-us/wrc/public-profile.html?id=5222115";
        doReturn(userName).when(data).get("cq:lastModifiedBy", String.class);
        doReturn(user).when(userManager).getAuthorizable(userName);
        Value value = mock(Value.class);
        Value[] values = { value };
        String email = "test@gmail.com";
        doReturn(values).when(user).getProperty("./profile/email");
        doReturn(email).when(value).getString();
        String expectedEmail = extract.processUserFields(data, userManager, properties);
        assertEquals(userName, properties.get("author"));
        assertEquals(authorLink, properties.get("authorLink"));
        assertEquals(email, expectedEmail);
    }

    @Test
    public void testExtractPageProperties() {
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        try (MockedStatic<ResolverUtil> mock = mockStatic(ResolverUtil.class)) {
            mock.when(() -> cacheManager.getServiceResolver(anyString())).thenReturn(resourceResolver);
            PageManager pageManager = mock(PageManager.class);
            TagManager tagManager = mock(TagManager.class);
            UserManager userManager = mock(UserManager.class);
            Page page = mock(Page.class);
            lenient().when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
            lenient().when(resourceResolver.adaptTo(TagManager.class)).thenReturn(tagManager);
            lenient().when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
            lenient().when(pageManager.getPage(anyString())).thenReturn(page);
            lenient().when(runModeConfigService.getPublishInstanceDomain()).thenReturn("http://test-link.com");
            ValueMap testData = new ValueMapDecorator(ImmutableMap.of(
                "startDate", new GregorianCalendar(2023, JUNE,3),
                "endDate", new GregorianCalendar(2023, OCTOBER,3),
                "productTags", "Workday",
                "usingWorkday", "true",
                "usingWorkdayTags", "true"
            ));
            Tag[] tags = (new Tag[]{mock(Tag.class), mock(Tag.class)});
            Tag[] namespaces = (new Tag[]{mock(Tag.class), mock(Tag.class)});
            String[] names = new String[] {"product", "productTags"};
            lenient().when(page.getProperties()).thenReturn(testData);
            lenient().when(page.getTags()).thenReturn(tags);
            for (int i = 0; i <tags.length; i++) {
                lenient().when(tags[i].getNamespace()).thenReturn(namespaces[i]);
            }
            for (int i = 0; i <namespaces.length; i++) {
                lenient().when(namespaces[i].getName()).thenReturn(names[i]);
            }
            extract.extractPageProperties("test");
        }
    }

    @Test
    public void testProcessTextComponent() {
        List<Node> testItems = new ArrayList<>();
        testItems.add(mock(Node.class));
        testItems.add(mock(Node.class));

        testItems.forEach( node -> {
            try {
                Property property = mock(Property.class);
                Property propertyText = mock(Property.class);
                Value value = mock(Value.class);
                Value value1 = mock(Value.class);

                lenient().when(node.hasProperty(eq(SLING_RESOURCE_TYPE_PROPERTY))).thenReturn(true);
                lenient().when(node.getProperty(eq(SLING_RESOURCE_TYPE_PROPERTY))).thenReturn(property);
                lenient().when(node.getProperty(eq("text"))).thenReturn(propertyText);
                lenient().when(property.getValue()).thenReturn(value);
                lenient().when(value.getString()).thenReturn(TEXT_COMPONENT);
                lenient().when(propertyText.getValue()).thenReturn(value1);
                lenient().when(value1.getString()).thenReturn(TEXT_COMPONENT);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        });

        NodeIterator nodeIterator = new NodeIterator() {
            int count = 0;
            @Override
            public Node nextNode() {
                if (count < testItems.size()) {
                    Node next = testItems.get(count);
                    count++;
                    return next;
                }
                return null;
            }

            @Override
            public void skip(long l) {
            }

            @Override
            public long getSize() {
                return testItems.size();
            }

            @Override
            public long getPosition() {
                return count;
            }

            @Override
            public boolean hasNext() {
                return count < testItems.size();
            }

            @Override
            public Object next() {
                return nextNode();
            }
        };

        extract.processTextComponent(nodeIterator, new ArrayList<>());
    }
}
