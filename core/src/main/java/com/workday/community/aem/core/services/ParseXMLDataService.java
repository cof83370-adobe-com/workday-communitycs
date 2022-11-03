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
	 * Read XML from jcr source.
	 *
	 * @param <T> the generic type
	 * @param resolver the resolver
	 * @param paramsMap the params map
	 * @param clazz the clazz
	 * @return the event pages list
	 */
	 public <T> T readXMLFromJcrSource(ResourceResolver resolver, Map<String, String> paramsMap, Class<T> clazz);
}
