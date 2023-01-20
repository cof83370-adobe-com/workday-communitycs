package com.workday.community.aem.migration.services;

import java.util.List;
import java.util.Map;

import com.workday.community.aem.migration.models.PageNameBean;

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
	 * @param paramsMap the params map
	 * @param data the data
	 * @param list the list
	 */
	void doCreatePage(final Map<String, String> paramsMap, Object data, List<PageNameBean> list);
}
