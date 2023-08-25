package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.LmsConfig;
import com.workday.community.aem.core.exceptions.LmsException;

public interface LmsService {
    /**
     * @param config Service configuration object for Lms API service. This
     *               method is used for programmatically pass
     *               a configuration to the service object during service activate
     *               stage.
     */
    void activate(LmsConfig config);

    /**
     * Gets the Lms API Bearer token
     * 
     * @return Bearer token.
     * @throws LmsException
     */
    String getApiToken() throws LmsException;;

    /**
     * Gets the course detail data of the given course.
     * 
     * @param courseTitle Course title.
     * @return Course detail json as string.
     * @throws LmsException
     */
    String getCourseDetail(String courseTitle) throws LmsException;
}
