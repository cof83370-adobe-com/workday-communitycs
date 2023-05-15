package com.workday.community.aem.core.utils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import java.io.IOException;

public interface ServletCallback {
  String execute(SlingHttpServletRequest request, SlingHttpServletResponse response, String body) throws IOException;
}