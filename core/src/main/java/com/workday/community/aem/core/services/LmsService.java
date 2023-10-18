package com.workday.community.aem.core.services;

import com.workday.community.aem.core.config.LmsConfig;
import com.workday.community.aem.core.exceptions.LmsException;

/**
 * Defines an interface for the LMS service.
 */
public interface LmsService {

  /**
   * Activates the LMS service.
   *
   * @param config Service configuration object for Lms API service. This
   *               method is used for programmatically pass
   *               a configuration to the service object during service activate
   *               stage.
   */
  void activate(LmsConfig config);

  /**
   * Gets the Lms API Bearer Token required for course list and course detail
   * APIs.
   *
   * @return Bearer token.
   * @throws LmsException LmsException Object.
   */
  String getApiToken() throws LmsException;

  /**
   * Makes LMS API call and fetches the course detail data of the given course.
   *
   * @param courseTitle Course title.
   * @return Course detail json as string.
   * @throws LmsException LmsException object.
   */
  String getCourseDetail(String courseTitle) throws LmsException;

}
