package com.workday.community.aem.core.services;

import java.util.HashMap;

/**
 * The CoveoSourceApiService interface.
 */
public interface CoveoSourceApiService {

	/**
	 * Generate api uri.
	 *
	 * @return The api uri
	 */
    public String generateSourceApiUri();

	/**
	 * Call Api.
	 *
	 * @return The api response
	 */
    public HashMap<String, Object> callApi();

    /**
	 * Get total indexed number.
	 *
	 * @return The number of indexed pages
	 */
    public long getTotalIndexedNumber();
}
