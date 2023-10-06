package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;

/**
 * Defines an interface for a course detail model.
 */
public interface CourseDetailModel {

  /**
   * Gets the course detail json.
   *
   * @return The course detail json for UI.
   */
  JsonObject getCourseDetailData();

}
