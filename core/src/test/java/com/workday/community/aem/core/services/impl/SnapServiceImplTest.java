package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.pojos.restclient.ApiResponse;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.utils.RestApiUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class SnapServiceImplTest {

  /**
   * AemContext
   */
  private final AemContext context = new AemContext();

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  ResourceResolverFactory resResolverFactory;

  @Mock
  RunModeConfigService runModeConfigService;

  Resource resource;

  private GetSnapConfig snapConfig;

  private SnapServiceImpl snapService;

  private CacheManagerServiceImpl cacheManagerService;

  @BeforeEach
  public void setup() throws CacheException, LoginException {
    cacheManagerService = new CacheManagerServiceImpl();
    CacheConfig cacheConfig = TestUtil.getCacheConfig();
    cacheManagerService.activate(cacheConfig);
    cacheManagerService.setResourceResolverFactory(resResolverFactory);

    if (snapService == null) {
      snapService = new SnapServiceImpl();
      snapService.setCacheManagerService(cacheManagerService);
      snapService.setRunModeConfigService(runModeConfigService);
    }

    context.registerService(ResourceResolverFactory.class, resResolverFactory);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    context.registerService(objectMapper);
    context.registerService(cacheManagerService);
    resource = mock(Resource.class);

    snapConfig = (x, y) -> new SnapConfig() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return null;
      }

      @Override
      public String snapUrl() {
        return x == 0 ? null : x == 1 ? "http://test/snap" : "http://test/snap/";
      }

      public String snapContextPath() {
        return "http://testContextendpoint";
      }

      @Override
      public String navApi() {
        return y == 0 ? null : y == 1 ? "/menu" : "/menu/";
      }

      @Override
      public String navApiKey() {
        return "testApiKey";
      }

      @Override
      public String navApiToken() {
        return "testApiToken";
      }

      @Override
      public String snapContextApiToken() {
        return "snapContextApiTokenTest";
      }

      @Override
      public String sfdcUserAvatarUrl() {
        return y == 0 ? null : y == 1 ? "/avatar" : "/avatar/";
      }

      public String sfdcUserAvatarToken() {
        return "TestPhotoToken";
      }

      public String snapContextApiKey() {
        return "testSnapContextApiKey";
      }

      @Override
      public String sfToAemUserGroupMap() {
        return "";
      }

      @Override
      public String sfdcUserAvatarApiKey() {
        return "testSfdcUserAvatarApiKey";
      }

      @Override
      public String navFallbackMenuData() {
        return "/content/dam/workday-community/resources/local-header-data.json";
      }

      @Override
      public boolean beta() {
        return true;
      }

      @Override
      public String snapProfilePath() {
        return "snapProfilePath";
      }

      @Override
      public String snapProfileApiToken() {
        return "snapProfileApiToken";
      }

      @Override
      public String snapProfileApiKey() {
        return "snapProfileApiKey";
      }

      @Override
      public String userProfileUrl() {
        return "userProfileURL";
      }

      public boolean enableCache() {
        return true;
      }
    };
  }

  @Test
  public void testGetUserHeaderMenu() {
    Asset asset = mock(Asset.class);
    Rendition original = mock(Rendition.class);
    ResourceResolver resolverMock = mock(ResourceResolver.class);
    try (MockedStatic<ResolverUtil> staticMock = mockStatic(ResolverUtil.class);
         MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      staticMock.when(() -> ResolverUtil.newResolver(eq(resResolverFactory), anyString()))
          .thenReturn(resolverMock);
      lenient().when(resolverMock.getResource(any())).thenReturn(resource);
      lenient().when(resource.adaptTo(any())).thenReturn(asset);
      lenient().when(asset.getOriginal()).thenReturn(original);

      // Mock Content
      ByteArrayInputStream content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);

      // Case 0 Empty configuration
      snapService.activate(snapConfig.get(0, 0));
      String menuData = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(16635, menuData.length());
      cacheManagerService.invalidateCache();

      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);
      snapService.activate(snapConfig.get(0, 1));
      menuData = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(16635, menuData.length());
      cacheManagerService.invalidateCache();

      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);
      snapService.activate(snapConfig.get(1, 0));
      menuData = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(16635, menuData.length());
      cacheManagerService.invalidateCache();

      // Case 1: no resolver mock
      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);
      snapService.activate(snapConfig.get(1, 1));
      String menuData0 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(16635, menuData0.length());
      cacheManagerService.invalidateCache();

      // Case 2 No content
      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);
      snapService.activate(snapConfig.get(1, 2));
      String menuData1 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(16635, menuData1.length());
      cacheManagerService.invalidateCache();

      // Case 3 With mock content for default fallback
      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);
      snapService.activate(snapConfig.get(2, 1));
      String menuData2 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(16635, menuData2.length());
      cacheManagerService.invalidateCache();

      // Case 4: With mock content for Request call.
      ApiResponse response = new ApiResponse();
      mocked.when(() -> RestApiUtil.doMenuGet(anyString(), anyString(),
          anyString(), anyString())).thenReturn(response);

      snapService.activate(snapConfig.get(2, 2));
      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);
      lenient().when(runModeConfigService.getEnv()).thenReturn("prod");
      String menuData3 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(menuData2, menuData3);
      cacheManagerService.invalidateCache();

      // Case 5 With contact information
      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);
      Gson gson = new Gson();
      JsonObject sfMenu = gson.fromJson(menuData2, JsonObject.class);
      JsonObject profileObject = new JsonObject();
      profileObject.addProperty("firstName", "Justin");
      profileObject.addProperty("lastName", "Zhang");
      sfMenu.add("profile", profileObject);

      JsonObject contactInfo = new JsonObject();
      contactInfo.addProperty("firstName", "Justin");
      contactInfo.addProperty("lastName", "Zhang");
      sfMenu.add("contactInformation", contactInfo);

      response.setResponseCode(HttpStatus.SC_OK);
      response.setResponseBody(gson.toJson(sfMenu));
      String menuData4 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(gson.fromJson(sfMenu, JsonObject.class).size(),
          gson.fromJson(menuData4, JsonObject.class).size());
    }
  }

  @Test
  public void testGetUserHeaderMenuWithException() throws Exception {
    Asset asset = mock(Asset.class);
    Rendition original = mock(Rendition.class);
    ResourceResolver resolverMock = mock(ResourceResolver.class);
    try (MockedStatic<ResolverUtil> staticMock = mockStatic(ResolverUtil.class);
         MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      staticMock.when(() -> ResolverUtil.newResolver(eq(resResolverFactory), anyString()))
          .thenReturn(resolverMock);
      lenient().when(resolverMock.getResource(any())).thenReturn(resource);
      lenient().when(resource.adaptTo(any())).thenReturn(asset);
      lenient().when(asset.getOriginal()).thenReturn(original);

      // Mock Content
      ByteArrayInputStream content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);

      // Empty configuration
      snapService.activate(snapConfig.get(0, 0));
      String menuData = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      resolverMock = mock(ResourceResolver.class);
      lenient().when(resolverMock.getResource(any())).thenReturn(resource);
      lenient().when(resResolverFactory.getServiceResourceResolver(any())).thenReturn(resolverMock);

      asset = mock(Asset.class);
      lenient().when(resource.adaptTo(any())).thenReturn(asset);

      original = mock(Rendition.class);
      lenient().when(asset.getOriginal()).thenReturn(original);

      content = getTestContent(
          "/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
      lenient().when(original.adaptTo(any())).thenReturn(content);

      // With url and exception
      snapService.activate(snapConfig.get(1, 1));
      mocked.when(() -> RestApiUtil.doMenuGet(anyString(), anyString(),
              anyString(), anyString()))
          .thenThrow(new SnapException("test fails"));

      String menuData2 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(menuData, menuData2);
    }
  }

  @Test
  public void testGetProfilePhoto() {
    // Case 1: No return from failed call
    snapService.activate(snapConfig.get(1, 1));
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    HttpClientBuilder builder = mock(HttpClientBuilder.class);

    try (MockedStatic<HttpClients> MockedHttpClients = mockStatic(HttpClients.class);
         MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      MockedHttpClients.when(HttpClients::custom).thenReturn(builder);
      lenient().when(builder.build()).thenReturn(httpClient);
      assertNull(this.snapService.getProfilePhoto(DEFAULT_SFID_MASTER));

      // Case 2: return from mocked call.
      ProfilePhoto retObj = new ProfilePhoto();
      retObj.setPhotoVersionId("1.1");
      retObj.setFileNameWithExtension("foo.png");
      retObj.setBase64content("test fdfdf");
      retObj.setSuccess("true");

      String mockRet = "test fdfdf";

      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString()))
          .thenReturn(mockRet);
      this.snapService.getProfilePhoto(DEFAULT_SFID_MASTER);
      assertEquals(retObj.getBase64content(), "test fdfdf");
    }
  }

  @Test
  public void testGetProfilePhotoWithException() {
    snapService.activate(snapConfig.get(1, 2));
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString()))
          .thenThrow(new SnapException("test fails"));
      assertNull(this.snapService.getProfilePhoto(DEFAULT_SFID_MASTER));
    }
  }

  @Test
  public void testGetUserContext() {
    snapService.activate(snapConfig.get(1, 1));
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      String testUserContext = "{\"email\":\"foo@workday.com\"}";

      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString()))
          .thenReturn(testUserContext);

      JsonObject ret = this.snapService.getUserContext(DEFAULT_SFID_MASTER);
      assertEquals(testUserContext, ret.toString());
    }
  }

  @Test
  public void testGetUserContextWithException() {
    snapService.activate(snapConfig.get(1, 1));
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString()))
          .thenThrow(new SnapException("test fails"));
      JsonObject ret = this.snapService.getUserContext(DEFAULT_SFID_MASTER);
      assertEquals(ret, new JsonObject());
    }
  }

  @Test
  public void testGetAdobeDigitalData() {
    snapService.activate(snapConfig.get(1, 1));
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      String profileString =
          "{\"contactRole\":\"Workday\", \"contactNumber\":\"123\", \"wrcOrgId\":\"456\", \"organizationName\":\"Test organization\", \"isWorkmate\":true}";

      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString()))
          .thenReturn(profileString);

      String ret = this.snapService.getUserProfile(DEFAULT_SFID_MASTER);
      assertEquals(profileString, ret);
      String pageTitle = "FAQ page";
      String contentType = "FAQ";
      String contactNumber = "123";
      String organizationName = "Test organization";
      String adobeData =
          snapService.getAdobeDigitalData(DEFAULT_SFID_MASTER, pageTitle, contentType);
      assertTrue(adobeData.contains(pageTitle));
      assertTrue(adobeData.contains(contentType));
      assertTrue(adobeData.contains(pageTitle));
      assertTrue(adobeData.contains(contactNumber));
      assertTrue(adobeData.contains(organizationName));

      String adobeData1 =
          snapService.getAdobeDigitalData(DEFAULT_SFID_MASTER, pageTitle, contentType);
      assertEquals(adobeData, adobeData1);
    }
  }

  @Test
  public void testGetAdobeDigitalDataWithException() {
    snapService.activate(snapConfig.get(1, 1));
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString()))
          .thenThrow(new SnapException("test fails"));
      String ret = this.snapService.getUserProfile(DEFAULT_SFID_MASTER);
      assertNull(ret);
      String pageTitle = "FAQ page";
      String contentType = "FAQ";
      String adobeData =
          snapService.getAdobeDigitalData(DEFAULT_SFID_MASTER, pageTitle, contentType);
      assertTrue(adobeData.contains(pageTitle));
      assertTrue(adobeData.contains(contentType));
    }
  }

  private ByteArrayInputStream getTestContent(String jsonFile) {
    InputStream inputStream = getClass().getResourceAsStream(jsonFile);
    assert inputStream != null;

    byte[] buffer = new byte[1024];
    int length;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      while ((length = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, length);
      }
    } catch (IOException e) {
      // handle the exception
    } finally {
      try {
        inputStream.close();
        outputStream.close();
      } catch (IOException e) {
        // handle the exception
      }
    }

    byte[] byteArray = outputStream.toByteArray();

    return new ByteArrayInputStream(byteArray);
  }
}
