package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.dto.BookDto;
import com.workday.community.aem.core.models.TocModel;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.utils.PageUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class MetadataImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class TocModelmplTest {

  /** The Constant EXT_PAGE_PATH. */
  private static final String EXT_PAGE_PATH = "https://community.workday.com/pro-services/tools/458760";

  /** The Constant BOOK_SOURCE_PATH. */
  private static final String BOOK_SOURCE_PATH = "/content/book-1/jcr:content/root/container/container/book/firstlevel/item0/secondlevel/item0/thirdlevel/item0";

  /** The Constant CURRENT_PAGE_STR. */
  private static final String CURRENT_PAGE_STR = "/content/event-page";

  /** The resource resolver. */
  @Mock
  ResourceResolver resourceResolver;

  /** The user. */
  @Mock
  User user;

  /** The request. */
  @Mock
  SlingHttpServletRequest request;

  /** The context. */
  private final AemContext context = new AemContext();

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    MockitoAnnotations.openMocks(this);
    context.addModelsForClasses(TocModelImpl.class);
    context.load().json("/com/workday/community/aem/core/models/impl/TocModelImplTest.json", "/content");
    context.registerService(SlingHttpServletRequest.class, request);
    context.registerService(ResourceResolver.class, resourceResolver);
  }

  /**
   * Test init.
   *
   * @throws Exception the exception
   */
  @Test
  void testInit() throws Exception {
    RunModeConfigService runModeConfigService = mock(RunModeConfigService.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");
    Page currentPage = context.currentResource(CURRENT_PAGE_STR).adaptTo(Page.class);
    QueryService queryService = mock(QueryService.class);
    context.registerService(QueryService.class, queryService);
    List<String> pathList = new ArrayList<>();
    pathList.add(BOOK_SOURCE_PATH);
    lenient().when(queryService.getBookNodesByPath(currentPage.getPath(), null)).thenReturn(pathList);
    context.registerService(Page.class, currentPage);
    when(resourceResolver.resolve(anyString())).thenReturn(mock(Resource.class));
    TocModelImpl tocModel = context.request().adaptTo(TocModelImpl.class);
    assertEquals(false, tocModel.isTocDisplay());
  }

  /**
   * Test book resource path null.
   *
   * @throws Exception the exception
   */
  @Test
  void testBookResourcePathNull() throws Exception {
    RunModeConfigService runModeConfigService = mock(RunModeConfigService.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");
    Page currentPage = context.currentResource(CURRENT_PAGE_STR).adaptTo(Page.class);
    QueryService queryService = mock(QueryService.class);
    context.registerService(QueryService.class, queryService);
    List<String> paths = new ArrayList<>();
    lenient().when(queryService.getBookNodesByPath(currentPage.getPath(), null)).thenReturn(paths);
    context.registerService(Page.class, currentPage);
    TocModel tocModel = context.request().adaptTo(TocModel.class);
    assertEquals(List.of(), tocModel.getFinalList());
  }

  /**
   * Test get final list only for first level.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetFinalListOnlyForFirstLevel() throws Exception {
    RunModeConfigService runModeConfigService = mock(RunModeConfigService.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");

    UserGroupService mockGroupService = mock(UserGroupService.class);
    context.registerService(UserGroupService.class, mockGroupService);
    lenient().when(mockGroupService.hasAccessToViewLink(anyString(), any())).thenReturn(true);

    Page currentPage = context.currentResource(CURRENT_PAGE_STR).adaptTo(Page.class);
    QueryService queryService = mock(QueryService.class);
    context.registerService(QueryService.class, queryService);
    List<String> pathList = new ArrayList<>();
    pathList.add(BOOK_SOURCE_PATH);
    lenient().when(queryService.getBookNodesByPath(currentPage.getPath(), null)).thenReturn(pathList);
    context.registerService(Page.class, currentPage);
    Resource mockedResource = mock(Resource.class);
    when(resourceResolver.resolve(anyString())).thenReturn(mockedResource);
    Resource firstLevelresource = mock(Resource.class);
    lenient().when(mockedResource.getChild("firstlevel")).thenReturn(firstLevelresource);
    Iterator<Resource> firstLevelItr = mock(Iterator.class);
    when(firstLevelresource.listChildren()).thenReturn(firstLevelItr);
    when(firstLevelItr.hasNext()).thenReturn(true).thenReturn(false);
    Resource itemsRes = mock(Resource.class);
    when(firstLevelItr.next()).thenReturn(itemsRes);
    ValueMap vmap = mock(ValueMap.class);
    when(itemsRes.adaptTo(ValueMap.class)).thenReturn(vmap);
    when(vmap.get("mainpagepath", "")).thenReturn(EXT_PAGE_PATH);
    try (MockedStatic<PageUtils> pageUtilsMock = mockStatic(PageUtils.class)) {
      pageUtilsMock.when(() -> PageUtils.isPublishInstance(any())).thenReturn(true);
      TocModelImpl tocModel = context.request().adaptTo(TocModelImpl.class);
      verify(firstLevelItr, times(2)).hasNext();
      verify(firstLevelItr, times(1)).next();
      assertEquals(1, tocModel.getFinalList().size());
      assertEquals("https://community.workday.com/pro-services/tools/458760",
          tocModel.getFinalList().get(0).getHeadingLink());
      assertEquals("", tocModel.getFinalList().get(0).getHeadingTitle());
      assertEquals(
          "BookDto(headingTitle=, headingLink=https://community.workday.com/pro-services/tools/458760, childLevelList=[])",
          tocModel.getFinalList().get(0).toString());
      assertNotNull(tocModel.getFinalList().get(0));
    }
  }

  /**
   * Test get final list only for first level with internal public link.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetFinalListOnlyForFirstLevelWithInternalPublicLink() throws Exception {
    RunModeConfigService runModeConfigService = mock(RunModeConfigService.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");

    UserGroupService mockGroupService = mock(UserGroupService.class);
    context.registerService(UserGroupService.class, mockGroupService);
    lenient().when(mockGroupService.hasAccessToViewLink(anyString(), any())).thenReturn(true);

    Page currentPage = context.currentResource(CURRENT_PAGE_STR).adaptTo(Page.class);
    QueryService queryService = mock(QueryService.class);
    context.registerService(QueryService.class, queryService);
    List<String> pathList = new ArrayList<>();
    pathList.add(BOOK_SOURCE_PATH);
    lenient().when(queryService.getBookNodesByPath(currentPage.getPath(), null)).thenReturn(pathList);
    context.registerService(Page.class, currentPage);
    Resource mockedResource = mock(Resource.class);
    when(resourceResolver.resolve(anyString())).thenReturn(mockedResource);
    Resource firstLevelresource = mock(Resource.class);
    lenient().when(mockedResource.getChild("firstlevel")).thenReturn(firstLevelresource);
    Iterator<Resource> firstLevelItr = mock(Iterator.class);
    when(firstLevelresource.listChildren()).thenReturn(firstLevelItr);
    when(firstLevelItr.hasNext()).thenReturn(true).thenReturn(false);
    Resource itemsRes = mock(Resource.class);
    when(firstLevelItr.next()).thenReturn(itemsRes);
    ValueMap vmap = mock(ValueMap.class);
    when(itemsRes.adaptTo(ValueMap.class)).thenReturn(vmap);
    when(vmap.get("mainpagepath", ""))
        .thenReturn("/content/workday-community/en-us/public/palla-user-authentication-testing/page1");
    lenient().when(request.getResourceResolver()).thenReturn(resourceResolver);
    PageManager pm = mock(PageManager.class);
    lenient().when(resourceResolver.adaptTo(PageManager.class)).thenReturn(pm);
    Page page = mock(Page.class);
    lenient().when(pm.getPage("/content/workday-community/en-us/public/palla-user-authentication-testing/page1"))
        .thenReturn(page);
    lenient().when(page.getTitle()).thenReturn("sample page title");
    try (MockedStatic<PageUtils> pageUtilsMock = mockStatic(PageUtils.class)) {
      pageUtilsMock.when(() -> PageUtils.isPublishInstance(any())).thenReturn(true);
      TocModelImpl tocModel = context.request().adaptTo(TocModelImpl.class);
      verify(firstLevelItr, times(2)).hasNext();
      verify(firstLevelItr, times(1)).next();
      assertEquals(1, tocModel.getFinalList().size());
    }
  }

  /**
   * Test get final list only for second level without access.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetFinalListOnlyForSecondLevelWithoutAccess() throws Exception {

    RunModeConfigService runModeConfigService = mock(RunModeConfigService.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");

    UserGroupService mockGroupService = mock(UserGroupService.class);
    context.registerService(UserGroupService.class, mockGroupService);
    lenient().when(mockGroupService.hasAccessToViewLink(anyString(), any())).thenReturn(true);

    Page currentPage = context.currentResource(CURRENT_PAGE_STR).adaptTo(Page.class);
    QueryService queryService = mock(QueryService.class);
    context.registerService(QueryService.class, queryService);
    List<String> pathList = new ArrayList<>();
    pathList.add(BOOK_SOURCE_PATH);
    lenient().when(queryService.getBookNodesByPath(currentPage.getPath(), null)).thenReturn(pathList);
    context.registerService(Page.class, currentPage);
    Resource mockedResource = mock(Resource.class);
    when(resourceResolver.resolve(anyString())).thenReturn(mockedResource);
    Resource firstLevelresource = mock(Resource.class);
    lenient().when(mockedResource.getChild("firstlevel")).thenReturn(firstLevelresource);
    Iterator<Resource> firstLevelItr = mock(Iterator.class);
    when(firstLevelresource.listChildren()).thenReturn(firstLevelItr);
    when(firstLevelItr.hasNext()).thenReturn(true).thenReturn(false);
    Resource itemsRes = mock(Resource.class);
    when(firstLevelItr.next()).thenReturn(itemsRes);
    ValueMap vmap = mock(ValueMap.class);
    when(itemsRes.adaptTo(ValueMap.class)).thenReturn(vmap);
    when(vmap.get("mainpagepath", "")).thenReturn(EXT_PAGE_PATH);
    Resource secLevelresource = mock(Resource.class);
    when(itemsRes.getChild("secondlevel")).thenReturn(secLevelresource);

    Iterator<Resource> secLevelItr = mock(Iterator.class);
    when(secLevelresource.listChildren()).thenReturn(secLevelItr);
    when(secLevelItr.hasNext()).thenReturn(true).thenReturn(false);

    Resource secItemsRes = mock(Resource.class);
    when(secLevelItr.next()).thenReturn(secItemsRes);

    ValueMap vmap2 = mock(ValueMap.class);
    when(secItemsRes.adaptTo(ValueMap.class)).thenReturn(vmap2);
    when(vmap2.get("secondpagepath", ""))
        .thenReturn("/content/workday-community/en-us/user-authentication-testing/page1");
    when(firstLevelItr.hasNext()).thenReturn(true).thenReturn(false);
    try (MockedStatic<PageUtils> pageUtilsMock = mockStatic(PageUtils.class)) {
      pageUtilsMock.when(() -> PageUtils.isPublishInstance(any())).thenReturn(true);
      TocModelImpl tocModel = context.request().adaptTo(TocModelImpl.class);
      // verify(secLevelItr, times(2)).hasNext();
      verify(secLevelItr, times(1)).next();
      verify(firstLevelItr, times(2)).hasNext();
      verify(firstLevelItr, times(1)).next();
      // assertEquals(1, tocModel.getFinalList().size());
    }
  }

  /**
   * Test accumulate sub tree.
   *
   * @throws Exception the exception
   */
  @Test
  void testAccumulateSubTree() throws Exception {

    Resource secItemsRes = mock(Resource.class);
    Resource thirdLevelresource = mock(Resource.class);
    lenient().when(secItemsRes.getChild("thirdlevel")).thenReturn(thirdLevelresource);

    Iterator<Resource> thirdLevelItr = mock(Iterator.class);
    when(thirdLevelresource.listChildren()).thenReturn(thirdLevelItr);
    when(thirdLevelItr.hasNext()).thenReturn(true, true, false);

    Resource thirdItemsRes = mock(Resource.class);
    Resource thirdItemsRes2 = mock(Resource.class);
    when(thirdLevelItr.next()).thenReturn(thirdItemsRes, thirdItemsRes2);

    ValueMap vmap = mock(ValueMap.class);
    when(thirdItemsRes.adaptTo(ValueMap.class)).thenReturn(vmap);
    when(vmap.get("thirdpagepath", "")).thenReturn(EXT_PAGE_PATH);

    ValueMap vmap2 = mock(ValueMap.class);
    when(thirdItemsRes2.adaptTo(ValueMap.class)).thenReturn(vmap2);
    when(vmap2.get("thirdpagepath", "")).thenReturn("https://community.workday.com/pro-services/tools/551678");

    TocModelImpl tocModel = context.request().adaptTo(TocModelImpl.class);
    BookDto firstLevelDTO = new BookDto();
    BookDto secondLevelDTO = new BookDto();
    tocModel.accumulateSubTree(secItemsRes, firstLevelDTO, secondLevelDTO);
    verify(thirdLevelItr, times(3)).hasNext();
    verify(thirdLevelItr, times(2)).next();
    assertEquals(2, secondLevelDTO.getChildLevelList().size());
    assertEquals(EXT_PAGE_PATH, firstLevelDTO.getChildLevelList().get(0).getChildLevelList().get(0).getHeadingLink());
  }

  /**
   * Test is toc display.
   *
   * @throws Exception the exception
   */
  @Test
  void testIsTocDisplay() throws Exception {
    TocModelImpl tocModel = context.request().adaptTo(TocModelImpl.class);
    assertFalse(tocModel.isTocDisplay());
  }
}