package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.commons.Externalizer;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;

/**
 * The Class ExtractPagePropertiesServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
public class ExtractPagePropertiesServiceImplTest {
    
    /** The service ExtractPagePropertiesServiceImpl. */
    @Spy 
    private ExtractPagePropertiesServiceImpl extract;

    /** The service HttpsURLConnectionService. */
    @Mock
    ResourceResolverFactory resourceResolverFactory;

    @Mock
    Externalizer externalizer;

    /**
     * Test process taxonomy field product.
     */
    @Test
    public void testProcessTaxonomyFieldProduct() {
        TagManager tagManager = Mockito.mock(TagManager.class);
        Tag tagOne = Mockito.mock(Tag.class);
        Tag tagTwo = Mockito.mock(Tag.class);
        Tag tagThree = Mockito.mock(Tag.class);
        String[] taxonomyTagIds = {"product:1/2", "product:2"};
        doReturn(tagOne).when(tagManager).resolve("product:1");
        doReturn(tagTwo).when(tagManager).resolve("product:1/2");
        doReturn(tagThree).when(tagManager).resolve("product:2");

        doReturn("Product 1").when(tagOne).getTitle();
        doReturn("Product 1.2").when(tagTwo).getTitle();
        doReturn("Product 2").when(tagThree).getTitle();

        ArrayList<String> values = extract.processTaxonomyFields(tagManager, taxonomyTagIds, "productTags");
        assertTrue(values.contains("Product 1"));
        assertTrue(values.contains("Product 2"));
        assertTrue(values.contains("Product 1|Product 1.2"));  
        assertFalse(values.contains("Product 1.2")); 
    }

    /**
     * Test process taxonomy field release.
     */
    @Test
    public void testProcessTaxonomyFieldRelease() {
        TagManager tagManager = Mockito.mock(TagManager.class);
        Tag tagOne = Mockito.mock(Tag.class);
        Tag tagTwo = Mockito.mock(Tag.class);
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
     * Test process text component.
     */
    @Test
    public void testProcessTextComponent() {
        NodeIterator it = Mockito.mock(NodeIterator.class);
        ArrayList<String> textlist = new ArrayList<String>();
        doReturn(false).when(it).hasNext();
        extract.processTextComponnet(it, textlist);
        assertEquals(0, textlist.size());
    }

    /**
     * Test process page permission.
     */
    @Test
    public void testProcessPermission() {
        ValueMap data = Mockito.mock(ValueMap.class);
        HashMap<String, Object> properties = new HashMap<String, Object>();
        extract.processPermission(data, properties, "test@gmail.com");
        assertTrue(properties.containsKey("permissions"));
    }

    /**
     * Test process string fields.
     */
    @Test 
    public void testPorcessStringFields() {
        ValueMap data = Mockito.mock(ValueMap.class);
        HashMap<String, Object> properties = new HashMap<String, Object>();
        doReturn("/conf/community/settings/wcm/templates/event-page-template").when(data).get("cq:template", null);
        doReturn("Page title").when(data).get("jcr:title", null);
        doReturn(null).when(data).get("pageTitle", null);
        extract.processStringFields(data, properties);
        assertEquals(properties.get("pageTitle"), "Page title");
        assertEquals(properties.get("contentType"), "Calendar Event");
    }

    /**
     * Test process date fields.
     */
    @Test 
    public void testPorcessDateFields() {
        ValueMap data = Mockito.mock(ValueMap.class);
        HashMap<String, Object> properties = new HashMap<String, Object>();
        GregorianCalendar value = new GregorianCalendar();
        doReturn(value).when(data).get("startDate", null);
        extract.processDateFields(data, properties);
        assertEquals(properties.get("startDate"), value.getTimeInMillis() / 1000);
    }

    /**
     * Test process user fields.
     * @throws RepositoryException
     */
    @Test 
    public void testPorcessUserFields() throws RepositoryException {
        ValueMap data = Mockito.mock(ValueMap.class);
        HashMap<String, Object> properties = new HashMap<String, Object>();
        UserManager userManager = Mockito.mock(UserManager.class);
        User user = Mockito.mock(User.class);
        String userName = "admin";
        String authorLink = "https://dev-resourcecenter.workday.com/en-us/wrc/public-profile.html?id=5222115";
        doReturn(userName).when(data).get("cq:lastModifiedBy", String.class);
        doReturn(user).when(userManager).getAuthorizable(userName);
        Value value = Mockito.mock(Value.class);
        Value[] values = { value };
        String email = "test@gmail.com";
        doReturn(values).when(user).getProperty("./profile/email");
        doReturn(email).when(value).getString();
        String expectedEmail = extract.processUserFields(data, userManager, properties);
        assertEquals(userName, properties.get("author"));
        assertEquals(authorLink, properties.get("authorLink"));
        assertEquals(email, expectedEmail);
    }
}
