package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.models.Metadata;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
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

/**
 * The Class MetadataImplTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class MetadataImplTest {
  /**
   * The context.
   */
  private final AemContext context = new AemContext();

  /**
   * The resource resolver.
   */
  @Mock
  ResourceResolver resourceResolver;

  /**
   * The user manager.
   */
  @Mock
  UserManager userManager;

  /**
   * The authorizable.
   */
  @Mock
  Authorizable authorizable;

  /**
   * The gn value.
   */
  @Mock
  Value gnValue;

  /**
   * The fn value.
   */
  @Mock
  Value fnValue;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    context.load()
        .json("/com/workday/community/aem/core/models/impl/MetadataImplTest.json", "/content");
  }

  /**
   * Testget author name author prop not in jcr.
   *
   * @throws RepositoryException the repository exception
   */
  @Test
  public void testUserName_NoPropInJcr() throws RepositoryException {
    Page currentPage = context.currentResource("/content/event-page").adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    Metadata metadata = context.request().adaptTo(Metadata.class);
    String authorName = metadata.getUserName();
    assertEquals("", authorName);
  }

  /**
   * Testget author name author name null.
   *
   * @throws RepositoryException the repository exception
   */
  @Test
  public void testUserName() throws RepositoryException {
    Page currentPage = context.currentResource("/content/release-notes-page").adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    Metadata metadata = context.request().adaptTo(Metadata.class);
    String authorName = metadata.getUserName();
    assertEquals("test username", authorName);
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
