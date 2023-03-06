package com.workday.community.aem.core.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.workday.community.aem.core.constants.GlobalConstants;
import com.workday.community.aem.core.services.QueryService;
import com.workday.community.aem.core.utils.ResolverUtil;

import javax.jcr.Session;

/**
 * The Class QueryServiceImpl.
 */
@Component(
    service = QueryService.class,
    immediate = true
)
public class QueryServiceImpl implements QueryService{
    
    /** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The query builder. */
    @Reference
    QueryBuilder queryBuilder;

    /** The resource resolver factory. */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Override
    public long getNumOfTotalPages(){
        long totalResults = 0;
        Session session = null;
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory)) {
            Map<String,String> queryMap = new HashMap<>();
            queryMap.put("path", GlobalConstants.CONTENT_PATH);
            queryMap.put("type","cq:Page");

            session = resourceResolver.adaptTo(Session.class);
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult result = query.getResult();
            totalResults = (long) result.getTotalMatches();
            resourceResolver.close();
        }
        catch (Exception e){
            logger.error("Exception occured when running query to get total number of pages {} ", e.getMessage());
        }
        return totalResults;
    }
}
