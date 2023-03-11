package com.workday.community.aem.core.services;

public interface NavMenuApiService {

	/**
	 * Gets the user navigation header menu data.
	 *
	 * @return The user nav menu header data.
	 */
	public String getUserNavigationHeaderData(String sfId);

	/**
	 * Reads the fail state json from content DAM
	 * 
	 * @return json string of fail state data.
	 * @throws Exception
	 */
	public String getFailStateData() throws Exception;
}