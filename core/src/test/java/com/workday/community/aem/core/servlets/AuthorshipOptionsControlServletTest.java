package com.workday.community.aem.core.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Session;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.apache.sling.servlethelpers.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class AuthorshipOptionsControlServletTest.
 * 
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class AuthorshipOptionsControlServletTest {

  /**
   * The context.
   */
  private final AemContext context = new AemContext();

  /**
   * The authorship render condition model test.
   */
  @InjectMocks
  private AuthorshipOptionsControlServlet authorshipOptionsControlServlet;

  /**
   * The runmode config service.
   */
  @Mock
  RunModeConfigService runModeConfigService;

  /**
   * The runmode config service.
   */
  @Mock
  UserService userService;

  /**
   * The mocked user.
   */
  @Mock
  User user;

  /**
   * The request.
   */
  private MockSlingHttpServletRequest mockSlingRequest;
  
  /**
   * The request.
   */
  private MockSlingHttpServletResponse mockSlingResponse;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    context.addModelsForClasses(AuthorshipOptionsControlServlet.class);
    context.registerService(RunModeConfigService.class, runModeConfigService);
    mockSlingRequest = context.request();
    mockSlingResponse = context.response();
  }

  /**
   * Test doGet method of the servlet for allow scenario.
   *
   * @throws Exception the exception
   */
  @Test
  void testAuthorshipTableAccessTrue() throws Exception {
    Group group = mock(Group.class);
    user = mock(User.class);

    List<Group> userGroups = new ArrayList<>();
    userGroups.add(group);
    Iterator<Group> it = userGroups.iterator();

    String groupId = "CMTY CC Admin {DEV}";

    lenient().when(runModeConfigService.getEnv()).thenReturn("dev");
    lenient().when(userService.getCurrentUser(mockSlingRequest)).thenReturn(user);
    lenient().when(user.memberOf()).thenReturn(it);
    lenient().when(group.getID()).thenReturn(groupId);

    authorshipOptionsControlServlet.doGet(mockSlingRequest, mockSlingResponse);
    assertNotNull(mockSlingResponse);
    Gson gson = new Gson();
    JsonObject detailJson = gson.fromJson(mockSlingResponse.getOutputAsString(), JsonObject.class);
    assertEquals(true, detailJson.get("render").getAsBoolean());
  }

  /**
   * Test doGet method of the servlet for deny scenario.
   *
   * @throws Exception the exception
   */
  @Test
  void testAuthorshipTableAccessFalse() throws Exception {
    Group group = mock(Group.class);
    user = mock(User.class);

    List<Group> userGroups = new ArrayList<>();
    userGroups.add(group);
    Iterator<Group> it = userGroups.iterator();

    String groupId = "CMTY Education Author {DEV}";

    lenient().when(runModeConfigService.getEnv()).thenReturn("dev");
    lenient().when(userService.getCurrentUser(mockSlingRequest)).thenReturn(user);
    lenient().when(user.memberOf()).thenReturn(it);
    lenient().when(group.getID()).thenReturn(groupId);

    authorshipOptionsControlServlet.doGet(mockSlingRequest, mockSlingResponse);
    assertNotNull(mockSlingResponse);
    Gson gson = new Gson();
    JsonObject detailJson = gson.fromJson(mockSlingResponse.getOutputAsString(), JsonObject.class);
    assertEquals(false, detailJson.get("render").getAsBoolean());
  }
}
