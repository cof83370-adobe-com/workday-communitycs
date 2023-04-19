package com.workday.community.aem.core.postprocessors;

import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserService;

import org.apache.jackrabbit.api.security.user.User;
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

/**
 * The Class CustomAuthenticationInfoPostProcessorTest.
 */
@ExtendWith(MockitoExtension.class)
public class CustomAuthenticationInfoPostProcessorTest {

    /** The okta service. */
    @Mock
    private OktaService oktaService;

    /** The user service. */
    @Mock
    private UserService userService;

    /** The AuthenticationInfo. */
    @Mock
    private AuthenticationInfo info;

    /** The HttpServletRequest. */
    @Mock
    private HttpServletRequest req;

    /** The HttpServletResponse. */
    @Mock
    private HttpServletResponse res;

    /** The mocked user. */
    @Mock
    private User user;

    /** The sourceValue. */
    @Mock
    Value sourceValue;

    /** Index service. */
    @InjectMocks
    CustomAuthenticationInfoPostProcessor processor;

    /**
     * Test postProcess.
     */
    @Test
    void testPostProcess() throws Exception {
        Value[] sourceValueArray = new Value[]{sourceValue};
        when(oktaService.isOktaIntegrationEnabled()).thenReturn(true);
        when(info.getUser()).thenReturn("testuser");
        when(user.getPath()).thenReturn("/workdaycommunity/okta/testuser");
        when(userService.getUser(anyString())).thenReturn(user);
        when(user.getProperty(PROFILE_SOURCE_ID)).thenReturn(sourceValueArray);
        when(sourceValueArray[0].getString()).thenReturn("testSourceValue");

        processor.postProcess(info, req, res);
        verify(user).getProperty(PROFILE_SOURCE_ID);
    }
}
