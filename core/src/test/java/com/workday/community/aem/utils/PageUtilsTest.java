package com.workday.community.aem.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.utils.PageUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class PageUtilsTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class PageUtilsTest {

  /** The context. */
  private final AemContext context = new AemContext();

  /** The resource resolver. */
  @Mock
  ResourceResolver resourceResolver;

  /** The session. */
  @Mock
  Session session;

  /** The property. */
  @Mock
  Property property;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test get page tags title list.
   *
   * @throws RepositoryException the repository exception
   */
  @Test
  void testGetPageTagsTitleList() throws RepositoryException {
    when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    when(session.itemExists(anyString())).thenReturn(true);
    PageManager pageManager = mock(PageManager.class);
    Page page = mock(Page.class);
    Tag[] tags = new Tag[2];
    tags[0] = context.create().tag("work-day:groups/customer");
    tags[1] = context.create().tag("work-day:groups/workmate");

    when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    when(pageManager.getPage(anyString())).thenReturn(page);
    when(page.getTags()).thenReturn(tags);

    List<String> expectedTagTitlesList = new ArrayList<>();
    expectedTagTitlesList.add("customer");
    expectedTagTitlesList.add("workmate");

    List<String> actualTagTitlesList = PageUtils.getPageTagsTitleList("pagePath", resourceResolver);

    assertEquals(expectedTagTitlesList, actualTagTitlesList);
  }

  /**
   * Test publish instance true.
   */
  @Test
  void testPublishInstanceTrue() {
    boolean responseVal = PageUtils.isPublishInstance("publish");
    assertEquals(responseVal, true);
  }

  /**
   * Test publish instance false.
   */
  @Test
  void testPublishInstanceFalse() {
    boolean responseVal = PageUtils.isPublishInstance("author");
    assertEquals(responseVal, false);
  }

  /**
   * Test append extension with internal page.
   */
  @Test
  void testAppendExtensionWithInternalPage() {
    String responseVal = PageUtils.appendExtension("/content/workday-community/en-us/testpath/test");
    assertEquals(responseVal, "/content/workday-community/en-us/testpath/test.html");
  }

  /**
   * Test append extension with external link.
   */
  @Test
  void testAppendExtensionWithExternalLink() {
    String responseVal = PageUtils.appendExtension("https:///example.com/testpath/test.html");
    assertEquals(responseVal, "https:///example.com/testpath/test.html");
  }

  /**
   * Test get page title from path.
   */
  @Test
  void testGetPageTitleFromPath() {
    PageManager pageManager = mock(PageManager.class);
    when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    Page page = mock(Page.class);
    when(pageManager.getPage(anyString())).thenReturn(page);
    when(page.getTitle()).thenReturn("Test Page Title");
    String responseVal = PageUtils.getPageTitleFromPath(resourceResolver,
        "/content/workday-community/en-us/testpath/test");
    assertEquals(responseVal, "Test Page Title");
  }

  /**
   * Test get page tags title list returns empty list if page is null.
   *
   * @throws RepositoryException the repository exception
   */
  @Test
  void testGetPageTagsTitleListReturnsEmptyListIfPageIsNull() throws RepositoryException {
    when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    when(session.itemExists(anyString())).thenReturn(true);
    PageManager pageManager = mock(PageManager.class);
    when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    when(pageManager.getPage(anyString())).thenReturn(null);

    List<String> expectedTagTitlesList = Collections.emptyList();
    List<String> actualTagTitlesList = PageUtils.getPageTagsTitleList("pagePath", resourceResolver);

    assertEquals(expectedTagTitlesList, actualTagTitlesList);
  }

}