package com.workday.community.aem.core.postprocessors;

import com.workday.community.aem.core.services.OktaService;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.workday.community.aem.core.constants.WccConstants.PROFILE_SOURCE_ID;
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
    private ResourceResolverFactory resolverFactory;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private UserManager userManager;

    @Mock
    private Authorizable authorizable;

    /**
     * The sourceValue .
     */
    @Mock
    Value sourceValue;

    @InjectMocks
    CustomAuthenticationInfoPostProcessor processor;

    @Test
    void testPostProcess() throws Exception {
        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(info.getUser()).thenReturn("testuser");
        when(resolverFactory.getServiceResourceResolver(anyMap())).thenReturn(resolver);
        when(resolver.adaptTo(UserManager.class)).thenReturn(userManager);
        when(resolver.isLive()).thenReturn(true);

        when(userManager.getAuthorizable("testuser")).thenReturn(authorizable);
        when(authorizable.getPath()).thenReturn("/workdaycommunity/okta/testuser");

        Value[] sourceValueArray = new Value[]{sourceValue};

        when(authorizable.getProperty(PROFILE_SOURCE_ID)).thenReturn(sourceValueArray);
        when(sourceValueArray[0].getString()).thenReturn("testSourceValue");

        processor.postProcess(info, req, res);


        verify(resolverFactory).getServiceResourceResolver(anyMap());
        verify(authorizable).getProperty(PROFILE_SOURCE_ID);
        verify(resolver).close();
    }
}
