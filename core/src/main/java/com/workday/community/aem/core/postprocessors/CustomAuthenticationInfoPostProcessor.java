package com.workday.community.aem.core.postprocessors;

import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.services.UserService;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.auth.core.spi.AuthenticationInfo;
import org.apache.sling.auth.core.spi.AuthenticationInfoPostProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.workday.community.aem.core.constants.GlobalConstants.OKTA_USER_PATH;
import static com.workday.community.aem.core.constants.WccConstants.PROFILE_SOURCE_ID;

@Component(name = "CustomAuthenticationInfoPostProcessor",
        service = AuthenticationInfoPostProcessor.class,
        immediate = true
)
public class CustomAuthenticationInfoPostProcessor implements AuthenticationInfoPostProcessor {

    /** The okta service. */
    @Reference
    transient OktaService oktaService;

    /** The user service. */
    @Reference
    UserService userService;

    /** The sourceId. */
    String sourceId;

    /** The logger. */
    public static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationInfoPostProcessor.class);

    @Override
    public void postProcess(AuthenticationInfo info, HttpServletRequest req, HttpServletResponse res)
            throws LoginException {
        LOG.info("inside  CustomAuthenticationInfoPostProcessor.postProcess()");
        LOG.info("isOktaIntegrationEnabled {}", oktaService.isOktaIntegrationEnabled());
        if (oktaService.isOktaIntegrationEnabled() && null != info && StringUtils.isNotBlank(info.getUser())) {
            String userId = info.getUser();
            User user = userService.getUser(userId);      
            try {
                if (user != null && user.getPath().contains(OKTA_USER_PATH)) {
                    LOG.info("userpath: {}", user.getPath());
                    Value[] valueArray = user.getProperty(PROFILE_SOURCE_ID);
                    if (null != valueArray && null != valueArray[0]) {
                        //TODO Source ID should be taken care in next sprint.
                        sourceId = valueArray[0].getString();
                        LOG.info("sourceId: {}", sourceId);
                    }
                }
            } 
            catch (RepositoryException e) {
                LOG.error("Error in CustomAuthenticationInfoPostProcessor {}", e.getMessage());          
            }
        }
    }

}
