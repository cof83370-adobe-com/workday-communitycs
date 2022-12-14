package com.workday.community.aem.core.services;

import java.util.List;
import java.util.Map;

import com.workday.community.aem.core.models.EventPageData;
import com.workday.community.aem.core.models.PageNameBean;

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
	 * @throws Exception 
	 */
	void doCreatePage(final Map<String, String> paramsMap, EventPageData data, List<PageNameBean> list);
}
