package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.LMSConfig;

public interface LMSService {
    /**
     * @param config Service configuration object for LMS API service. This
     *               method is used for programmatically pass
     *               a configuration to the service object during service activate
     *               stage.
     */
    void activate(LMSConfig config);

    /**
     * Gets the LMS API Bearer token
     * 
     * @return Bearer token.
     */
    String getLMSAPIToken();

    /**
     * Gets the course detail data of the given course.
     * 
     * @param courseTitle Course title.
     * @return Course detail json as string.
     */
    String getCourseDetail(String courseTitle);

}
