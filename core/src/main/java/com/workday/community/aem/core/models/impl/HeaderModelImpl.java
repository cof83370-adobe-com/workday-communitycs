package com.workday.community.aem.core.models.impl;

import com.drew.lang.annotations.NotNull;
import com.workday.community.aem.core.models.HeaderModel;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.OurmUtils;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
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

    @NotNull
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
        if (resourceResolver == null) {
            logger.error("ResourceResolver is not injected (null) in HeaderModelImpl init method.");
            throw new RuntimeException();
        }

        sfId = OurmUtils.getSalesForceId(resourceResolver);
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
        return this.snapService.getUserHeaderMenu(sfId);
    }

    public String getUserAvatarUrl() {
        String base64ImageURL;
        String extension;

        try {
              ProfilePhoto photoAPIResponse = this.snapService.getProfilePhoto(sfId);
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