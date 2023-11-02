package com.workday.community.aem.core.filters;

import static com.workday.community.aem.core.constants.GlobalConstants.PUBLISH;
import static com.workday.community.aem.core.constants.WccConstants.ACCESS_CONTROL_TAG;

import com.workday.community.aem.core.services.RunModeConfigService;
import com.workday.community.aem.core.services.UserGroupService;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.engine.EngineConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The Class ComponentFilter.
 */
@Slf4j
@Component(service = Filter.class, configurationPolicy = ConfigurationPolicy.OPTIONAL, property = {
    EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_INCLUDE,
    EngineConstants.SLING_FILTER_METHODS + "=GET",
    EngineConstants.SLING_FILTER_PATTERN + "=/content/workday-community/(.*)",

})
public class ComponentFilter implements Filter {

  /**
   * Path to dynamic resources.
   */
  private static final String DYNAMIC_RESOURCE_TYPE_PATH = "workday-community/components/dynamic/";

  /**
   * The run mode config service.
   */
  @Reference
  private RunModeConfigService runModeConfigService;

  /**
   * The resolver factory.
   */
  @Reference
  private ResourceResolverFactory resolverFactory;

  /**
   * The user group service.
   */
  @Reference
  private UserGroupService userGroupService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.info("Initialize the component hide filter");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain)
      throws IOException, ServletException {

    if (servletRequest instanceof SlingHttpServletRequest) {
      Instant start = Instant.now();
      SlingHttpServletRequest request = (SlingHttpServletRequest) servletRequest;
      String instance = runModeConfigService.getInstance();
      String resourceType = request.getResource().getResourceType();
      if (instance != null && instance.equals(PUBLISH)
          && resourceType.contains(DYNAMIC_RESOURCE_TYPE_PATH)) {
        List<String> userGroupsList = userGroupService.getCurrentUserGroups(request);
        log.debug("ComponentFilter::ACL Tags of user {}", userGroupsList);
        ValueMap properties = request.getResource().getValueMap();
        List<String> componentAclTags = Arrays
            .asList(properties.get("componentACLTags", new String[0]));
        List<String> accessControlList = new ArrayList<>();
        componentAclTags
            .forEach(
                tag -> accessControlList.add(tag.replace(ACCESS_CONTROL_TAG.concat(":"), "")));
        log.debug("ComponentFilter::ACL Tags of component {}", accessControlList);
        if (CollectionUtils.isNotEmpty(accessControlList)
            && CollectionUtils.isNotEmpty(userGroupsList)
            && CollectionUtils.intersection(accessControlList, userGroupsList).isEmpty()) {
          log.debug("ComponentFilter::Permission not matching.. not rendeing component.");
          log.debug(
              "...............Execution time of filter method for resource {} {}...............",
              resourceType, Duration.between(start, Instant.now()));
          return;
        } else if (CollectionUtils.isNotEmpty(accessControlList)
            && CollectionUtils.isEmpty(userGroupsList)) {
          log.debug("ComponentFilter::User permissions are empty.. not rendeing component.");
          log.debug(
              "...............Execution time of filter method for resource {} {}...............",
              resourceType, Duration.between(start, Instant.now()));
          return;
        }
      }
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    log.info("Exiting the component hide filter");
  }
}
