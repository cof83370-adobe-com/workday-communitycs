package com.workday.community.aem.core.models;

import com.google.gson.JsonObject;

public interface CourseDetailModel {
    /**
     * Gets the course detail json.
     *
     * @return The course detail json for UI.
     */
    JsonObject getCourseDetailData();
}