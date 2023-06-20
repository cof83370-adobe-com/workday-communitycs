package com.workday.community.aem.core.models.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.models.Metadata;

/**
 * The Class MetadataImpl.
 */
@Model(adaptables = { Resource.class, SlingHttpServletRequest.class }, adapters = { Metadata.class }, resourceType = {
        MetadataImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class MetadataImpl implements Metadata {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(MetadataImpl.class);

    /** The Constant RESOURCE_TYPE. */
    protected static final String RESOURCE_TYPE = "workday-community/components/common/metadata";

    /** The author name. */
    private String authorName;

    /** The current page. */
    @Inject
    private Page currentPage;

    /** The resource resolver. */
    @Inject
    private ResourceResolver resourceResolver;

    /**
     * Gets the author name.
     *
     * @return the author name
     */
    @Override
    public String getAuthorName() {
            String fullName = "";
            ValueMap currentPageProperties = currentPage.getProperties();
            if (null != currentPageProperties) {
                String author = currentPageProperties.get(GlobalConstants.PROP_AUTHOR, String.class);
                if (null != author) {
                    String authorFullName = getFullNameByUserID(author);
                    fullName = StringUtils.isNotBlank(authorFullName) ? authorFullName : author;
                } else {
                    fullName = getFullNameByUserID(currentPageProperties.get(GlobalConstants.PROP_JCR_CREATED_BY, String.class));
                }
            }
            if (StringUtils.isNotBlank(fullName)) {
                authorName = StringUtils.trim(fullName);
            }
        return authorName;
    }

    /**
     * Gets the full name by user ID.
     *
     * @param userID the user ID
     * @return the full name by user ID
     */
    private String getFullNameByUserID(String userID) {
        UserManager userManager = resourceResolver.adaptTo(UserManager.class);
        String fullName = "";
        try {
            Authorizable authorizable = userManager.getAuthorizable(userID);
            if (null != authorizable && !authorizable.isGroup()) {
                String firstName = authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_GIVENNAME) != null
                        ? authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_GIVENNAME)[0].getString()
                        : null;
                String lastName = authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_FAMILYNAME) != null
                        ? authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_FAMILYNAME)[0].getString()
                        : null;
                if (null != firstName || null != lastName) {
                    fullName = String.format("%s %s", StringUtils.trimToEmpty(firstName),
                            StringUtils.trimToEmpty(lastName));
                }
            }
        } catch (RepositoryException e) {
            LOG.error(String.format("RepositoryException in MetadataImpl::getFullNameByUserID: %s", e.getMessage()));
        }
        return fullName;
    }

    /**
     * Gets the posted date.
     *
     * @return the posted date
     */
    @Override
    public Date getPostedDate() {
        return currentPage.getProperties().get(GlobalConstants.PROP_POSTED_DATE, Date.class);
    }

    /**
     * Gets the updated date.
     *
     * @return the updated date
     */
    @Override
    public Date getUpdatedDate() {
        return currentPage.getProperties().get(GlobalConstants.PROP_UPDATED_DATE, Date.class);
    }
}
