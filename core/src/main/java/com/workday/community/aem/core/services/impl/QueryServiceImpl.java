package com.workday.community.aem.core.services.impl;

import com.day.cq.search.result.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
@Component(service = QueryService.class, immediate = true)
public class QueryServiceImpl implements QueryService {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /** The query builder. */
    @Reference
    QueryBuilder queryBuilder;

    /** The resource resolver factory. */
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /** The service user. */
    public static final String SERVICE_USER = "queryserviceuser";

    @Override
    public long getNumOfTotalPages() {
        long totalResults = 0;
        Session session;
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, SERVICE_USER)) {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("path", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH);
            queryMap.put("type", "cq:Page");

            session = resourceResolver.adaptTo(Session.class);
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult result = query.getResult();
            totalResults = result.getTotalMatches();
        } catch (Exception e) {
            logger.error("Exception occurred when running query to get total number of pages {} ", e.getMessage());
        }
        return totalResults;
    }

    @Override
    public List getPagesByTemplates(String[] templates) {
        Session session = null;
        List<String> paths = new ArrayList<>();
        try (ResourceResolver resourceResolver = ResolverUtil.newResolver(resourceResolverFactory, SERVICE_USER)) {
            session = resourceResolver.adaptTo(Session.class);
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("type", "cq:Page");
            queryMap.put("group.p.or", "true");
            for (int i = 0; i < templates.length; i++) {
                queryMap.put("group." + i + "_property", "jcr:content/cq:template");
                queryMap.put("group." + i + "_property.value", templates[i]);
            }
            queryMap.put("1_property", "cq:lastReplicationAction");
            queryMap.put("1_property.value", "Activate");
            queryMap.put("p.limit", "-1");
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult searchResult = query.getResult();
            for(Hit hit : searchResult.getHits()) {
                String path = hit.getPath();
                paths.add(path);
            }
        } catch (Exception e) {
            logger.error("Exception occurred when running query to get pages {} ", e.getMessage());
        } finally {
            if(session != null) {
                session.logout();
            }
        }
        return paths;
    }
}
