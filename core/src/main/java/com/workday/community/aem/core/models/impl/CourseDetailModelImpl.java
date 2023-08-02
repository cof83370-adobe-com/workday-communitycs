package com.workday.community.aem.core.models.impl;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.lang.annotations.NotNull;
import com.workday.community.aem.core.models.CourseDetailModel;
import com.workday.community.aem.core.services.LMSService;

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

    /** Course Title */
    String courseTitle;

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
        if (!StringUtils.isEmpty(courseTitle)) {
            return lmsService.getCourseDetail(courseTitle);
        }
        return StringUtils.EMPTY;
    }

}
