package com.workday.community.aem.core.models.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.Template;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.SearchApiConfigService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.services.UserService;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static junit.framework.Assert.assertNotNull;
import static junitx.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Session;

import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTENT_TYPE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.PAGE_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_NUMBER;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.CONTACT_ROLE;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_ID;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_NAME;
import static com.workday.community.aem.core.constants.AdobeAnalyticsConstants.ACCOUNT_TYPE;
import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;

/**
 * The Class HeaderModelImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
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

  @Mock
  UserService userService;

  /**
   * RunModeConfig service.
   */
  @Mock
  RunModeConfigService runModeConfigService;

  /**
   * SearchApiConfig service.
   */
  @Mock
  SearchApiConfigService searchApiConfigService;

  @Mock
  ResourceResolver resolver;

  @Spy
  @InjectMocks
  MockSlingHttpServletRequest request = context.request();

  @Mock
  Session jcrSession;

  /**
   * Set up method for test run.
   */
  @BeforeEach
  public void setup() {
    context.addModelsForClasses(HeaderModelImpl.class);
    context.registerService(SnapService.class, snapService, SERVICE_RANKING, Integer.MAX_VALUE);
    context.registerService(Page.class, currentPage);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    context.registerService(SearchApiConfigService.class, searchApiConfigService);
    context.registerService(UserService.class, userService);
  }

  /**
   * Test method for getUserHeaderMenu in HeaderModel class.
   */
  @Test
  void testGetUserHeaderMenu() {
    HeaderModel headerModel = request.adaptTo(HeaderModel.class);
    assertNotNull(headerModel);
    assertEquals("{\"unAuthenticated\": true}", headerModel.getUserHeaderMenus());
    when(request.getResourceResolver()).thenReturn(resolver);
    when(resolver.adaptTo(Session.class)).thenReturn(jcrSession);
    lenient().when(snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER)).thenReturn("");
    lenient().when(snapService.enableCache()).thenReturn(true);
    headerModel = request.adaptTo(HeaderModel.class);
    assertNotNull(headerModel);
    assertEquals("", headerModel.getUserHeaderMenus());
  }

  /**
   * Test method for getDataLayerData in HeaderModel class.
   */
  @Test
  void testGetDataLayerData() {
    // Case 1: return data.
    JsonObject digitalData = new JsonObject();
    JsonObject userProperties = new JsonObject();
    JsonObject orgProperties = new JsonObject();
    JsonObject pageProperties = new JsonObject();
    String contactRole = "Training Coordinator; Named Support Contact; Community Org Administrator";
    String contactNumber = "45689";
    String accountName = "McKee Foods Corporation";
    String accountId = "123";
    String accountType = "customer";
    String title = "FAQ";
    Gson gson = new Gson();
    userProperties.addProperty(CONTACT_ROLE, contactRole);
    userProperties.addProperty(CONTACT_NUMBER, contactNumber);
    orgProperties.addProperty(ACCOUNT_ID, accountId);
    orgProperties.addProperty(ACCOUNT_NAME, accountName);
    orgProperties.addProperty(ACCOUNT_TYPE, accountType);
    pageProperties.addProperty(CONTENT_TYPE, title);
    pageProperties.addProperty(PAGE_NAME, title);
    digitalData.add("user", userProperties);
    digitalData.add("org", orgProperties);
    digitalData.add("page", pageProperties);
    String digitalDataString = String.format("{\"%s\":%s}", "digitalData", gson.toJson(digitalData));

    HeaderModel headerModel = context.request().adaptTo(HeaderModel.class);
    assertNotNull(headerModel);
    Template template = mock(Template.class);
    lenient().when(template.getPath()).thenReturn("/conf/workday-community/settings/wcm/templates/faq");
    lenient().when(currentPage.getTemplate()).thenReturn(template);
    lenient().when(currentPage.getTitle()).thenReturn(title);
    lenient().when(snapService.getAdobeDigitalData(DEFAULT_SFID_MASTER, title, title)).thenReturn(digitalDataString);
    lenient().when(runModeConfigService.getInstance()).thenReturn("publish");
    String data = headerModel.getDataLayerData();
    assertTrue(data.contains(contactNumber));
    assertTrue(data.contains(contactRole));
    assertTrue(data.contains(accountId));
    assertTrue(data.contains(accountName));
    assertTrue(data.contains(accountType));
    assertTrue(data.contains(title));

    // Case 2: return null.
    lenient().when(runModeConfigService.getInstance()).thenReturn("author");
    assertEquals(null, headerModel.getDataLayerData());
  }

  /**
   * Test method getGlobalSearchURL.
   */
  @Test
  void testGetGlobalSearchURL() {
    String uri = "https://www.resourcecenter.workday.com";
    lenient().when(searchApiConfigService.getGlobalSearchURL()).thenReturn(uri);
    HeaderModel headerModel = context.request().adaptTo(HeaderModel.class);
    assertNotNull(headerModel);
    assertEquals(uri, headerModel.getGlobalSearchURL());
  }
}
