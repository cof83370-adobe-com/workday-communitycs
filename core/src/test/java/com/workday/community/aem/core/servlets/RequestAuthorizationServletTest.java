package com.workday.community.aem.core.servlets;

import static org.apache.sling.api.SlingHttpServletResponse.SC_FORBIDDEN;
import static org.apache.sling.api.SlingHttpServletResponse.SC_OK;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Session;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class RequestAuthorizationServletTest {

  @InjectMocks
  private RequestAuthorizationServlet servlet;

  @Mock
  private SlingHttpServletRequest request;

  @Mock
  private SlingHttpServletResponse response;

  @Mock
  private ResourceResolver resourceResolver;

  @Mock
  private ResourceResolver requestResourceResolver;

  @Mock
  private ResourceResolverFactory resolverFactory;

  @Mock
  private Session session;

  @Mock
  private UserGroupService userGroupService;

  @Mock
  private UserService userService;

  @Mock
  private User user;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testDoHeadValidUserForPages() throws Exception {

    // Set up test parameters
    String uri = "/content/workday-community/en-us/example-uri";
    Map<String, Object> serviceParams = new HashMap<>();
    serviceParams.put(ResourceResolverFactory.SUBSERVICE,
        "workday-community-administrative-service");


    // Mock behavior
    when(request.getParameter("uri")).thenReturn(uri);
    when(resolverFactory.getServiceResourceResolver(serviceParams)).thenReturn(resourceResolver);
    when(userGroupService.validateCurrentUser(request, uri)).thenReturn(true);
    when(userService.isLoggedInUser(request)).thenReturn(true);

    // Call the method
    servlet.doHead(request, response);

    // Verify the interactions and assertions
    verify(request).getParameter("uri");
    verify(response).setStatus(SC_OK);
    }

  @Test
  void testDoHead_InvalidUserForPages() throws Exception {

    // Set up test parameters
    String uri = "/content/workday-community/en-us/example-uri";
    Map<String, Object> serviceParams = new HashMap<>();
    serviceParams.put(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    // Mock behavior
    when(request.getParameter("uri")).thenReturn(uri);
    when(userService.isLoggedInUser(request)).thenReturn(false);

    // Call the method
    servlet.doHead(request, response);

    // Verify the interactions and assertions
    verify(request).getParameter("uri");
    verify(response).setStatus(SC_FORBIDDEN);

    }

  @Test
  void testDoHeadForPublicPages() throws Exception {

    // Set up test parameters
    String uri = "/content/workday-community/en-us/public/example-uri";
    Map<String, Object> serviceParams = new HashMap<>();
    serviceParams.put(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    // Mock behavior
    when(request.getParameter("uri")).thenReturn(uri);

    // Call the method
    servlet.doHead(request, response);

    // Verify the interactions and assertions
    verify(request).getParameter("uri");
    verify(response).setStatus(SC_OK);

  }

  @Test
  void testDoHeadForPublicAssets() throws Exception {

    // Set up test parameters
    String uri = "/content/dam/workday-community/en-us/public/home.jpeg";
    Map<String, Object> serviceParams = new HashMap<>();
    serviceParams.put(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    // Mock behavior
    when(request.getParameter("uri")).thenReturn(uri);

    // Call the method
    servlet.doHead(request, response);

    // Verify the interactions and assertions
    verify(request).getParameter("uri");
    verify(response).setStatus(SC_OK);

  }


  @Test
  void testDoHeadForPrivateAssetsForInvalidUser() throws Exception {

    // Set up test parameters
    String uri = "/content/dam/workday-community/en-us/home.jpeg";
    Map<String, Object> serviceParams = new HashMap<>();
    serviceParams.put(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    // Mock behavior
    when(request.getParameter("uri")).thenReturn(uri);

    // Call the method
    servlet.doHead(request, response);

    // Verify the interactions and assertions
    verify(request).getParameter("uri");
    verify(response).setStatus(SC_FORBIDDEN);

  }

  @Test
  void testDoHeadForPrivateAssetsForValidUser() throws Exception {

    // Set up test parameters
    String uri = "/content/dam/workday-community/en-us/images/home.jpeg";
    Map<String, Object> serviceParams = new HashMap<>();
    serviceParams.put(ResourceResolverFactory.SUBSERVICE,
            "workday-community-administrative-service");

    // Mock behavior
    when(request.getParameter("uri")).thenReturn(uri);
    when(userService.getCurrentUser(request)).thenReturn(user);
    when(user.getPath()).thenReturn("/home/user/workday-community/okta");

    // Call the method
    servlet.doHead(request, response);

    // Verify the interactions and assertions
    verify(request).getParameter("uri");
    verify(response).setStatus(SC_OK);

  }

}
