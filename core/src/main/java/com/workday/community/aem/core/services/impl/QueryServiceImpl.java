package com.workday.community.aem.core.services.impl;

import com.day.cq.search.result.Hit;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import org.apache.commons.lang3.StringUtils;
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

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.day.cq.wcm.api.constants.NameConstants.NT_PAGE;
import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static com.workday.community.aem.core.constants.GlobalConstants.USER_ROOT_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.OKTA_USER_PATH;

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

    @Reference
    CacheManagerService cacheManager;

    @Override
    public long getNumOfTotalPublishedPages() {
        long totalResults = 0;
        Session session = null;
        try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(READ_SERVICE_USER)) {
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("path", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH);
            queryMap.put("type", NT_PAGE);
            queryMap.put("1_property", "jcr:content/cq:lastReplicationAction");
            queryMap.put("1_property.value", "Activate");

            session = resourceResolver.adaptTo(Session.class);
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult result = query.getResult();
            totalResults = result.getTotalMatches();
        } catch (CacheException e) {
            logger.error("Exception occurred when running query to get total number of pages {} ", e.getMessage());
        } finally {
            if (session != null) {
                session.logout();
            }
        }
        return totalResults;
    }

    @Override
    public List<String> getPagesByTemplates(String[] templates) {
        Session session = null;
        List<String> paths = new ArrayList<>();
        try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(READ_SERVICE_USER)) {
            session = resourceResolver.adaptTo(Session.class);
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("path", GlobalConstants.COMMUNITY_CONTENT_ROOT_PATH);
            queryMap.put("type", NT_PAGE);
            queryMap.put("group.p.or", "true");
            for (int i = 0; i < templates.length; i++) {
                queryMap.put(String.format("group.%d_property", i), "jcr:content/cq:template");
                queryMap.put(String.format("group.%d_property.value", i), templates[i]);
            }
            queryMap.put("1_property", "jcr:content/cq:lastReplicationAction");
            queryMap.put("1_property.value", "Activate");
            queryMap.put("p.limit", "-1");
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult searchResult = query.getResult();
            for (Hit hit : searchResult.getHits()) {
                String path = hit.getPath();
                paths.add(path);
            }
        } catch (CacheException | RepositoryException e) {
            logger.error("Exception occurred when running query to get pages {} ", e.getMessage());
        } finally {
            if (session != null) {
                session.logout();
            }
        }
        return paths;
    }

    @Override
    public List<String> getInactiveUsers() {
        Session session = null;
        List<String> users = new ArrayList<>();
        try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(READ_SERVICE_USER)) {
            session = resourceResolver.adaptTo(Session.class);

            // Get all users.
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("path", USER_ROOT_PATH.concat(OKTA_USER_PATH));
            queryMap.put("type", "rep:User");
            queryMap.put("p.limit", "-1");
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult searchResult = query.getResult();
            for (Hit hit : searchResult.getHits()) {
                String path = hit.getPath();
                users.add(path);
            }

            // Get active users.
            Map<String, String> queryMapActive = new HashMap<>();
            queryMapActive.put("path", USER_ROOT_PATH.concat(OKTA_USER_PATH));
            queryMapActive.put("type", "rep:Token");
            queryMapActive.put("relativedaterange.property", "rep:token.exp");
            queryMapActive.put("relativedaterange.lowerBound", "-1s");
            queryMapActive.put("p.limit", "-1");
            Query queryActive = queryBuilder.createQuery(PredicateGroup.create(queryMapActive), session);
            SearchResult searchResultActive = queryActive.getResult();
            for (Hit hit : searchResultActive.getHits()) {
                String path = hit.getPath();
                path = path.substring(0, path.indexOf("/.tokens"));
                // Remove active users.
                users.remove(path);
            }
        } catch (CacheException | RepositoryException e) {
            logger.error("Exception occurred when running query to get inactive users {} ", e.getMessage());
        } finally {
            if (session != null) {
                session.logout();
            }
        }
        return users;

    }

    /**
     * Gets the book nodes by path.
     *
     * @param bookPagePath the book page path
     * @param currentPath  the current path
     * @return the book nodes by path
     */
    @Override
    public List<String> getBookNodesByPath(String bookPagePath, String currentPath) {
        Session session = null;
        List<String> paths = new ArrayList<>();
        try (ResourceResolver resourceResolver = cacheManager.getServiceResolver(READ_SERVICE_USER)) {
            session = resourceResolver.adaptTo(Session.class);
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("path", GlobalConstants.COMMUNITY_CONTENT_BOOK_ROOT_PATH);
            queryMap.put("fulltext", bookPagePath);
            queryMap.put("p.limit", "-1");
            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult searchResult = query.getResult();
            for (Hit hit : searchResult.getHits()) {
                String path = hit.getPath();
                if (StringUtils.isNotEmpty(currentPath) && path.contains(currentPath)) {
                    continue;
                }
                paths.add(path);
            }
        } catch (CacheException | RepositoryException e) {
            logger.error("Exception occurred when running query to get book pages {} ", e.getMessage());
        } finally {
            if (session != null) {
                session.logout();
            }
        }
        return paths;
    }
}
