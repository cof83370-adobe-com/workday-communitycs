package com.workday.community.aem.core.models.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.models.Metadata;
import java.util.Date;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

/**
 * The Class MetadataImpl.
 */
@Slf4j
@Model(
    adaptables = {Resource.class, SlingHttpServletRequest.class},
    adapters = {Metadata.class},
    resourceType = {MetadataImpl.RESOURCE_TYPE},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MetadataImpl implements Metadata {

  /**
   * The Constant RESOURCE_TYPE.
   */
  protected static final String RESOURCE_TYPE = "workday-community/components/common/metadata";

  /**
   * The author name.
   */
  private String authorName;

  /**
   * The current page.
   */
  @Inject
  private Page currentPage;

  /**
   * The resource resolver.
   */
  @Inject
  private ResourceResolver resourceResolver;

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAuthorName() {
    String fullName = "";
    ValueMap currentPageProperties = currentPage.getProperties();
    if (null != currentPageProperties) {
      String author = currentPageProperties.get(GlobalConstants.PROP_AUTHOR, String.class);
      if (null != author) {
        String authorFullName = getFullNameByUserId(author);
        fullName = StringUtils.isNotBlank(authorFullName) ? authorFullName : author;
      } else {
        fullName = getFullNameByUserId(
            currentPageProperties.get(JcrConstants.JCR_CREATED_BY, String.class));
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
   * @param userId the user ID.
   *
   * @return the full name by user ID.
   */
  private String getFullNameByUserId(String userId) {
    UserManager userManager = resourceResolver.adaptTo(UserManager.class);
    String fullName = "";
    try {
      Authorizable authorizable = userManager.getAuthorizable(userId);
      if (null != authorizable && !authorizable.isGroup()) {
        String firstName =
            authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_GIVENNAME) != null
                ?
                authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_GIVENNAME)[0].getString()
                : null;
        String lastName =
            authorizable.getProperty(GlobalConstants.PROP_USER_PROFILE_FAMILYNAME) != null
                ? authorizable.getProperty(
                GlobalConstants.PROP_USER_PROFILE_FAMILYNAME)[0].getString()
                : null;
        if (null != firstName || null != lastName) {
          fullName = String.format("%s %s", StringUtils.trimToEmpty(firstName),
              StringUtils.trimToEmpty(lastName));
        }
      }
    } catch (RepositoryException e) {
      log.error("RepositoryException in MetadataImpl::getFullNameByUserID: {}", e.getMessage());
    }
    return fullName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Date getPostedDate() {
    return currentPage.getProperties().get(GlobalConstants.PROP_POSTED_DATE, Date.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Date getUpdatedDate() {
    return currentPage.getProperties().get(GlobalConstants.PROP_UPDATED_DATE, Date.class);
  }
}
