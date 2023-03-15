package com.workday.community.aem.core.models.impl;

import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * The class NavHeaderModelImpl.
 */
@Model(
        adaptables = {
                Resource.class,
                SlingHttpServletRequest.class
        },
        adapters = {HeaderModel.class},
        resourceType = {HeaderModelImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class HeaderModelImpl implements HeaderModel {

    /**
     * The Constant RESOURCE_TYPE.
     */
    protected static final String RESOURCE_TYPE = "workday-community/components/react/header";

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(HeaderModelImpl.class);

    @SlingObject
    private ResourceResolver resourceResolver;

    /**
     * The navMenuApi service.
     */

    @NonNull
    @OSGiService
    SnapService snapService;

    String sfId;

    @PostConstruct
    protected void init() {
        logger.debug("Initializing HeaderModel ....");

        Session session = resourceResolver.adaptTo(Session.class);
        UserManager userManager = resourceResolver.adaptTo(UserManager.class);

        try {
            User user = (User) userManager.getAuthorizable(session.getUserID());
            sfId = user.getProperty(GlobalConstants.WRCConstants.PROFILE_SOURCE_ID) != null ?
                    user.getProperty(GlobalConstants.WRCConstants.PROFILE_SOURCE_ID)[0].getString() : null;
        } catch (RepositoryException e) {
            logger.error("Exception in init HeaderModelImpl method: %s", e.getMessage());
        }

        if (StringUtils.isBlank(sfId)) {
            // Default fallback
            logger.debug("Salesforce Id for current user is unavailable");
            sfId = "masterdata";
        }
    }

    /**
     * Calls the NavMenuApiService to get header menu data.
     *
     * @return Nav menu as string.
     */
    public String getUserHeaderMenus() {
        return snapService.getUserHeaderMenu(sfId);
    }

    public String getUserAvatarUrl() {
        String base64ImageURL;
        String extension;

        try {
            ProfilePhoto photoAPIResponse = snapService.getProfilePhoto(sfId);
            if (StringUtils.isNotBlank(photoAPIResponse.getPhotoVersionId())) {
                if (photoAPIResponse.getBase64content().contains("data:image/")) {
                    return photoAPIResponse.getBase64content();
                }

                int lastIndex = photoAPIResponse.getFileNameWithExtension().lastIndexOf('.');
                extension = photoAPIResponse.getFileNameWithExtension().substring(lastIndex + 1).toLowerCase();
                base64ImageURL = "data:image/" + extension + ";base64," + photoAPIResponse.getBase64content();

                return base64ImageURL;
            }

        } catch (Exception e) {
            logger.error("Exception in getUserAvatarUrl method = {}, {}", e.getClass().getName(), e.getMessage());
        }

        return "";
    }
}