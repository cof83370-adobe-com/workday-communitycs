package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.services.UserGroupService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
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

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

import static org.apache.sling.api.SlingHttpServletResponse.SC_FORBIDDEN;
import static org.apache.sling.api.SlingHttpServletResponse.SC_OK;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoHead_ValidUser() throws Exception {

        // Set up test parameters
        String uri = "/content/workday-community/en-us/example-uri";
        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");


        // Mock behavior
        when(request.getParameter("uri")).thenReturn(uri);
        when(request.getResourceResolver()).thenReturn(requestResourceResolver);
        when(requestResourceResolver.adaptTo(Session.class)).thenReturn(session);

        when(resolverFactory.getServiceResourceResolver(serviceParams)).thenReturn(resourceResolver);
        when(userGroupService.validateCurrentUser(request, uri)).thenReturn(true);

        // Call the method
        servlet.doHead(request, response);

        // Verify the interactions and assertions
        verify(request).getParameter("uri");
        verify(response).setStatus(SC_OK);
    }

    @Test
    void testDoHead_InvalidUser() throws Exception {

        // Set up test parameters
        String uri = "/content/workday-community/en-us/example-uri";
        Map<String, Object> serviceParams = new HashMap<>();
        serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");

        // Mock behavior
        when(request.getParameter("uri")).thenReturn(uri);
        when(request.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(null);

        // Call the method
        servlet.doHead(request, response);

        // Verify the interactions and assertions
        verify(request).getParameter("uri");
        verify(response).setStatus(SC_FORBIDDEN);

    }

}
