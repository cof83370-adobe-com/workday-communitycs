package com.workday.community.aem.core.services;

import com.day.cq.search.QueryBuilder;
import org.apache.sling.api.resource.ResourceResolverFactory;

public interface QueryService {
	/**
	 *
	 * @param queryBuilder Object
	 */
	void setQueryBuilder(QueryBuilder queryBuilder);

	/**
	 *
	 * @param resourceResolverFactory ResourceResolverFactory Object
	 */
	void setResovlerFactory(ResourceResolverFactory resourceResolverFactory);


    /**
	 * Gets the total number of pages.
	 *
	 * @return The total number of pages.
	 */
    public long getNumOfTotalPages();
}
