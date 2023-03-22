package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static junit.framework.Assert.assertNotNull;
import static junitx.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.workday.community.aem.core.constants.GlobalConstants.WRCConstants.DEFAULT_SFID_MASTER;

/**
 * The Class HeaderModelImplTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class HeaderModelImplTest {

  /**
   * AemContext
   */
  private final AemContext context = new AemContext();

  /**
   * MenuApiService object
   */
  @Mock
  SnapService snapService;

  /**
   * Set up method for test run.
   */
  @BeforeEach
  public void setup() {
    context.addModelsForClasses(HeaderModelImpl.class);
    context.registerService(SnapService.class, snapService, SERVICE_RANKING, Integer.MAX_VALUE);
  }

  /**
   * Test method for getUserHeaderMenu in NavHeaderModel class.
   */
  @Test
  void testGetUserHeaderMenu() {
    lenient().when(snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER)).thenReturn("");
    HeaderModel navModel = context.request().adaptTo(HeaderModel.class);
    assertNotNull(navModel);
    assertEquals("", navModel.getUserHeaderMenus());
  }

  @Test
  void testGetUserAvatarUrl() {
    HeaderModel navModel = context.request().adaptTo(HeaderModel.class);

    // Case 1: Mock return with format
    ProfilePhoto ret = new ProfilePhoto();
    ret.setPhotoVersionId("fooVersion");
    ret.setBase64content("data:image/xxx");

    lenient().when(snapService.getProfilePhoto(DEFAULT_SFID_MASTER)).thenReturn(ret);
    assertNotNull(navModel);
    assertEquals("data:image/xxx", navModel.getUserAvatarUrl());

    // Case 2: Real Mock return with another format
    ret.setBase64content("xxx");
    ret.setFileNameWithExtension("fff.png");
    ret.setBase64content("content");
    assertEquals("data:image/png;base64,content", navModel.getUserAvatarUrl());

    // Case 3: Exception return
    lenient().when(snapService.getProfilePhoto(DEFAULT_SFID_MASTER)).thenThrow(new RuntimeException());
    assertEquals("", navModel.getUserAvatarUrl());
  }
}