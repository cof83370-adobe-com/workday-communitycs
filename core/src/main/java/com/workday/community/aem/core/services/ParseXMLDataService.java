/**
 * 
 */
package com.workday.community.aem.core.services;

import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * The Interface ParseXMLDataService.
 *
 * @author pepalla
 */
public interface ParseXMLDataService {
	/**
	 * Read xml from jcr and delegate to page creation service.
	 *
	 * @param resolver the resolver
	 * @param paramsMap the params map
	 * @param templatePath the template path
	 * @return the string
	 */
	void readXmlFromJcrAndDelegateToPageCreationService(ResourceResolver resolver, Map<String, String> paramsMap,
			String templatePath);
}
