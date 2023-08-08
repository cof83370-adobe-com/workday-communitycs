package com.workday.community.aem.core.models.impl;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.lang.annotations.NotNull;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.models.CourseDetailModel;
import com.workday.community.aem.core.services.LMSService;
import com.workday.community.aem.core.services.UserGroupService;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The model implementation class for the course detail page.
 */
@Model(adaptables = {
        Resource.class,
        SlingHttpServletRequest.class
}, adapters = { CourseDetailModel.class }, resourceType = {
        HeaderModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CourseDetailModelImpl implements CourseDetailModel {
    @Self
    private SlingHttpServletRequest request;

    @Self
    private SlingHttpServletResponse response;

    /**
     * The Constant RESOURCE_TYPE.
     */
    protected static final String RESOURCE_TYPE = "workday-community/components/react/header";

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(HeaderModelImpl.class);

    /**
     * The LMS service.
     */
    @NotNull
    @OSGiService
    LMSService lmsService;

    /**
     * The UserGroup service.
     */
    @NotNull
    @OSGiService
    UserGroupService userGroupService;

    /** Course Title */
    String courseTitle;

    /** The gson service. */
    private final Gson gson = new Gson();

    @PostConstruct
    protected void init() {
        logger.debug("Initializing CourseDetailModel.");
        courseTitle = getQueryParamValueFromUrl("title");
    }

    /**
     * Get the query paramater value.
     * 
     * @param param Query parameter.
     * @return Value of the query parameter.
     */
    private String getQueryParamValueFromUrl(String param) {
        if (request.getParameter(param) != null) {
            return request.getParameter(param);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the course detail data as json string.
     * 
     * @return Course detail json string.
     */
    @Override
    public String getCourseDetailData() {
        boolean isValid = false;
        String courseDetailJson = StringUtils.EMPTY;

        try {
            courseDetailJson = lmsService.getCourseDetail(courseTitle);
            if (StringUtils.isNotBlank(courseDetailJson)) {
                // Gson object for json handling.
                JsonObject courseDetail = gson.fromJson(courseDetailJson, JsonObject.class);

                if (courseDetail.get("accessControl") != null && !courseDetail.get("accessControl").isJsonNull()) {
                    String accessControl = courseDetail.get("accessControl").getAsString();
                    List<String> accessControlTags = new ArrayList<String>(Arrays.asList(accessControl.split(",")));
                    isValid = userGroupService.checkLoggedInUserHasAccessControlTags(
                            request.getResourceResolver(),
                            accessControlTags);
                } else {
                    logger.error("Access control is empty. So, user doesn't have correct permissions.");
                }
            }
            if (!isValid) {
                ((SlingHttpServletResponse) response).setStatus(SC_FORBIDDEN);
                ((SlingHttpServletResponse) response).sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
            }
        } catch (IOException ex) {
            logger.error("Exception occurred in getCourseDetailData: {}.", ex.getMessage());
            courseDetailJson = StringUtils.EMPTY;
        }

        return courseDetailJson;
    }

}
