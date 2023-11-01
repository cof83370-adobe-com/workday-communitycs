package com.workday.community.aem.core.models;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.services.RunModeConfigService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jcr.Session;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.spi.security.authentication.external.basic.DefaultSyncConfig.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.servlet.MockRequestPathInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class AuthorshipRenderConditionModelTest.
 *
 * @author uttej.vardineni
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class AuthorshipRenderConditionModelTest {

  /**
   * The context.
   */
  private final AemContext context = new AemContext();

  /**
   * The session.
   */
  @Mock
  Session session;

  /**
   * The user manager.
   */
  @Mock
  UserManager userManager;

  /**
   * The authorizable.
   */
  @Mock
  Authorizable authorizable;

  @Mock
  RunModeConfigService runModeConfigService;

  /**
   * The mocked user.
   */
  @Mock
  User user;

  /**
   * The authorship render condition model test.
   */
  private AuthorshipRenderConditionModel authorshipRenderConditionModelTest;

  /**
   * The current page.
   */
  private Page currentPage;

  /**
   * The request.
   */
  @Mock
  private SlingHttpServletRequest request;

  /**
   * The resource.
   */
  private Resource resource;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @BeforeEach
  public void setup() throws Exception {
    context.addModelsForClasses(AuthorshipRenderConditionModel.class);
    Map<String, Object> pageProperties = new HashMap<>();

    currentPage = context.create().page("/content/workday-community/event",
        "/conf/workday-community/settings/wcm/templates/event-page-template", pageProperties);

    currentPage = context.currentResource("/content/workday-community/event").adaptTo(Page.class);
    resource = context.create().resource(currentPage, "eventspage",
        "sling:resourceType", "workday-community/components/structure/eventspage", "editGroups",
        "[CMTY CC Admin]");
    context.registerService(Page.class, currentPage);
    context.registerService(RunModeConfigService.class, runModeConfigService);
  }

  /**
   * Test AuthorTitleRenderConditionModel.
   *
   * @throws Exception the exception
   */
  @Test
  void testAuthorshipRenderConditionModelReadOnly() throws Exception {
    String userId = "testUser";
    String groupId = "dummyGroup";
    context.request().setResource(resource);
    MockRequestPathInfo requestPathInfo =
        (MockRequestPathInfo) context.request().getRequestPathInfo();
    requestPathInfo.setResourcePath("/authorReadOnly/granite:condition");

    userManager = mock(UserManager.class);
    session = mock(Session.class);
    authorizable = mock(Authorizable.class);
    Group group = mock(Group.class);
    user = mock(User.class);

    List<String> groups = new ArrayList<>();
    groups.add(groupId);
    List<Group> userGroups = new ArrayList<>();
    userGroups.add(group);
    Iterator<Group> it = userGroups.iterator();
    when(userManager.getAuthorizable(userId)).thenReturn(user);
    lenient().when(session.getUserID()).thenReturn(userId);
    lenient().when(runModeConfigService.getEnv()).thenReturn("local");
    lenient().when(userManager.getAuthorizable(groupId)).thenReturn(null);
    lenient().when(userManager.createGroup(groupId)).thenReturn(group);
    lenient().when(user.memberOf()).thenReturn(it);

    context.registerAdapter(ResourceResolver.class, Session.class, session);
    context.registerAdapter(ResourceResolver.class, UserManager.class, userManager);
    context.registerAdapter(ResourceResolver.class, Authorizable.class, authorizable);

    authorshipRenderConditionModelTest =
        context.request().adaptTo(AuthorshipRenderConditionModel.class);
    assertNotNull(authorshipRenderConditionModelTest);
  }

  /**
   * Test AuthorTitleRenderConditionModel.
   *
   * @throws Exception the exception
   */
  @Test
  void testAuthorshipRenderConditionModel() throws Exception {
    String userId = "testUser";
    String groupId = "dummyGroup";
    context.request().setResource(resource);
    MockRequestPathInfo requestPathInfo =
        (MockRequestPathInfo) context.request().getRequestPathInfo();
    requestPathInfo.setResourcePath("/author/granite:condition");

    userManager = mock(UserManager.class);
    session = mock(Session.class);
    authorizable = mock(Authorizable.class);
    Group group = mock(Group.class);
    user = mock(User.class);

    List<String> groups = new ArrayList<>();
    groups.add(groupId);
    List<Group> userGroups = new ArrayList<>();
    userGroups.add(group);
    Iterator<Group> it = userGroups.iterator();
    when(userManager.getAuthorizable(userId)).thenReturn(user);
    lenient().when(session.getUserID()).thenReturn(userId);
    lenient().when(runModeConfigService.getEnv()).thenReturn("local");
    lenient().when(userManager.getAuthorizable(groupId)).thenReturn(null);
    lenient().when(userManager.createGroup(groupId)).thenReturn(group);
    lenient().when(user.memberOf()).thenReturn(it);

    context.registerAdapter(ResourceResolver.class, Session.class, session);
    context.registerAdapter(ResourceResolver.class, UserManager.class, userManager);
    context.registerAdapter(ResourceResolver.class, Authorizable.class, authorizable);

    authorshipRenderConditionModelTest =
        context.request().adaptTo(AuthorshipRenderConditionModel.class);
    assertNotNull(authorshipRenderConditionModelTest);
  }
}