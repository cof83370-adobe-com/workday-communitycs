package com.workday.community.aem.core.services;

/**
 * The Interface OurmUsersApiConfigService.
 */
public interface OurmUsersApiConfigService {
    
    /**
     * Gets the search field lookup API.
     *
     * @return the search field lookup API
     */
    String getSearchFieldLookupAPI();

    /**
     * Gets the search field consumer key.
     *
     * @return the search field consumer key
     */
    String getSearchFieldConsumerKey();

    /**
     * Gets the search field consumer secret.
     *
     * @return the search field consumer secret
     */
    String getSearchFieldConsumerSecret();

}
