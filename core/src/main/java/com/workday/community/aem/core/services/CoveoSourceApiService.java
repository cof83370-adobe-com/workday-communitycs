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
  String generateSourceApiUri();

  /**
   * Call Api.
   *
   * @return The api response
   */
  HashMap<String, Object> callApi();

  /**
   * Get total indexed number.
   *
   * @return The number of indexed pages
   * @see <a href="https://docs.coveo.com/en/65/index-content/get-detailed-information-about-a-source">Coveo reference.</a>
   */
  long getTotalIndexedNumber();

}
