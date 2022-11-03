package com.workday.community.aem.core.services;

import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;

import com.workday.community.aem.core.models.EventPageData;

/**
 * The Interface PageCreationService.
 * 
 * 
 * @author pepalla
 */
public interface PageCreationService {
	
	/**
	 * Do create page.
	 *
	 * @param req the req
	 * @param paramsMap the params map
	 * @param data the data
	 */
	void doCreatePage(SlingHttpServletRequest req, final Map<String, String> paramsMap, EventPageData data);
}
