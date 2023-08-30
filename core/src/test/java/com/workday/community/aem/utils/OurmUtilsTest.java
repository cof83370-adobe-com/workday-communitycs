package com.workday.community.aem.utils;

import com.workday.community.aem.core.TestUtil;
import com.workday.community.aem.core.services.JcrUserService;
import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.workday.community.aem.core.constants.SnapConstants.DEFAULT_SFID_MASTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith({MockitoExtension.class})
public class OurmUtilsTest {
  SlingHttpServletRequest request;

  private UserManager userManager;

  private JcrUserService userService;

  @BeforeEach
  public void setup() {
    request = mock(SlingHttpServletRequest.class);
    userManager = mock(UserManager.class);
  }

  @Test
  public void testGetSalesForceIdDefaultMaster() throws Exception {
    // case 1:
    lenient().when(userManager.getAuthorizable(anyString())).thenThrow(new RuntimeException());
    String sfId = OurmUtils.getSalesForceId(request, userService);
    assertEquals(DEFAULT_SFID_MASTER, sfId);
  }

  @Test
  public void testGetSalesForceIdMock() throws Exception {
    // case 2:
    User user = TestUtil.getMockUser();
    lenient().when(userManager.getAuthorizable(anyString())).thenReturn(user);

    JcrUserService userService = mock(JcrUserService.class);
    lenient().when(userService.getCurrentUser(request)).thenReturn(user);
    String testSfId = OurmUtils.getSalesForceId(request, userService);
    assertEquals("testSfId", testSfId);
  }
}
