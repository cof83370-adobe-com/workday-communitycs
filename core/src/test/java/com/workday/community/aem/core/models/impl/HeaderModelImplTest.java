package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SnapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static junit.framework.Assert.assertNotNull;
import static junitx.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.util.HashMap;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;

/**
 * The Class HeaderModelImplTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class HeaderModelImplTest {

  /**
   * AemContext.
   */
  private final AemContext context = new AemContext();

  /**
   * MenuApiService object.
   */
  @Mock
  SnapService snapService;

  /**
   * Page service.
   */
  @Mock
  Page currentPage;

  /**
   * RunModeConfig service.
   */
  @Mock
  RunModeConfigService runModeConfigService;

  /**
   * Set up method for test run.
   */
  @BeforeEach
  public void setup() {
    context.addModelsForClasses(HeaderModelImpl.class);
    context.registerService(SnapService.class, snapService, SERVICE_RANKING, Integer.MAX_VALUE);
    context.registerService(Page.class, currentPage);
    context.registerService(RunModeConfigService.class, runModeConfigService);
  }

  /**
   * Test method for getUserHeaderMenu in HeaderModel class.
   */
  @Test
  void testGetUserHeaderMenu() {
    lenient().when(snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER)).thenReturn("");
    HeaderModel headerModel = context.request().adaptTo(HeaderModel.class);
    assertNotNull(headerModel);
    assertEquals("", headerModel.getUserHeaderMenus());
  }

  /**
   * Test method for getUserAvatarUrl in HeaderModel class.
   */
  @Test
  void testGetUserAvatarUrl() {
    HeaderModel headerModel = context.request().adaptTo(HeaderModel.class);

    // Case 1: Mock return with format
    ProfilePhoto ret = new ProfilePhoto();
    ret.setPhotoVersionId("fooVersion");
    ret.setBase64content("data:image/xxx");

    lenient().when(snapService.getProfilePhoto(DEFAULT_SFID_MASTER)).thenReturn(ret);
    assertNotNull(headerModel);
    assertEquals("data:image/xxx", headerModel.getUserAvatar());

    // Case 2: Real Mock return with another format
    ret.setBase64content("xxx");
    ret.setFileNameWithExtension("fff.png");
    ret.setBase64content("content");
    assertEquals("data:image/png;base64,content", headerModel.getUserAvatar());

    // Case 3: Exception return
    lenient().when(snapService.getProfilePhoto(DEFAULT_SFID_MASTER)).thenThrow(new RuntimeException());
    assertEquals("", headerModel.getUserAvatar());
  }

  /**
   * Test method for getDataLayerData in HeaderModel class.
   */
  @Test
  void testGetDataLayerData() {
    // Case 1: return data.
    HashMap<String, Object> digitalData = new HashMap<String, Object>();
    HashMap<String, String> userProperties = new HashMap<String, String>();
    String contactRole = "Training Coordinator; Named Support Contact; Community Org Administrator";
    userProperties.put("contactNumber", "100001210867");
    userProperties.put("contactRole", contactRole);
    digitalData.put("user", userProperties);

    HashMap<String, String> orgProperties = new HashMap<String, String>();
    String accountName = "McKee Foods Corporation";
    orgProperties.put("accountName", accountName);
    digitalData.put("org", orgProperties);
    
    HeaderModel headerModel = context.request().adaptTo(HeaderModel.class);
    assertNotNull(headerModel);
    Template template = mock(Template.class);
    String title = "FAQ";
    lenient().when(template.getPath()).thenReturn("/conf/workday-community/settings/wcm/templates/faq");
    lenient().when(currentPage.getTemplate()).thenReturn(template);
    lenient().when(currentPage.getTitle()).thenReturn(title);
    lenient().when(snapService.getAdobeDigitalData(anyString())).thenReturn(digitalData);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");
    String data = headerModel.getDataLayerData();
    assertTrue(data.contains(title));
    assertTrue(data.contains(contactRole));
    assertTrue(data.contains(accountName));

    // Case 2: return null.
    lenient().when(runModeConfigService.getInstance()).thenReturn("author");
    assertEquals(null, headerModel.getDataLayerData());
  }
}