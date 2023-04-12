package com.workday.community.aem.core.services.impl;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.pojos.restclient.APIResponse;
import com.workday.community.aem.core.services.SnapService;

import com.workday.community.aem.core.utils.RestApiUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class SnapServiceImplTest {

  /**
   * AemContext
   */
  private final AemContext context = new AemContext();

  @Mock
  ResourceResolverFactory resResolverFactory;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private GetSnapConfig snapConfig;

  SnapService snapService;

  Resource resource;

  @BeforeEach
  public void setup() {
    context.registerService(ResourceResolverFactory.class, resResolverFactory);
    context.registerService(objectMapper);

    snapService = new SnapServiceImpl();
    resource = mock(Resource.class);
    snapService.setResourceResolverFactory(resResolverFactory);

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

      public String sfdcUserAvatarApiKey() {
        return "testSfdcUserAvatarApiKey";
      }

      @Override
      public String navFallbackMenuData() {
        return "/content/dam/workday-community/resources/local-header-data.json";
      }

      @Override
      public String navFallbackMenuServiceUser() {
        return "navserviceuser";
      }

      @Override
      public boolean beta() {
        return true;
      }
    };
  }

  @Test
  public void testGetUserHeaderMenu() throws Exception {
    Asset asset = mock(Asset.class);
    Rendition original = mock(Rendition.class);
    ResourceResolver resolverMock = mock(ResourceResolver.class);

    // Case 0 Empty configuration
    snapService.activate(snapConfig.get(0, 0));
    assertEquals("", this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER));

    snapService.activate(snapConfig.get(0, 1));
    assertEquals("", this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER));

    snapService.activate(snapConfig.get(1, 0));
    assertEquals("", this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER));

    // Case 1: no resolver mock
    snapService.activate(snapConfig.get(1, 1));
    String menuData0 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
    assertEquals("", menuData0);

    lenient().when(resolverMock.getResource(any())).thenReturn(resource);
    lenient().when(resResolverFactory.getServiceResourceResolver(any())).thenReturn(resolverMock);

    lenient().when(resource.adaptTo(any())).thenReturn(asset);
    lenient().when(asset.getOriginal()).thenReturn(original);

    // Case 2 No content
    snapService.activate(snapConfig.get(1, 2));
    String menuData1 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
    assertEquals("", menuData1);

    // Case 3 With Mock Content
    ByteArrayInputStream content = getTestContent("/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
    lenient().when(original.adaptTo(any())).thenReturn(content);

    // Case 4 With mock content for default fallback
    snapService.activate(snapConfig.get(2, 1));
    String menuData2 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
    assertEquals(16756, menuData2.length());

    //Case 4: With mock content for Request call.
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      APIResponse response = mock(APIResponse.class);
      mocked.when(() -> RestApiUtil.doGetMenu(anyString(), anyString(), anyString(), anyString())).thenReturn(response);
      when(response.getResponseBody()).thenReturn(menuData2);
      snapService.activate(snapConfig.get(2, 2));
      String menuData3 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals(menuData2, menuData3);
    }
  }

  @Test
  public void testGetUserHeaderMenuWithException() {
    Rendition original = mock(Rendition.class);

    snapService.activate(snapConfig.get(1, 1));
    ByteArrayInputStream content = getTestContent("/com/workday/community/aem/core/models/impl/FailStateHeaderTestData.json");
    lenient().when(original.adaptTo(any())).thenReturn(content);

    // Case 4 With mock content for default fallback
    String menuData2 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);

    //Case 4: With mock content for Request call.
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      APIResponse response = mock(APIResponse.class);
      mocked.when(() -> RestApiUtil.doGetMenu(anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException());
      lenient().when(response.getResponseBody()).thenReturn(menuData2);
      String menuData3 = this.snapService.getUserHeaderMenu(DEFAULT_SFID_MASTER);
      assertEquals("", menuData3);
    }
  }


  @Test
  public void testGetProfilePhoto() throws Exception {
    // Case 1: No return from failed call
    snapService.activate(snapConfig.get(1, 1));
    assertNull(this.snapService.getProfilePhoto(DEFAULT_SFID_MASTER));

    // Case 2: return from mocked call.
    ProfilePhoto retObj = new ProfilePhoto();
    retObj.setDescription("test");
    retObj.setPhotoVersionId("1.1");
    retObj.setFileNameWithExtension("foo.png");
    retObj.setBase64content("test fdfdf");

    String mockRet = objectMapper.writeValueAsString(retObj);

    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString())).thenReturn(mockRet);
      ProfilePhoto photoObj = this.snapService.getProfilePhoto(DEFAULT_SFID_MASTER);
      assertEquals(retObj.getBase64content(), photoObj.getBase64content());
    }
  }

  @Test
  public void testGetProfilePhotoWithException() {
    snapService.activate(snapConfig.get(1, 2));
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString())).thenThrow(new RuntimeException());
      assertNull(this.snapService.getProfilePhoto(DEFAULT_SFID_MASTER));
    }
  }

  @Test
  public void testGetUserContext() {
    snapService.activate(snapConfig.get(1, 1));
    try(MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      String testUserContext = "{\"email\":\"foo@workday.com\"}";

      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString())).thenReturn(testUserContext);

      JsonObject ret = this.snapService.getUserContext(DEFAULT_SFID_MASTER);
      assertEquals(testUserContext, ret.toString());
    }
  }

  @Test
  public void testGetUserContextWithException() {
    snapService.activate(snapConfig.get(1, 1));
    try(MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      mocked.when(() -> RestApiUtil.doSnapGet(anyString(), anyString(), anyString())).thenThrow(new RuntimeException());
      JsonObject ret = this.snapService.getUserContext(DEFAULT_SFID_MASTER);
      assertNull(ret);
    }
  }

  private ByteArrayInputStream getTestContent(String jsonFile) {
    InputStream inputStream = getClass().getResourceAsStream(jsonFile);

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
