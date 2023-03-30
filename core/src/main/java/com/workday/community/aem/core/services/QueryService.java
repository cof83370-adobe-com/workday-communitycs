package com.workday.community.aem.core.services;

import java.util.List;

public interface QueryService {
    /**
	 * Gets the total number of pages.
	 *
	 * @return The total number of pages.
	 */
    long getNumOfTotalPages();

	/**
	 * Gets the pages by templates.
	 *
	 * @return List of page path.
	 */
	List getPagesByTemplates(String[] templates);
}
