package com.workday.community.aem.core.postprocessors;

import com.workday.community.aem.core.services.OktaService;
import org.apache.commons.lang3.StringUtils;
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

import javax.jcr.Value;
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

    ResourceResolver resolver;

    String sourceId;
    public static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationInfoPostProcessor.class);

    @Override
    public void postProcess(AuthenticationInfo info, HttpServletRequest req, HttpServletResponse res)
            throws LoginException {
        LOG.info("inside  CustomAuthenticationInfoPostProcessor.postProcess() {}",info.getUser());
        LOG.info("isOktaIntegrationEnabled {}",oktaService.isOktaIntegrationEnabled());
        if (oktaService.isOktaIntegrationEnabled() && null != info && StringUtils.isNotBlank(info.getUser())) {
            String userId = info.getUser();
            try {
                LOG.info("User ID: {}", userId);
                Map<String, Object> serviceParams = new HashMap<>();
                serviceParams.put(ResourceResolverFactory.SUBSERVICE, "workday-community-administrative-service");
                resolver = resolverFactory.getServiceResourceResolver(serviceParams);
                UserManager userManager = resolver.adaptTo(UserManager.class);
                Authorizable user = userManager.getAuthorizable(userId);

                if (null != user && user.getPath().contains("/workdaycommunity")) {
                    Value[] valueArray = user.getProperty("profile/sourceId");
                    if(null !=valueArray && null != valueArray[0])
                    {
                        sourceId = valueArray[0].getString();
                        LOG.info("sourceId: {}", sourceId);
                        LOG.error("sourceId: {}", sourceId);
                    }
                }
            } catch (Exception e) {
                LOG.error("Error in CustomAuthenticationInfoPostProcessor {}", e.getMessage());
            } finally {
                if (resolver != null && resolver.isLive()) {
                    resolver.close();
                    resolver = null;
                }

            }

        }
    }

}
