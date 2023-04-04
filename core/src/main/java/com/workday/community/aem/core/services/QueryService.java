package com.workday.community.aem.core.services;

import java.util.List;

public interface QueryService {
    /**
	 * Gets the total number of published pages.
	 *
	 * @return The total number of pages.
	 */
    long getNumOfTotalPublishedPages();

	/**
	 * Gets the pages by templates.
	 *
	 * @return List of page path.
	 */
	List<String> getPagesByTemplates(String[] templates);
}
