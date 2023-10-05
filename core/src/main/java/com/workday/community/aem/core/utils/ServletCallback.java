package com.workday.community.aem.core.utils;

import java.io.IOException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

/**
 * The functional interface for servlet callback.
 */
public interface ServletCallback {
  String execute(SlingHttpServletRequest request, SlingHttpServletResponse response, String body) throws IOException;
}
