package com.workday.community.aem.core.services.impl;

import com.day.cq.dam.api.Asset;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.config.SnapConfig;
import com.workday.community.aem.core.pojos.ProfilePhoto;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.RestApiUtil;
import com.workday.community.aem.core.utils.ResolverUtil;
import com.workday.community.aem.core.utils.restclient.APIRequest;
import com.workday.community.aem.core.utils.restclient.APIResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component(service = SnapService.class, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
@Designate(ocd = SnapConfig.class)
public class SnapServiceImpl implements SnapService {
    private final static Logger logger = LoggerFactory.getLogger(SnapService.class);

    /** The resource resolver factory. */
    @Reference
    ResourceResolverFactory resResolverFactory;

    private SnapConfig config;

    private ObjectMapper objectMapper;

    @Activate
    @Modified
    protected void activate(SnapConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        logger.info("SnapService is activated.");
    }

    @Deactivate
    protected void deactivate() {
        logger.info("SnapService is de-activated.");
    }

    public String getUserHeaderMenu(String sfId) {
        String jsonResponse;

        try {
            String url = String.format(config.snapUrl() + config.navApi(), sfId);
            String traceId = "Community AEM-" + new Date().getTime();
            // Construct the request header.
            APIRequest getUserNavigationDataReq = RestApiUtil.constructAPIRequestHeader(url,
                    config.navApiToken(), config.navApiKey(), traceId);
            logger.debug("NavMenuApiServiceImpl: Calling SNAP getUserNavigationData() - " + url);

            // Execute the request.
            APIResponse snapRes = RestApiUtil.getRequest(getUserNavigationDataReq);

            // Gson object for json handling.
            Gson gson = new Gson();
            JsonObject navResponseObj = gson.fromJson(snapRes.getResponseBody(), JsonObject.class);
            jsonResponse = gson.toJson(navResponseObj);
        } catch (Exception e) {
            logger.error("Error in getNavUserData method :: {}", e.getMessage());
            return this.getFailStateHeaderMenu();
        }
        return jsonResponse;
    }

    @Override
    public ProfilePhoto getProfilePhoto(String userId) {
        String url = String.format(config.snapUrl()+config.sfdc_get_photo_url(), userId);
        try {
            logger.info("SnapImpl: Calling SNAP getProfilePhoto()..."+config.snapUrl()+config.sfdc_get_photo_url());
            String jsonResponse = RestApiUtil.requestSnapJsonResponse(url, config.sfdc_get_photo_token(), config.sfdc_api_key());
            if (jsonResponse != null ) {
                ProfilePhoto profilePhoto = objectMapper.readValue(jsonResponse, ProfilePhoto.class);
                return profilePhoto;
            }
        } catch (Exception e) {
            logger.error("Error in getProfilePhoto method, {} ",e.getMessage());
        }
        return null;
    }

    private String getFailStateHeaderMenu() {
        String failStateResponse;

        // Reading the JSON File from DAM
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = ResolverUtil.newResolver(resResolverFactory,
                    "navserviceuser");
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }

        Resource resource = resourceResolver.getResource("/content/dam/workday-community/FailStateHeaderData.json");
        Asset asset = resource.adaptTo(Asset.class);
        Resource original = asset.getOriginal();
        InputStream content = original.adaptTo(InputStream.class);

        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                content, StandardCharsets.UTF_8));

        while (true) {
            try {
                if ((line = br.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sb.append(line);
        }

        // Gson object for json handling.
        Gson gson = new Gson();
        JsonObject navResponseObj = gson.fromJson(sb.toString(), JsonObject.class);
        failStateResponse = gson.toJson(navResponseObj);
        return failStateResponse;
    }
}
