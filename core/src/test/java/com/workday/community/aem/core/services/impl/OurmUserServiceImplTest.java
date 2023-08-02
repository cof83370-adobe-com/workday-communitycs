package com.workday.community.aem.core.services.impl;

import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.OurmDrupalConfig;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.OurmUserService;
import com.workday.community.aem.core.utils.RestApiUtil;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

/**
 * The Class OurmUsersApiConfigServiceImplTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class OurmUserServiceImplTest {

  /** The ourmUsers api service. */
  private final OurmUserService ourmUserService = new OurmUserServiceImpl();

  /** The ourmUsers config. */
  private final OurmDrupalConfig ourmDrupalConfig = new OurmDrupalConfig() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return Annotation.class;
    }

    @Override
    public String ourmDrupalRestRoot() {
      return "https://den.community-workday.com";
    }

    @Override
    public String ourmDrupalConsumerKey() {
      return "mockConsumerKey";
    }

    @Override
    public String ourmDrupalConsumerSecret() {
      return "mockConsumerSecret";
    }

    @Override
    public String ourmDrupalUserSearchPath() {
      return "/user/search";
    }
  };

  /**
   * Setup.
   */
  @BeforeEach
  public void setup() {
    ((OurmUserServiceImpl) ourmUserService).activate(ourmDrupalConfig);
  }

  /**
   * fetchOurmUsertest
   *
   * @throws OurmException the ourm exception
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  @Test
  public void fetchOurmUsertest() throws OurmException, IOException {
    String searchText = "fakeString";
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      String testUserContext = "{\"users\":[{\"sfId\":\"fakeSfId\",\"username\":\"fakeUserName\",\"firstName\":\"fake_first_name\",\"lastName\":\"fake_last_name\",\"email\":\"fakeEmail\",\"profileImageData\":\"fakeProfileData\"}]}";

      mocked.when(() -> RestApiUtil.doOURMGet(anyString(), anyString())).thenReturn(testUserContext);

      JsonObject ret = this.ourmUserService.searchOurmUserList(searchText);
      assertEquals(testUserContext, ret.toString());
    }
  }

/**
   * FetchOurmUserWithSpacetest
   *
   * @throws OurmException the ourm exception
   * @throws IOException   Signals that an I/O exception has occurred.
   */
  @Test
  public void fetchOurmUserWithSpacetest() throws OurmException, IOException {
    String searchText = "fake String";
    try (MockedStatic<RestApiUtil> mocked = mockStatic(RestApiUtil.class)) {
      String testUserContext = "{\"users\":[{\"sfId\":\"fakeSfId\",\"username\":\"fakeUserName\",\"firstName\":\"fake_first_name\",\"lastName\":\"fake_last_name\",\"email\":\"fakeEmail\",\"profileImageData\":\"fakeProfileData\"}]}";

      mocked.when(() -> RestApiUtil.doOURMGet(anyString(), anyString())).thenReturn(testUserContext);

      JsonObject ret = this.ourmUserService.searchOurmUserList(searchText);
      assertEquals(testUserContext, ret.toString());
    }
  }
}