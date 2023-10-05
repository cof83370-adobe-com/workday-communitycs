
package com.workday.community.aem.core.filters;

import static com.workday.community.aem.core.constants.GlobalConstants.PUBLISH;
import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static com.workday.community.aem.core.constants.WccConstants.ACCESS_CONTROL_TAG;

import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
import com.workday.community.aem.core.utils.ResolverUtil;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
    static final String DYNAMIC_RESOURCE_TYPE_PATH = "workday-community/components/dynamic/";

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(ComponentFilter.class);

    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;

    /** The user group service. */
    @Reference
    private UserGroupService userGroupService;

    /** The run mode config service. */
    @Reference
    RunModeConfigService runModeConfigService;

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
            Instant start = Instant.now();
            SlingHttpServletRequest request = (SlingHttpServletRequest) servletRequest;
            String instance = runModeConfigService.getInstance();
            String resourceType = request.getResource().getResourceType();
            if (instance != null && instance.equals(PUBLISH)
                    && resourceType.contains(DYNAMIC_RESOURCE_TYPE_PATH)) {
                try (ResourceResolver resolver = ResolverUtil.newResolver(resolverFactory,
                        READ_SERVICE_USER)) {
                    List<String> userGroupsList = userGroupService.getCurrentUserGroups(request);
                    logger.debug("ComponentFilter::ACL Tags of user {}", userGroupsList);
                    ValueMap properties = request.getResource().getValueMap();
                    List<String> componentACLTags = Arrays
                            .asList(properties.get("componentACLTags", new String[0]));
                    List<String> accessControlList = new ArrayList<>();
                    componentACLTags
                            .forEach(tag -> accessControlList.add(tag.replace(ACCESS_CONTROL_TAG.concat(":"), "")));
                    logger.debug("ComponentFilter::ACL Tags of component {}", accessControlList);
                    if (CollectionUtils.isNotEmpty(accessControlList) && CollectionUtils.isNotEmpty(userGroupsList)
                            && CollectionUtils.intersection(accessControlList, userGroupsList).isEmpty()) {
                        logger.debug("ComponentFilter::Permission not matching.. not rendeing component.");
                        logger.debug(
                                "......................Execution time of filter method for resource {} {}...............",
                                resourceType, Duration.between(start, Instant.now()));
                        return;
                    } else if (CollectionUtils.isNotEmpty(accessControlList)
                            && CollectionUtils.isEmpty(userGroupsList)) {
                        logger.debug("ComponentFilter::User permissions are empty.. not rendeing component.");
                        logger.debug(
                                "......................Execution time of filter method for resource {} {}...............",
                                resourceType, Duration.between(start, Instant.now()));
                        return;
                    }
                } catch (LoginException e) {
                    logger.error("Exception retrieving Resource Resolver for path {}",
                            request.getResource().getPath());
                }
            }
            logger.debug(
                    "......................Component Filter Execution time for resource {} {}...............",
                    resourceType, Duration.between(start, Instant.now()));
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
