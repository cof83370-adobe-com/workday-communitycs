
package com.workday.community.aem.core.filters;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class ComponentFilterTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class ComponentFilterTest {

    /** The context. */
    private final AemContext context = new AemContext();

    /** The resolver factory. */
    @Mock
    ResourceResolverFactory resolverFactory;

    /** The resolver. */
    @Mock
    ResourceResolver resolver;

    /** The user group service. */
    @Mock
    UserGroupService userGroupService;

    /** The user service. */
    @Mock
    UserService userService;

    /** The user manager. */
    @Mock
    UserManager userManager;

    /** The user. */
    @Mock
    User user;

    /** The request. */
    @Spy
    @InjectMocks
    MockSlingHttpServletRequest request = context.request();

    /** The response. */
    @Spy
    @InjectMocks
    MockSlingHttpServletResponse response = context.response();

    /** The filter chain. */
    @Mock
    FilterChain filterChain;

    /** The filter config. */
    @Mock
    FilterConfig filterConfig;

    /** The component filter. */
    @InjectMocks
    private ComponentFilter componentFilter;

    /** The run mode config */
    @Mock
    RunModeConfigService runModeConfigService;

    /**
     * Test D0 filter without valid user.
     *
     * @throws ServletException the servlet exception
     * @throws IOException      Signals that an I/O exception has occurred.
     * @throws LoginException   the login exception
     */
    @Test
    void testD0FilterWithoutValidUser() throws ServletException, IOException, LoginException {
        componentFilter.init(filterConfig);
        Resource resource = mock(Resource.class);
        request.setResource(resource);
        String[] tagList = { "access-control:authenticated", "access-control:customer_all" };
        ValueMap properties = mock(ValueMap.class);
        when(request.getResource().getValueMap()).thenReturn(properties);

        when(properties.get("componentACLTags", new String[0])).thenReturn(tagList);

        lenient().when(ResolverUtil.newResolver(resolverFactory, "READ_SERVICE_USER"))
                .thenReturn(resolver);
        lenient().when(runModeConfigService.getInstance()).thenReturn("publish");

        when(request.getResource().getResourceType())
                .thenReturn("workday-community/components/dynamic/");

        componentFilter.doFilter(request, response, filterChain);
        assertNotNull(response);

    }
}
