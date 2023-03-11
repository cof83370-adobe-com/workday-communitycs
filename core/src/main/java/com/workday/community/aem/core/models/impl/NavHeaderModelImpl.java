package com.workday.community.aem.core.models.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.osgi.service.component.annotations.Reference;

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

    @Reference
    private ResourceResolverFactory resolverFactory;

    /**
     * Calls the NavMenuApiService to get header menu data.
     *
     * @return Nav menu as string.
     */
    public String getUserNavigationHeaderMenu() {
        String sfid = StringUtils.EMPTY;

        sfid = "masterdata";
        // TODO: Add logic to fetch SFID of logged in user here.

        logger.trace("User id : {}", sfid);

        String userNavResponse = StringUtils.EMPTY;
        if (sfid.equalsIgnoreCase(StringUtils.EMPTY)) {
            return userNavResponse;
        }

        try {
            userNavResponse = navMenuApiService.getUserNavigationHeaderData(sfid);

            // If there is an error in getting the data from service call,
            // read the fail state data from DAM.
            if (StringUtils.isEmpty(userNavResponse) || userNavResponse.equals("null")) {
                userNavResponse = navMenuApiService.getFailStateData();
            }
        } catch (Exception e) {
            logger.error("NavHeader Response Exception: " + e.getMessage());
        }

        logger.trace("Nav menu API Response : {}", userNavResponse);
        return userNavResponse;
    }
}