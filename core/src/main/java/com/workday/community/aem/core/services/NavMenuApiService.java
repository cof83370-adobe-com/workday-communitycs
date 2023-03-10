package com.workday.community.aem.core.services;

public interface NavMenuApiService {

    /**
	 * Gets the user navigation header menu data.
	 *
	 * @return The user nav menu header data.
	 */
    public String getUserNavigationHeaderData(String sfId);
}