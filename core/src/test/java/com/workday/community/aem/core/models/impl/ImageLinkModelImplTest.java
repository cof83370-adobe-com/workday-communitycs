package com.workday.community.aem.core.models.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.workday.community.aem.core.dto.ImageLinkDto;
import com.workday.community.aem.core.models.ImageLinkModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.utils.PageUtils;
import java.util.List;
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

/**
 * The Class ImageLinkModelImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class ImageLinkModelImplTest {

  /** The context. */
  private final AemContext context = new AemContext();

  /** The request. */
  @Mock
  SlingHttpServletRequest request;

  /** The Constant RESOURCE_PATH. */
  private static final String RESOURCE_PATH = "/content/release-notes-page/jcr:content/root/container/container/container_1959071175/imagelink_1560270299";

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    MockitoAnnotations.openMocks(this);
    context.addModelsForClasses(ImageLinkModelImpl.class);
    context.load().json("/com/workday/community/aem/core/models/impl/ImageLinkModelImplTestData.json", "/content");
    context.registerService(SlingHttpServletRequest.class, request);
    context.build().resource(RESOURCE_PATH, "jcr:title", "resource title").commit();
    context.currentResource(RESOURCE_PATH);
  }

  /**
   * Test get final list.
   *
   * @throws Exception the exception
   */
  @Test
  void testGetFinalList() throws Exception {
    UserGroupService mockGroupService = mock(UserGroupService.class);
    context.registerService(UserGroupService.class, mockGroupService);
    lenient().when(mockGroupService.hasAccessToViewLink(anyString(), any())).thenReturn(true);

    RunModeConfigService runModeConfigService = mock(RunModeConfigService.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");
    try (MockedStatic<PageUtils> pageUtilsMock = mockStatic(PageUtils.class)) {
      pageUtilsMock.when(() -> PageUtils.isPublishInstance(any())).thenReturn(true);
      pageUtilsMock.when(() -> PageUtils.appendExtension("/content/workday-community/en-us/admin-tools"))
          .thenReturn("/content/workday-community/en-us/admin-tools.html");
      pageUtilsMock.when(() -> PageUtils.appendExtension("https://sling.apache.org/documentation/bundles/models.html"))
          .thenReturn("https://sling.apache.org/documentation/bundles/models.html");
      ImageLinkModel imageLinkModel = context.request().adaptTo(ImageLinkModel.class);
      List<ImageLinkDto> imageLinksList = imageLinkModel.getFinalList();
      assertNotNull(imageLinksList);
      assertEquals(2, imageLinksList.size());
      assertEquals("/content/dam/workday-community/en-us/public/cat.jpeg", imageLinksList.get(0).getFileReference());
      assertEquals("alt text", imageLinksList.get(0).getImageAltText());
      assertEquals("internal link", imageLinksList.get(0).getLinkText());
      assertEquals("_blank", imageLinksList.get(0).getNewTab());
      assertEquals("/content/workday-community/en-us/admin-tools.html", imageLinksList.get(0).getPagePath());
      assertEquals("https://sling.apache.org/documentation/bundles/models.html", imageLinksList.get(1).getPagePath());
      assertEquals(
          "ImageLinkDto(fileReference=/content/dam/workday-community/en-us/public/cat.jpeg, imageAltText=alt text, linkText=internal link, newTab=_blank, pagePath=/content/workday-community/en-us/admin-tools.html)",
          imageLinksList.get(0).toString());
    }
  }
}
