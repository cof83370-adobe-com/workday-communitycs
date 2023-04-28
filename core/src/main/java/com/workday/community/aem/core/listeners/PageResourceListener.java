package com.workday.community.aem.core.listeners;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.vault.util.JcrConstants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.utils.CommonUtils;
import com.workday.community.aem.core.utils.ResolverUtil;

/**
 * The Class PageResourceListener.
 */
@Component(service = ResourceChangeListener.class, immediate = true, property = {
        ResourceChangeListener.PATHS + "=" + GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH,
        ResourceChangeListener.CHANGES + "=" + "REMOVED",
        ResourceChangeListener.CHANGES + "=" + "ADDED",
})

@ServiceDescription("PageResourceListener")
public class PageResourceListener implements ResourceChangeListener {
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(PageResourceListener.class);

    /** The resolver factory. */
    @Reference
    private ResourceResolverFactory resolverFactory;

    /** The query service. */
    @Reference
    QueryService queryService;

    @Override
    public void onChange(List<ResourceChange> changes) {
        if (changes.size() == 1 && changes.get(0).getType().toString().equals("REMOVED")) {
            removeBookNodes(changes.get(0).getPath());
            return;
        }

        changes.stream()
                .filter(item -> item.getType().toString() == "ADDED"
                        && item.getPath().endsWith(GlobalConstants.JCR_CONTENT_PATH))
                .forEach(change -> addAuthorPropertyToContentNode(change.getPath()));
    }

    public void addAuthorPropertyToContentNode(String path) {
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resolverFactory,
                "workday-community-administrative-service")) {
            if (resourceResolver.getResource(path) != null) {
                Node root = resourceResolver.getResource(path).adaptTo(Node.class);
                String createdUserId = root.hasProperty(JcrConstants.JCR_CREATED_BY)
                        ? root.getProperty(JcrConstants.JCR_CREATED_BY).getString()
                        : CommonUtils.getLoggedInUserId(resourceResolver);
                UserManager userManager = resourceResolver.adaptTo(UserManager.class);

                Authorizable authorizable = userManager.getAuthorizable(createdUserId);

                if (authorizable == null) {
                    logger.warn("No such user: ${userId}");
                    root.setProperty("author", createdUserId);
                } else {
                    if (null != authorizable && !authorizable.isGroup()) {
                        String firstName = authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_GIVENNAME) != null
                                ? authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_GIVENNAME)[0].getString()
                                : null;
                        String lastName = authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_FAMILYNAME) != null
                                ? authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_FAMILYNAME)[0].getString()
                                : null;
                        if (null != firstName || null != lastName) {
                            String fullName = String.format("%s %s", StringUtils.trimToEmpty(firstName),
                                    StringUtils.trimToEmpty(lastName));
                            root.setProperty("author", fullName);
                        }
                    }
                }
            }
            if (resourceResolver.hasChanges()) {
                resourceResolver.commit();
            }
        } catch (PersistenceException | RepositoryException | LoginException e) {
            logger.error("Exception occurred when adding author property to page {} ", e.getMessage());
        }
    }

    /**
     * Removes the book nodes.
     *
     * @param pagePath the page path
     */
    public void removeBookNodes(String pagePath) {

        try (ResourceResolver resolver = ResolverUtil.newResolver(resolverFactory,
                "workday-community-administrative-service")) {
            if (!pagePath.contains(GlobalConstants.JCR_CONTENT_PATH)) {
                List<String> paths = queryService.getBookNodesByPath(pagePath, null);
                for (String path : paths) {
                    if (resolver.getResource(path) != null) {
                        Node root = resolver.getResource(path).adaptTo(Node.class);
                        if (root != null) {
                            root.remove();
                        }
                    }
                }
                if (resolver.hasChanges()) {
                    resolver.commit();
                }
                logger.info("Removed node for page {}", pagePath);
            }
        } catch (PersistenceException | RepositoryException | LoginException e) {
            logger.error("Can't remove found nodes for page {}", pagePath);
        }
    }
}