package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.dto.BookDto;
import com.workday.community.aem.core.models.TocModel;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.PageUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

/**
 * The Class TocModelImpl.
 */
@Slf4j
@Model(adaptables = { Resource.class, SlingHttpServletRequest.class }, adapters = { TocModel.class }, resourceType = {
    TocModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TocModelImpl implements TocModel {

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/common/toc";

  /** The Constant FIRST_LEVEL_NODE_NAME. */
  protected static final String FIRST_LEVEL_NODE_NAME = "firstlevel";

  /** The Constant FIRST_LEVEL_LINK_PROPERTY. */
  protected static final String FIRST_LEVEL_LINK_PROPERTY = "mainpagepath";

  /** The Constant SECOND_LEVEL_NODE_NAME. */
  protected static final String SECOND_LEVEL_NODE_NAME = "secondlevel";

  /** The Constant SECOND_LEVEL_LINK_PROPERTY. */
  protected static final String SECOND_LEVEL_LINK_PROPERTY = "secondpagepath";

  /** The Constant THIRD_LEVEL_NODE_NAME. */
  protected static final String THIRD_LEVEL_NODE_NAME = "thirdlevel";

  /** The Constant THIRD_LEVEL_LINK_PROPERTY. */
  protected static final String THIRD_LEVEL_LINK_PROPERTY = "thirdpagepath";

  /** The Constant STR_CONCAT_FORMAT. */
  protected static final String STR_CONCAT_FORMAT = "%s.html";

  /** The Constant HAS_ACCESS_KEY. */
  protected static final String HAS_ACCESS_KEY = "hasAccess";

  /** The Constant LEVEL_DTO_KEY. */
  protected static final String LEVEL_DTO_KEY = "levelDTO";

  /** The run mode config service. */
  @OSGiService
  private RunModeConfigService runModeConfigService;

  /** The resource resolver. */
  @Inject
  private ResourceResolver resourceResolver;

  /** The user service. */
  @OSGiService
  private UserService userService;

  /** The final list. */
  @Getter
  private List<BookDto> finalList = new ArrayList<>();

  /** The instance. */
  private String instance;

  /** The toc display. */
  @Getter
  private boolean tocDisplay;

  /**
   * The current page.
   */
  @Inject
  private Page currentPage;

  /** The request. */
  @Inject
  private SlingHttpServletRequest request;

  /**
   * The query service.
   */
  @OSGiService
  private QueryService queryService;

  /** The user group service. */
  @OSGiService
  private UserGroupService userGroupService;

  /**
   * Inits the TocModel implementation.
   */
  @PostConstruct
  public void init() {
    if (null != currentPage) {
      List<String> bookPathList = queryService.getBookNodesByPath(currentPage.getPath(), null);
      if (null != bookPathList && !bookPathList.isEmpty()) {
        final String bookResourcePath = bookPathList.get(0).split("/firstlevel")[0];
        log.debug("Book Resource path is::{}", bookResourcePath);
        instance = runModeConfigService.getInstance();
        if (StringUtils.isNotBlank(bookResourcePath)) {
          updateFinalList(resourceResolver.resolve(bookResourcePath));
        }
      }
    }
  }

  /**
   * Gets the book page title.
   *
   * @param pagePath the page path
   * @return the book page title
   */
  private String getBookPageTitle(String pagePath) {
    if (StringUtils.isNotBlank(pagePath) && pagePath.startsWith(GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH)) {
      PageManager pm = resourceResolver.adaptTo(PageManager.class);
      Page page = pm.getPage(pagePath);
      return page.getTitle();
    }
    return StringUtils.EMPTY;
  }

  /**
   * Update final list. The HTL display list is set by this method. In case the
   * instance pertains to a publish or public content page path, the authorization
   * check is bypassed.
   *
   * @param resource the resource
   * @return the list
   */
  private void updateFinalList(Resource resource) {
    log.debug("Entered in updateFinalList method:");
    Resource firstLevelresource = resource.getChild(FIRST_LEVEL_NODE_NAME);
    if (null != firstLevelresource) {
      Iterator<Resource> firstLevelItr = firstLevelresource.listChildren();
      while (firstLevelItr.hasNext()) {
        // items under firstLevel /book/firstlevel/item0..
        Resource itemsRes = firstLevelItr.next();

        Map<String, Object> firResultantMap = prepareLevelWiseDto(itemsRes, FIRST_LEVEL_LINK_PROPERTY);
        BookDto firstLevelDto;
        boolean checkPageAccess = (boolean) firResultantMap.get(HAS_ACCESS_KEY);
        if (checkPageAccess && firResultantMap.containsKey(LEVEL_DTO_KEY)) {
          firstLevelDto = (BookDto) firResultantMap.get(LEVEL_DTO_KEY);
        } else {
          continue;
        }
        Resource secLevelresource = itemsRes.getChild(SECOND_LEVEL_NODE_NAME);
        checkAndSetSecLevelDto(secLevelresource, firstLevelDto);
        finalList.add(firstLevelDto);
      }
    }
    log.debug("Final list to show case in TOC:{}", finalList);
    if (!finalList.isEmpty()) {
      checkTheListToDisplay();
    }
  }

  /**
   * Check and set sec level DTO.
   *
   * @param secLevelresource the sec levelresource
   * @param firstLevelDto    the first level DTO
   */
  private void checkAndSetSecLevelDto(Resource secLevelresource, BookDto firstLevelDto) {
    if (secLevelresource != null) {
      Iterator<Resource> secLevelItr = secLevelresource.listChildren();
      while (secLevelItr.hasNext()) {
        // items under firstLevel /book/firstlevel/item0/secondlevel/item0..
        Resource secItemsRes = secLevelItr.next();
        Map<String, Object> secResultantMap = prepareLevelWiseDto(secItemsRes, SECOND_LEVEL_LINK_PROPERTY);
        BookDto secLevelDto;
        boolean checkAccess = (boolean) secResultantMap.get(HAS_ACCESS_KEY);
        if (checkAccess && secResultantMap.containsKey(LEVEL_DTO_KEY)) {
          secLevelDto = (BookDto) secResultantMap.get(LEVEL_DTO_KEY);
        } else {
          continue;
        }
        accumulateSubTree(secItemsRes, firstLevelDto, secLevelDto);
      }
    }
  }

  /**
   * Check the list to display.
   */
  private void checkTheListToDisplay() {
    // Masking to make all nodes under one root for logic flexibility.
    BookDto maskedRoot = new BookDto();
    maskedRoot.setHeadingLink("Root");
    maskedRoot.setChildLevelList(finalList);
    String reqItem = currentPage.getPath();
    // Find item and its parents
    Map<String, List<String>> itemParentsMap = new HashMap<>();
    findItemAndParents(maskedRoot, null, reqItem, itemParentsMap);

    // Check for parent items access.
    List<String> parents = itemParentsMap.get(reqItem);
    if (null != parents && !parents.isEmpty()) {
      tocDisplay = true;
    } else {
      log.debug("Item not found in final List:{}", finalList);
    }
  }

  /**
   * Find item and parents.
   *
   * @param currentItem    the current item
   * @param parents        the parents
   * @param targetValue    the target value
   * @param itemParentsMap the item parents map
   */
  private static void findItemAndParents(BookDto currentItem, List<String> parents, String targetValue,
      Map<String, List<String>> itemParentsMap) {
    if (currentItem.getHeadingLink().contains(targetValue)) {
      itemParentsMap.put(targetValue, parents);
      return;
    }

    List<String> updatedParents = new ArrayList<>();
    if (parents != null) {
      updatedParents.addAll(parents);
    }
    updatedParents.add(currentItem.getHeadingLink());

    for (BookDto child : currentItem.getChildLevelList()) {
      findItemAndParents(child, updatedParents, targetValue, itemParentsMap);
    }
  }

  /**
   * Accumulate sub tree.
   *
   * @param secItemsRes   the sec items res
   * @param firstLevelDto the first level DTO
   * @param secLevelDto   the sec level DTO
   */
  protected void accumulateSubTree(Resource secItemsRes, BookDto firstLevelDto, BookDto secLevelDto) {
    Resource thirdLevelresource = secItemsRes.getChild(THIRD_LEVEL_NODE_NAME);
    if (thirdLevelresource != null) {
      Iterator<Resource> thirdLevelItr = thirdLevelresource.listChildren();
      List<BookDto> thirdlist = new ArrayList<>();
      while (thirdLevelItr.hasNext()) {
        // /book/firstlevel/item0/secondlevel/item0/thirdlevel/item0..
        Resource thirdItemsRes = thirdLevelItr.next();
        Map<String, Object> resultantMap = prepareLevelWiseDto(thirdItemsRes, THIRD_LEVEL_LINK_PROPERTY);
        boolean accessCheck = (boolean) resultantMap.get(HAS_ACCESS_KEY);
        if (accessCheck && resultantMap.containsKey(LEVEL_DTO_KEY)) {
          thirdlist.add((BookDto) resultantMap.get(LEVEL_DTO_KEY));
        } else {
          break;
        }
      }
      secLevelDto.setChildLevelList(thirdlist);
    }
    firstLevelDto.getChildLevelList().add(secLevelDto);

  }

  /**
   * Prepare level wise DTO. This method is utilized across all levels and is
   * accountable for generating a DTO at each level, taking into consideration the
   * authorization criteria.
   *
   * @param givenItemResource the given item resource
   * @param linkPropertyName  the link property name
   * @return the map
   */
  Map<String, Object> prepareLevelWiseDto(Resource givenItemResource, String linkPropertyName) {
    Map<String, Object> map = new HashMap<>();
    BookDto levelDto = new BookDto();
    String givenLevelPageLink = fetchAuthoredPath(givenItemResource, linkPropertyName);
    boolean checkAccess = !PageUtils.isPublishInstance(instance)
        || userGroupService.hasAccessToViewLink(givenLevelPageLink, request);
    map.put(HAS_ACCESS_KEY, checkAccess);
    if (checkAccess) {
      String givenLinkPageTitle = getBookPageTitle(givenLevelPageLink);
      if (StringUtils.isNotBlank(givenLinkPageTitle)) {
        levelDto.setHeadingLink(String.format(STR_CONCAT_FORMAT, givenLevelPageLink));
      } else {
        levelDto.setHeadingLink(givenLevelPageLink);
      }
      levelDto.setHeadingTitle(givenLinkPageTitle);
      map.put(LEVEL_DTO_KEY, levelDto);
    }
    return map;
  }

  /**
   * Fetch authored path.
   *
   * @param itemResource the item resource
   * @param propName     the prop name
   * @return the string
   */
  private String fetchAuthoredPath(Resource itemResource, String propName) {
    ValueMap properties = Objects.requireNonNull(itemResource.adaptTo(ValueMap.class));
    return properties.get(propName, "");
  }
}