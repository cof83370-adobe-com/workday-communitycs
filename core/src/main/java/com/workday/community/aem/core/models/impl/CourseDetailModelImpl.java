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
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.lang.annotations.NotNull;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.constants.WccConstants;
import com.workday.community.aem.core.exceptions.LmsException;
import com.workday.community.aem.core.models.CourseDetailModel;
import com.workday.community.aem.core.services.LmsService;
import com.workday.community.aem.core.services.UserGroupService;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

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
        CourseDetailModelImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CourseDetailModelImpl implements CourseDetailModel {
    @Self
    private SlingHttpServletRequest request;

    @SlingObject
    private SlingHttpServletResponse response;

    /**
     * The Constant RESOURCE_TYPE.
     */
    protected static final String RESOURCE_TYPE = "workday-community/components/content/training-catalog/handlebar-content";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseDetailModelImpl.class);

    /**
     * The Lms service.
     */
    @NotNull
    @OSGiService
    LmsService lmsService;

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
        LOGGER.debug("Initializing CourseDetailModel.");
        courseTitle = getQueryParamValueFromUrl("title");
    }

    /**
     * Get the query parameter value.
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
     * Gets the course detail data.
     * 
     * @return Course detail json.
     * @throws IOException
     */
    @Override
    public JsonObject getCourseDetailData() throws IOException {
        try {
            String courseDetailJson = lmsService.getCourseDetail(courseTitle);
            // Redirect to 404 page when title is not present/ empty/ response is empty.
            if (StringUtils.isBlank(courseDetailJson)) {
                response.setStatus(SC_NOT_FOUND);
                response.sendRedirect(WccConstants.PAGE_NOT_FOUND_PATH);
                return null;
            }
            // Gson object for json handling.
            JsonObject courseDetail = gson.fromJson(courseDetailJson, JsonObject.class);
            // Redirect to 403 page when logged in user doesn't have access to course.
            if (checkAccessControlTags(courseDetail)) {
                response.setStatus(SC_FORBIDDEN);
                response.sendRedirect(WccConstants.FORBIDDEN_PAGE_PATH);
                return null;
            }
            return courseDetail;

        } catch (LmsException | IOException ex) {
            LOGGER.error("Exception occurred in getCourseDetailData: {}.", ex.getMessage());
            response.setStatus(SC_NOT_FOUND);
            response.sendRedirect(WccConstants.PAGE_NOT_FOUND_PATH);
        }
        return null;
    }

    /**
     * Checks the access control tags in the course detail response against the
     * logged-in user's access control.
     * 
     * @param courseDetail Course detail object.
     * @return True if user has access to view course, else false.
     */
    private boolean checkAccessControlTags(JsonObject courseDetail) {
        JsonElement accessControl = courseDetail.get("accessControl");
        if (accessControl != null && !accessControl.isJsonNull()) {
            List<String> accessControlTags = new ArrayList<>(
                    Arrays.asList(accessControl.getAsString().split(",")));
            return userGroupService.validateCurrentUser(request, accessControlTags);
        }
        LOGGER.error("User can't access because access control is not set in the returned course detail object.");
        return false;
    }
}
