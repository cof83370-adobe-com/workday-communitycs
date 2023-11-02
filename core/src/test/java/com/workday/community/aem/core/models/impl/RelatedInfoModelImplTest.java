package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.workday.community.aem.core.dto.RelatedInfoDto;
import com.workday.community.aem.core.models.RelatedInfoModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.PageUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.jackrabbit.api.security.user.User;

/**
 * The Class RelatedInfoModelImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class RelatedInfoModelImplTest {

  /** The context. */
  private final AemContext context = new AemContext();

  /** The request. */
  @Mock
  SlingHttpServletRequest request;

  /** The Constant RESOURCE_PATH. */
  private static final String RESOURCE_PATH = "/content/relatedInfo-test-page/jcr:content/root/container/container/container_1959071175/relatedinformation_1318845220";

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    MockitoAnnotations.openMocks(this);
    context.addModelsForClasses(RelatedInfoModelImpl.class);
    context.load().json("/com/workday/community/aem/core/models/impl/RelatedInfoModelImplTestData.json", "/content");
    context.registerService(SlingHttpServletRequest.class, request);
  }

  /**
   * Test get final list.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetFinalList() throws Exception {
    context.build().resource(RESOURCE_PATH, "jcr:title", "resource title").commit();
    context.currentResource(RESOURCE_PATH);

    UserGroupService mockGroupService = mock(UserGroupService.class);
    context.registerService(UserGroupService.class, mockGroupService);
    lenient().when(mockGroupService.validateCurrentUser(any(), anyString())).thenReturn(true);

    RunModeConfigService runModeConfigService = mock(RunModeConfigService.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");

    UserService mockUserervice = mock(UserService.class);
    context.registerService(UserService.class, mockUserervice);
    User mockedUser = mock(User.class);
    lenient().when(mockUserervice.getCurrentUser(any())).thenReturn(mockedUser);
    lenient().when(mockedUser.getID()).thenReturn("User Test");

    try (MockedStatic<PageUtils> pageUtilsMock = mockStatic(PageUtils.class)) {
      pageUtilsMock.when(() -> PageUtils.isPublishInstance(any())).thenReturn(true);

     pageUtilsMock.when(() -> PageUtils.appendExtension("/content/workday-community/en-us/admin-tools/books"))
          .thenReturn("/content/workday-community/en-us/admin-tools/books.html");

      pageUtilsMock.when(() -> PageUtils.appendExtension("https://sling.apache.org/documentation/bundles/models.html"))
          .thenReturn("https://sling.apache.org/documentation/bundles/models.html");

      pageUtilsMock.when(() -> PageUtils.appendExtension("/content/workday-community/en-us/admin-tools/test"))
          .thenReturn("/content/workday-community/en-us/admin-tools/test.html");

      RelatedInfoModel relatedInfoModel = context.request().adaptTo(RelatedInfoModel.class);
      RelatedInfoDto relatedInfoDto = relatedInfoModel.getRelatedInfoDto();

       assertNotNull(relatedInfoDto);
       assertEquals("alt text for curated block", relatedInfoDto.getAltText());
       assertEquals("curated block", relatedInfoDto.getDescription());
       assertEquals("/content/dam/workday-community/en-us/public/cat.jpeg", relatedInfoDto.getFileReference());
       assertEquals("3", relatedInfoDto.getRows());
       assertEquals("footer link text", relatedInfoDto.getFooterLinkText());
       assertEquals("/content/workday-community/en-us/admin-tools/test.html", relatedInfoDto.getFooterLinkUrl());
       assertEquals("_blank", relatedInfoDto.getFooterNewTab());
       assertEquals("false", relatedInfoDto.getDecorative());
       assertEquals("Related Info Block", relatedInfoDto.getHeadingTitle());
       assertEquals("static", relatedInfoDto.getType());
       assertEquals(2, relatedInfoDto.getRelatedInfoItemsList().size());
       assertEquals("link title1", relatedInfoDto.getRelatedInfoItemsList().get(0).getLinkTitle());
       assertEquals("_blank", relatedInfoDto.getRelatedInfoItemsList().get(0).getNewTab());
       assertEquals("/content/workday-community/en-us/admin-tools/books.html", relatedInfoDto.getRelatedInfoItemsList().get(0).getPagePath());
       assertEquals("https://sling.apache.org/documentation/bundles/models.html", relatedInfoDto.getRelatedInfoItemsList().get(1).getPagePath());
       assertEquals(false, relatedInfoDto.isAnonymousUser());
    }
  }
}
