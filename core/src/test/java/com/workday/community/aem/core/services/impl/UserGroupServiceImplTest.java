package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.config.CacheConfig;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.DamUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class UserGroupServiceImplTest {
  @Mock
  SlingHttpServletRequest request;

  @Mock
  UserServiceImpl userService;

  @Mock
  DrupalService drupalService;

  /**
   * The SnapConfig.
   */
  @Mock
  SnapConfig config;

  /**
   * The UserGroupServiceImpl service.
   */
  @InjectMocks
  UserGroupServiceImpl userGroupService;

  @Mock
  ResourceResolverFactory resResolverFactory;

  CacheManagerServiceImpl cacheManager;

  @Mock
  ResourceResolver mockResolver;

  MockedStatic<CommonUtils> mockCommonUtils;

  MockedStatic<DamUtils> mockDamUtils;

  Resource mockResource;

  Node mockNode;

  @BeforeEach
  public void setUp() throws Exception {
    cacheManager = new CacheManagerServiceImpl();
    CacheConfig cacheConfig = TestUtil.getCacheConfig();
    cacheManager.activate(cacheConfig);
    cacheManager.setResourceResolverFactory(resResolverFactory);
    userGroupService.setCacheManager(cacheManager);

    mockCommonUtils = mockStatic(CommonUtils.class);
    mockDamUtils = mockStatic(DamUtils.class);
    mockResource = mock(Resource.class);
    mockNode = mock(Node.class);
  }

  @Test
  void getUserGroupsBySfIdUser() {
    String SF_ID = "test=123";
    String userDataResponse =
        "{\"roles\":[\"role1\",\"role2\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"role1\",\"role2\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
    when(drupalService.getUserData(SF_ID)).thenReturn(userDataResponse);
    try (MockedStatic<OurmUtils> mockOurmUtils = mockStatic(OurmUtils.class)) {
      mockOurmUtils.when(() -> OurmUtils.getSalesForceId(any(), any())).thenReturn(SF_ID);
      List<String> testAemGroups = List.of("role1", "role2");
      assertEquals(testAemGroups, userGroupService.getCurrentUserGroups(request));
    }
  }

  @Test
  void testCustomerRoles() throws NoSuchFieldException, IllegalAccessException {
    String SF_ID = "test=123";
    String userDataResponse =
        "{\"roles\":[\"authenticated\",\"customer_adaptive\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
    when(drupalService.getUserData(SF_ID)).thenReturn(userDataResponse);
    List<String> groups = userGroupService.getUserGroupsFromDrupal(SF_ID);
    assertTrue(groups.contains("authenticated"));
    assertTrue(groups.contains("customer_adaptive"));

  }

  @Test
  void testPartnerRoles() throws NoSuchFieldException, IllegalAccessException {
    String SF_ID = "test=123";
    String userDataResponse =
        "{\"roles\":[\"authenticated\",\"partner_all\",\"partner_innovation_track\",\"partner_sales_track\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"Authenticated\",\"Internal - Workmates\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
    when(drupalService.getUserData(SF_ID)).thenReturn(userDataResponse);
    List<String> groups = userGroupService.getUserGroupsFromDrupal(SF_ID);
    assertTrue(groups.contains("partner_all"));
    assertTrue(groups.contains("partner_innovation_track"));
    assertTrue(groups.contains("partner_sales_track"));
  }

  @Test
  void testCheckLoggedInUserHasAccessControlTags()
      throws IllegalStateException, RepositoryException, CacheException, LoginException {
    String SF_ID = "test=123";
    String userDataResponse =
        "{\"roles\":[\"role1\",\"role2\"],\"profileImage\":\"data:image/jpeg;base64,\",\"adobe\":{\"user\":{\"contactNumber\":\"0034X00002xaPU2QAM\",\"contactRole\":[\"role1\",\"role2\"],\"isNSC\":false,\"timeZone\":\"America/Los_Angeles\"},\"org\":{\"accountId\": \"aEB4X0000004CfdWAE\",\"accountName\":\"Workday\",\"accountType\":\"workmate\"}}}";
    when(drupalService.getUserData(SF_ID)).thenReturn(userDataResponse);
    try (MockedStatic<OurmUtils> mockOurmUtils = mockStatic(OurmUtils.class)) {
      mockOurmUtils.when(() -> OurmUtils.getSalesForceId(any(), any())).thenReturn(SF_ID);
      List<String> testAemGroups = List.of("role1", "role2");
      assertTrue(userGroupService.validateCurrentUser(request, testAemGroups));
    }
  }

  @AfterEach
  public void after() {
    mockResolver.close();
    mockCommonUtils.close();
    mockDamUtils.close();
  }
}
