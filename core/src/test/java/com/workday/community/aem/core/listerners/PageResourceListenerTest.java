package com.workday.community.aem.core.listerners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.listeners.PageResourceListener;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.services.QueryService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.Property;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class PageResourceListenerTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class PageResourceListenerTest {

  /**
   * The context.
   */
  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  /**
   * The PageResourceListener.
   */
  @InjectMocks
  private PageResourceListener pageResourceListener;

  /**
   * A mocked CacheManagerService object.
   */
  @Mock
  private CacheManagerService cacheManager;

  /**
   * A mocked DrupalService object.
   */
  @Mock
  private DrupalService drupalService;

  /**
   * A mocked QueryService object.
   */
  @Mock
  private QueryService queryService;

  /**
   * A mocked Node object.
   */
  @Mock
  private Node node;

  /**
   * A mocked ResourceResolver object.
   */
  @Mock
  private ResourceResolver resolver;

  /**
   * A mocked PageManager object.
   */
  @Mock
  private PageManager pageManager;

  /**
   * A mocked ValueMap object.
   */
  @Mock
  private ValueMap valueMap;

  /**
   * A mocked Page object.
   */
  @Mock
  private Page page;

  /**
   * A mocked Resource object.
   */
  @Mock
  private Resource resource;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    context.load().json("/com/workday/community/aem/core/models/impl/BookOperationsServiceImplTestData.json",
        "/content");
    Page currentPage = context.currentResource("/content/book-faq-page").adaptTo(Page.class);
    context.registerService(Page.class, currentPage);
    context.registerService(ResourceResolver.class, resolver);
  }

  /**
   * Test Remove Book Nodes.
   *
   * @throws CacheException If there's an error getting a ResourceResolver from
   *                        the cache manager.
   */
  @Test
  void testRemoveBookNodes() throws CacheException {
    List<ResourceChange> changes = new ArrayList<>();
    ResourceChange resourceChange = this.createResourceChange(ResourceChange.ChangeType.REMOVED,
        context.currentPage().getPath());
    changes.add(resourceChange);

    List<String> pathList = new ArrayList<>();
    pathList.add("/content/book-1/jcr:content/root/container/container/book");
    lenient().when(queryService.getBookNodesByPath(context.currentPage().getPath(), null)).thenReturn(pathList);
    lenient().when(cacheManager.getServiceResolver(anyString())).thenReturn(resolver);

    when(resolver.getResource(anyString())).thenReturn(resource);
    pageResourceListener.onChange(changes);
    verify(resolver).close();
  }

  /**
   * Test Adding Author Property to Content Node.
   *
   * @throws Exception the exception
   */
  @Test
  void testAddAuthorPropertyToContentNode() throws Exception {
    Node expectedUserNode = mock(Node.class);
    Property prop1 = mock(Property.class);
    lenient().when(resolver.getResource(context.currentPage().getContentResource().getPath())).thenReturn(resource);
    lenient().when(resource.adaptTo(Node.class)).thenReturn(expectedUserNode);
    lenient().when(expectedUserNode.getProperty(anyString())).thenReturn(prop1);
    lenient().when(prop1.getString()).thenReturn("test user");
    String jsonString = "{\"users\":[{\"sfId\":\"0031B00002s0XHQQA2\",\"username\":\"acarmichael\",\"firstName\":\"fake_first_name\",\"lastName\":\"fake_last_name\",\"email\":\"andy.carmichael@workday.com.uat\",\"profileImageData\":\"data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGh...\"}]}";

    JsonElement jsonElement = JsonParser.parseString(jsonString);
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    assertNotNull(drupalService);
    lenient().when(drupalService.searchOurmUserList(anyString())).thenReturn(jsonObject);
    pageResourceListener.addAuthorPropertyToContentNode(context.currentPage().getContentResource().getPath(), resolver);
  }

  /**
   * Test Add Internal Workmates Tag.
   *
   * @throws Exception Exception object.
   */
  @Test
  void testAddMandatoryTags() throws Exception {
    List<String> updatedACLTags = new ArrayList<>(Arrays.asList("product:hcm", "access-control:internal_workmates"));
    String[] aclTags = { "product:hcm" };
    when(page.getProperties()).thenReturn(valueMap);
    when(page.getContentResource()).thenReturn(resource);
    when(resource.adaptTo(Node.class)).thenReturn(node);
    when(valueMap.get(GlobalConstants.CQ_TAGS_PROPERTY, String[].class)).thenReturn(aclTags);
    pageResourceListener.addMandatoryTags(context.currentPage().getContentResource().getPath(), page);
    verify(node, times(1)).setProperty(GlobalConstants.CQ_TAGS_PROPERTY, updatedACLTags.toArray(String[]::new));
  }

  private ResourceChange createResourceChange(ResourceChange.ChangeType changeType, String path) {
    return new ResourceChange(changeType, path, false);
  }

}
