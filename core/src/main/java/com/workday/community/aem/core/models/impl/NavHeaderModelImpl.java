package com.workday.community.aem.core.models.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;

import com.workday.community.aem.core.models.NavHeaderModel;
import com.workday.community.aem.core.services.NavMenuApiService;

/**
 * The class NavHeaderModelImpl.
 */
@Model(adaptables = { Resource.class, SlingHttpServletRequest.class }, adapters = {
        NavHeaderModel.class }, resourceType = {
                NavHeaderModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class NavHeaderModelImpl implements NavHeaderModel {

    /** The Constant RESOURCE_TYPE. */
    protected static final String RESOURCE_TYPE = "workday-community/components/react/header";

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The navMenuApi service. */
    @OSGiService
    NavMenuApiService navMenuApiService;

    /**
     * Calls the NavMenuApiService to get header menu data.
     *
     * @return Nav menu as string.
     */
    public String getUserNavigationHeaderMenu() {
        String sfid = StringUtils.EMPTY;

        sfid = "masterdata";
        // Add logic to fetch SFID of logged in user here.

        logger.trace("User id : {}", sfid);

        String userNavResponse = StringUtils.EMPTY;
        if (sfid.equalsIgnoreCase(StringUtils.EMPTY)) {
            return userNavResponse;
        }

        // Check if SFID exists
        try {

            userNavResponse = navMenuApiService.getUserNavigationHeaderData(sfid);

        } catch (RuntimeException e) {
            logger.error("NavHeader Response Exception: " + e.getMessage());
        }

        logger.trace("Nav menu API Response : {}", userNavResponse);
        return userNavResponse;
    }
}