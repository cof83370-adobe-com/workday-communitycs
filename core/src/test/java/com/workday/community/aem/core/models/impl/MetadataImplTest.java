package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.Metadata;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class MetadataImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class MetadataImplTest {
    /** The context. */
    private final AemContext context = new AemContext();

    /** The resource resolver. */
    @Mock
    ResourceResolver resourceResolver;

    /** The user manager. */
    @Mock
    UserManager userManager;

    /** The authorizable. */
    @Mock
    Authorizable authorizable;

    /** The gn value. */
    @Mock
    Value gnValue;

    /** The fn value. */
    @Mock
    Value fnValue;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    public void setup() throws Exception {
        context.load().json("/com/workday/community/aem/core/models/impl/MetadataImplTest.json", "/content");
    }

    /**
     * Testget author name author prop not in jcr.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_authorPropNotInJcr() throws RepositoryException {

        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("admin")).thenReturn(authorizable);
        when(authorizable.isGroup()).thenReturn(false);
        Value[] gnValueArray = new Value[] { gnValue };
        Value[] fnValueArray = new Value[] { fnValue };
        when(authorizable.getProperty("./profile/givenName")).thenReturn(gnValueArray);
        when(authorizable.getProperty("./profile/familyName")).thenReturn(fnValueArray);
        when(gnValueArray[0].getString()).thenReturn("Demo");
        when(fnValueArray[0].getString()).thenReturn("User");
        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("Demo User", authorName);
    }

    /**
     * Testget author name author user not found.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_authorUserNotFound() throws RepositoryException {

        Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("demo author")).thenReturn(null);
        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("demo author", authorName);
    }

    /**
     * Testget author name author name null.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_authorNameNull() throws RepositoryException {

        Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("demo author")).thenReturn(authorizable);
        when(authorizable.isGroup()).thenReturn(false);
        when(authorizable.getProperty("./profile/givenName")).thenReturn(null);
        when(authorizable.getProperty("./profile/familyName")).thenReturn(null);
        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("demo author", authorName);
    }

    /**
     * Testget author name only given name.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_onlyGivenName() throws RepositoryException {

        Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("demo author")).thenReturn(authorizable);
        when(authorizable.isGroup()).thenReturn(false);
        Value[] gnValueArray = new Value[] { gnValue };
        when(authorizable.getProperty("./profile/givenName")).thenReturn(gnValueArray);
        when(authorizable.getProperty("./profile/familyName")).thenReturn(null);
        when(gnValueArray[0].getString()).thenReturn("Demo");

        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("Demo", authorName);
    }

    /**
     * Testget author name only familyname.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_onlyFamilyname() throws RepositoryException {

        Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("demo author")).thenReturn(authorizable);
        when(authorizable.isGroup()).thenReturn(false);
        Value[] fnValueArray = new Value[] { fnValue };
        when(authorizable.getProperty("./profile/givenName")).thenReturn(null);
        when(authorizable.getProperty("./profile/familyName")).thenReturn(fnValueArray);
        when(fnValueArray[0].getString()).thenReturn("Author");

        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("Author", authorName);
    }

    /**
     * Testget author name white space.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_whiteSpace() throws RepositoryException {

        Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("demo author")).thenReturn(authorizable);
        when(authorizable.isGroup()).thenReturn(false);
        Value[] gnValueArray = new Value[] { gnValue };
        Value[] fnValueArray = new Value[] { fnValue };
        when(authorizable.getProperty("./profile/givenName")).thenReturn(gnValueArray);
        when(authorizable.getProperty("./profile/familyName")).thenReturn(fnValueArray);
        when(gnValueArray[0].getString()).thenReturn("  Author  ");
        when(fnValueArray[0].getString()).thenReturn("  ");

        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("Author", authorName);
    }

    /**
     * Testget author name repository exception with author.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_repositoryException_withAuthor() throws RepositoryException {

        Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("demo author")).thenReturn(authorizable);
        when(authorizable.isGroup()).thenReturn(false);
        when(authorizable.getProperty("./profile/givenName")).thenThrow(RepositoryException.class);
        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("demo author", authorName);
    }

    /**
     * Testget author name repository exception without author.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetAuthorName_repositoryException_withoutAuthor() throws RepositoryException {

        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        context.registerService(ResourceResolver.class, resourceResolver);
        context.registerService(UserManager.class, userManager);
        context.registerService(Authorizable.class, authorizable);

        when(resourceResolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(userManager.getAuthorizable("admin")).thenReturn(authorizable);
        when(authorizable.isGroup()).thenReturn(false);
        when(authorizable.getProperty("./profile/givenName")).thenThrow(RepositoryException.class);
        Metadata metadata = context.request().adaptTo(Metadata.class);

        String authorName = metadata.getAuthorName();
        assertEquals("Unknown", authorName);
    }

    /**
     * Testget posted date.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetPostedDate() throws RepositoryException {
        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        Metadata metadata = context.request().adaptTo(Metadata.class);
        assertNotNull(metadata.getPostedDate());
    }

    /**
     * Testget update date.
     *
     * @throws RepositoryException the repository exception
     */
    @Test
    public void testgetUpdateDate() throws RepositoryException {
        Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);
        context.registerService(Page.class, currentPage);
        Metadata metadata = context.request().adaptTo(Metadata.class);
        assertNotNull(metadata.getUpdatedDate());
    }
}
