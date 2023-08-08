
package com.workday.community.aem.core.filters;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.engine.EngineConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.utils.ResolverUtil;

import javax.servlet.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static com.workday.community.aem.core.constants.GlobalConstants.PUBLISH;

/**
 * The Class ComponentFilter.
 */
@Component(service = Filter.class, configurationPolicy = ConfigurationPolicy.OPTIONAL, property = {
        EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_INCLUDE,
        EngineConstants.SLING_FILTER_METHODS + "=GET",
        EngineConstants.SLING_FILTER_PATTERN + "=/content/workday-community/(.*)",

})

public class ComponentFilter implements Filter {

    /** The Constant whiteList. */
    static final String dynamicResourceTypePath = "workday-community/components/dynamic/";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(ComponentFilter.class);

    /** The resolver factory. */
    @Reference
    private transient ResourceResolverFactory resolverFactory;

    /** The user group service. */
    @Reference
    private transient UserGroupService userGroupService;

    /** The run mode config service. */
    @Reference
    RunModeConfigService runModeConfigService;
    /**
     * The user service user.
     */
    public static final String READ_SERVICE_USER = "readserviceuser";

    /**
     * Inits the.
     *
     * @param filterConfig the filter config
     * @throws ServletException the servlet exception
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initialize the component hide filter");
    }

    /**
     * Do filter.
     *
     * @param servletRequest  the servlet request
     * @param servletResponse the servlet response
     * @param filterChain     the filter chain
     * @throws IOException      Signals that an I/O exception has occurred.
     * @throws ServletException the servlet exception
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (servletRequest instanceof SlingHttpServletRequest) {

            SlingHttpServletRequest request = (SlingHttpServletRequest) servletRequest;
            String instance = runModeConfigService.getInstance();
            if (instance != null && instance.equals(PUBLISH)
                    && request.getResource().getResourceType().contains(dynamicResourceTypePath)) {
                Instant start = Instant.now();
                try (ResourceResolver resolver = ResolverUtil.newResolver(resolverFactory,
                        READ_SERVICE_USER)) {
                    List<String> groupsList = userGroupService.getLoggedInUsersGroups(resolver);
                    ValueMap properties = request.getResource().getValueMap();
                    List<String> accessControlList = Arrays
                            .asList(properties.get("componentACLTags", new String[0]));
                    if (CollectionUtils.isNotEmpty(accessControlList) && CollectionUtils.isNotEmpty(groupsList)
                            && CollectionUtils.intersection(accessControlList, groupsList).isEmpty()) {
                        return;
                    } else if (CollectionUtils.isNotEmpty(accessControlList)
                            && CollectionUtils.isEmpty(groupsList)) {
                        return;
                    }
                } catch (OurmException | LoginException e) {
                    logger.error("Exception retrieving Resource Resolver for path {}",
                            request.getResource().getPath());
                }
                Instant end = Instant.now();
                logger.error(
                        "......................Execution time of filter method for resource {} {}...............",
                        request.getResource().getResourceType(), Duration.between(start, end));
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Destroy.
     */
    @Override
    public void destroy() {
        logger.info("Exiting the component hide filter");
    }
}