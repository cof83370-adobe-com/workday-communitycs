package com.workday.community.aem.core.postprocessors;

import com.workday.community.aem.core.services.OktaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.apache.sling.auth.core.spi.AuthenticationInfoPostProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component(name = "CustomAuthenticationInfoPostProcessor",
        service = AuthenticationInfoPostProcessor.class,
        immediate = true
)
public class CustomAuthenticationInfoPostProcessor implements AuthenticationInfoPostProcessor {

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    transient OktaService oktaService;

    private Session session;
    ResourceResolver resolver;
    public static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationInfoPostProcessor.class);

    @Override
    public void postProcess(AuthenticationInfo info, HttpServletRequest req, HttpServletResponse res)
            throws LoginException {
        if (oktaService.isOktaIntegrationEnabled() && null != info && StringUtils.isNotBlank(info.getUser())) {
            String userId = info.getUser();
            try {

                Map<String, Object> serviceParams = new HashMap<>();
                serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
                resolver = resolverFactory.getServiceResourceResolver(serviceParams);
                session = resolver.adaptTo(Session.class);

                UserManager userManager = ((JackrabbitSession) session).getUserManager();
                Authorizable user = userManager.getAuthorizable(userId);

                if (null != user && user.getPath().contains("/workday")) {
                    LOG.error("User ID: {}", userId);
                    LOG.error("sourceId: {}", user.getProperty("profile/sourceId"));
                    LOG.error("oktaId: {}", user.getProperty("profile/oktaId"));
                }


            } catch (Exception e) {
                LOG.error("Error in CustomAuthenticationInfoPostProcessor {}", e.getMessage());
            } finally {
                if (resolver != null && resolver.isLive()) {
                    resolver.close();
                    resolver = null;
                }
                if (session != null && session.isLive()) {
                    session.logout();
                    session = null;
                }

            }

        }
    }


}
