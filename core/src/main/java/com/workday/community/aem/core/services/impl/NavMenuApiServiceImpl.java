package com.workday.community.aem.core.services.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import java.util.Dictionary;

import javax.annotation.Generated;

import org.osgi.service.component.annotations.Component;

import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import com.workday.community.aem.core.config.NavMenuApiConfig;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.NavMenuApiService;
import com.workday.community.aem.core.utils.RESTAPIUtil;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.utils.restclient.APIRequest;
import com.workday.community.aem.core.utils.restclient.APIResponse;

/**
 * The Class NavMenuApiServiceImpl.
 */
@Component(service = NavMenuApiService.class, immediate = true)
@Designate(ocd = NavMenuApiConfig.class)
public class NavMenuApiServiceImpl implements NavMenuApiService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** OSGi properties dictionary. */
    Dictionary<String, Object> properties;

    /**
     * Instance of the OSGi configuration class.
     */
    @Reference
    private ConfigurationAdmin configAdmin;

    /** The resource resolver factory. */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /**
     * Reads the OSGi configuration.
     */
    protected void getConfigs() {
        logger.debug("NavMenuApiConfig Read.");
        Configuration navAPIConfig;
        try {
            navAPIConfig = configAdmin
                    .getConfiguration("com.workday.community.aem.core.config.NavMenuApiConfig");
            properties = navAPIConfig.getProperties();
        } catch (IOException e) {
            logger.error("Error in getConfigs method :: {}", e.getMessage());
        }
    }

    /**
     * Overridden method of the getUserNavigationHeaderData of NavMenuApiService.
     *
     * @param sfid SFID of logged in user.
     * @return Nav menu header data.
     */
    @Override
    public String getUserNavigationHeaderData(String sfid) {
        String jsonResponse = null;
        if (sfid.equalsIgnoreCase(StringUtils.EMPTY)) {
            return StringUtils.EMPTY;
        }
        try {
            getConfigs();
            String url = String.format(
                    properties.get(GlobalConstants.NavMenuAPIConstants.SNAP_URL_CONST).toString()
                            + properties.get(GlobalConstants.NavMenuAPIConstants.NAV_API_ENDPOINT_CONST).toString(),
                    sfid);
            String traceId = "Community AEM-" + new Date().getTime();
            // Construct the request header.
            APIRequest getUserNavigationDataReq = RESTAPIUtil.constructAPIRequestHeader(url,
                    properties.get(GlobalConstants.NavMenuAPIConstants.NAV_API_TOKEN_CONST).toString(),
                    properties.get(GlobalConstants.NavMenuAPIConstants.NAV_API_KEY_CONST).toString(), traceId);
            logger.debug("NavMenuApiServiceImpl: Calling SNAP getUserNavigationData() - " + url);

            // Execute the request.
            APIResponse snapRes = RESTAPIUtil.getRequest(getUserNavigationDataReq);

            // Gson object for json handling.
            Gson gson = new Gson();
            JsonObject navResponseObj = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);
            jsonResponse = gson.toJson(navResponseObj);
        } catch (Exception e) {
            logger.error("Error in getNavUserData method :: {}", e.getMessage());
        }
        return jsonResponse;
    }

    /**
     * Reads the fail state json from content DAM
     * 
     * @return json string of fail state data.
     * @throws Exception
     */
    public String getFailStateData() throws Exception {
        String failStateResponse = null;

        // Reading the JSON File from DAM
        ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory,
                "navserviceuser");
        Resource resource = resourceResolver.getResource("/content/dam/workday-community/FailStateHeaderData.json");
        Asset asset = resource.adaptTo(Asset.class);
        Resource original = asset.getOriginal();
        InputStream content = original.adaptTo(InputStream.class);

        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                content, StandardCharsets.UTF_8));

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        // Gson object for json handling.
        Gson gson = new Gson();
        JsonObject navResponseObj = gson.fromJson(sb.toString(), JsonObject.class);
        failStateResponse = gson.toJson(navResponseObj);
        return failStateResponse;
    }

}