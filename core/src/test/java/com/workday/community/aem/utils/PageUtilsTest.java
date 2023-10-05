package com.workday.community.aem.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
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

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class PageUtilsTest {

  private final AemContext context = new AemContext();

  @Mock
  ResourceResolver resourceResolver;

  @Mock
  Session session;

  @Mock
  Property property;

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    when(session.itemExists(anyString())).thenReturn(true);
  }

  @Test
  void testGetPageTagsTitleList() throws RepositoryException {
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

  @Test
  void testGetPageTagsTitleListReturnsEmptyListIfPageIsNull() throws RepositoryException {
    PageManager pageManager = mock(PageManager.class);
    when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pageManager);
    when(pageManager.getPage(anyString())).thenReturn(null);

    List<String> expectedTagTitlesList = Collections.emptyList();
    List<String> actualTagTitlesList = PageUtils.getPageTagsTitleList("pagePath", resourceResolver);

    assertEquals(expectedTagTitlesList, actualTagTitlesList);
  }
}
