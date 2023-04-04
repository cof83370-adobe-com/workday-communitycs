package com.workday.community.aem.core.postprocessors;

import com.workday.community.aem.core.services.OktaService;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.mockito.InjectMocks;

import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomAuthenticationInfoPostProcessorTest {

    @Mock
    private OktaService oktaService;

    @Mock
    private AuthenticationInfo info;

    @Mock
    private HttpServletRequest req;

    @Mock
    private HttpServletResponse res;

    @Mock
    private Logger LOG;

    @Mock
    private ResourceResolverFactory resolverFactory;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private Session session;

    @Mock
    private UserManager userManager;

    @Mock
    private Authorizable authorizable;

    /** The oktaIdValue. */
    @Mock
    Value oktaIdValue;



    /** The sourceValue . */
    @Mock
    Value sourceValue;
    @InjectMocks
    CustomAuthenticationInfoPostProcessor processor;

    @Test
    void testPostProcess() throws Exception {

        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(info.getUser()).thenReturn("testuser");
        when(resolverFactory.getServiceResourceResolver(anyMap())).thenReturn(resolver);
        when(resolver.adaptTo(Session.class)).thenReturn(session);

        // set up the mock JackrabbitSession to return the mock UserManager
        JackrabbitSession jackrabbitSession = mock(JackrabbitSession.class);

        when(session.impersonate(any(Credentials.class))).thenReturn(jackrabbitSession);


        when(jackrabbitSession.getUserManager()).thenReturn(userManager);
        when(userManager.getAuthorizable("userId")).thenReturn(authorizable);
        when(authorizable.getPath()).thenReturn("/path/to/workday/testuser");

        Value[] sourceValueArray = new Value[] { sourceValue };
        Value[] oktaIdValueArray = new Value[] { oktaIdValue };

        when(authorizable.getProperty("profile/sourceId")).thenReturn(sourceValueArray);
        when(authorizable.getProperty("profile/oktaId")).thenReturn(oktaIdValueArray);
        when(sourceValueArray[0].getString()).thenReturn("testSourceValue");
        when(oktaIdValueArray[0].getString()).thenReturn("TestOktaID");
        processor.postProcess(info, req, res);

        verify(oktaService).isOktaIntegrationEnabled();


        verify(resolverFactory).getServiceResourceResolver(anyMap());
        verify(resolver).adaptTo(Session.class);


        verify(authorizable).getProperty("profile/sourceId");
        verify(authorizable).getProperty("profile/oktaId");
        verify(LOG).error("User ID: {}", "userId");
        verify(LOG).error("sourceId: {}", "sourceId");
        verify(LOG).error("oktaId: {}", "oktaId");
        verify(resolver).close();
        verify(session).logout();
    }
}
