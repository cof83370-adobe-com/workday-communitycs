package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.constants.GlobalConstants.READ_SERVICE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.workday.community.aem.core.exceptions.CacheException;
import com.workday.community.aem.core.services.CacheManagerService;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.day.cq.search.QueryBuilder;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.day.cq.search.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.workday.community.aem.core.constants.GlobalConstants.USER_ROOT_PATH;
import static com.workday.community.aem.core.constants.GlobalConstants.OKTA_USER_PATH;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class QueryServiceImplTest {

  @Mock
  QueryBuilder queryBuilder;

  @Mock
  ResourceResolverFactory resourceResolverFactory;

  @Mock
  CacheManagerService cacheManager;

  @InjectMocks
  QueryServiceImpl queryService;

  MockedStatic<ResolverUtil> mockResolver;

  @BeforeEach
  public void setUp() throws Exception {
    this.mockResolver = mockStatic(ResolverUtil.class);
  }

  @Test
  void testGetNumOfTotalPages() throws Exception {
    ResourceResolver resourceResolver = mock(ResourceResolver.class);
    when(cacheManager.getServiceResolver(eq(READ_SERVICE_USER))).thenReturn(resourceResolver);

    Session session = mock(Session.class);
    lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);

    Query query = mock(Query.class);

    try (MockedStatic<PredicateGroup> mockPredicate = mockStatic(PredicateGroup.class)) {
      PredicateGroup pg = mock(PredicateGroup.class);
      mockPredicate.when(() -> PredicateGroup.create(anyMap())).thenReturn(pg);

      lenient().when(queryBuilder.createQuery(eq(pg), eq(session))).thenReturn(query);
      SearchResult result = mock(SearchResult.class);
      lenient().when(query.getResult()).thenReturn(result);
      lenient().when(result.getTotalMatches()).thenReturn(10L);

      assertEquals(10, queryService.getNumOfTotalPublishedPages());

      // case 1
      mockResolver.when(() -> ResolverUtil.newResolver(any(), eq(READ_SERVICE_USER))).thenThrow(new LoginException());
      assertEquals(10, queryService.getNumOfTotalPublishedPages());
    }
  }

  @Test
  void testPagesByTemplates() throws RepositoryException, CacheException {
    ResourceResolver resourceResolver = mock(ResourceResolver.class);
    when(cacheManager.getServiceResolver(eq(READ_SERVICE_USER))).thenReturn(resourceResolver);

    Session session = mock(Session.class);
    when(resourceResolver.adaptTo(Session.class)).thenReturn(session);

    Query query = mock(Query.class);
    when(queryBuilder.createQuery(any(), eq(session))).thenReturn(query);

    Hit hit = mock(Hit.class);
    when(hit.getPath()).thenReturn("/test/path");

    SearchResult result = mock(SearchResult.class);
    List<Hit> hitList = new ArrayList<>();
    hitList.add(hit);
    when(result.getHits()).thenReturn(hitList);

    when(query.getResult()).thenReturn(result);

    List<String> paths = queryService.getPagesByTemplates(new String[] { "template/path" });
    assertEquals("/test/path", paths.get(0));
    verify(session).logout();

  }

  @Test
  void testgetPagesByBookPath() throws RepositoryException, CacheException {
    ResourceResolver resourceResolver = mock(ResourceResolver.class);
    String hitResultPath = "/content/workday-community/en-us/thomas-sandbox/test-download-component/jcr:content/root/container/container/book/firstlevel/item1";

    when(cacheManager.getServiceResolver(eq(READ_SERVICE_USER))).thenReturn(resourceResolver);
    Session session = mock(Session.class);
    when(resourceResolver.adaptTo(Session.class)).thenReturn(session);

    Query query = mock(Query.class);
    when(queryBuilder.createQuery(any(), eq(session))).thenReturn(query);

    Hit hit = mock(Hit.class);
    when(hit.getPath()).thenReturn(hitResultPath);

    SearchResult result = mock(SearchResult.class);
    List<Hit> hitList = new ArrayList<>();
    hitList.add(hit);

    when(result.getHits()).thenReturn(hitList);

    when(query.getResult()).thenReturn(result);

    List<String> paths = queryService.getBookNodesByPath("/content/workday-community/en-us/sprint-17/cmtyaem-341",
        "/content/workday-community/en-us/products/human-capital-management/resources/next-level/faq");
    assertEquals(hitResultPath, paths.get(0));
    verify(session).logout();

  }

  @Test
  void testGetInactiveUsers() throws RepositoryException, CacheException {
    ResourceResolver resourceResolver = mock(ResourceResolver.class);
    when(cacheManager.getServiceResolver(eq(READ_SERVICE_USER))).thenReturn(resourceResolver);

    Session session = mock(Session.class);
    when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
    // Get all users.
    Query query = mock(Query.class);
    Map<String, String> queryMap = new HashMap<>();
    queryMap.put("path", USER_ROOT_PATH.concat(OKTA_USER_PATH));
    queryMap.put("type", "rep:User");
    queryMap.put("p.limit", "-1");
    MockedStatic<PredicateGroup> mockPredicate = mockStatic(PredicateGroup.class);
    PredicateGroup pg = mock(PredicateGroup.class);
    mockPredicate.when(() -> PredicateGroup.create(queryMap)).thenReturn(pg);
    when(queryBuilder.createQuery(pg, session)).thenReturn(query);
    Hit hitOne = mock(Hit.class);
    when(hitOne.getPath()).thenReturn(USER_ROOT_PATH.concat(OKTA_USER_PATH).concat("/A"));
    Hit hitTwo = mock(Hit.class);
    when(hitTwo.getPath()).thenReturn(USER_ROOT_PATH.concat(OKTA_USER_PATH).concat("/B"));
    SearchResult result = mock(SearchResult.class);
    List<Hit> hitList = new ArrayList<>();
    hitList.add(hitOne);
    hitList.add(hitTwo);
    when(result.getHits()).thenReturn(hitList);
    when(query.getResult()).thenReturn(result);
    // Get active users.
    Query queryActive = mock(Query.class);
    Map<String, String> queryMapActive = new HashMap<>();
    queryMapActive.put("path", USER_ROOT_PATH.concat(OKTA_USER_PATH));
    queryMapActive.put("type", "rep:Token");
    queryMapActive.put("relativedaterange.property", "rep:token.exp");
    queryMapActive.put("relativedaterange.lowerBound", "-1s");
    queryMapActive.put("p.limit", "-1");
    PredicateGroup pgActive = mock(PredicateGroup.class);
    mockPredicate.when(() -> PredicateGroup.create(queryMapActive)).thenReturn(pgActive);
    when(queryBuilder.createQuery(pgActive, session)).thenReturn(queryActive);
    SearchResult resultActive = mock(SearchResult.class);
    List<Hit> hitListActive = new ArrayList<>();
    Hit hit = mock(Hit.class);
    when(hit.getPath()).thenReturn(USER_ROOT_PATH.concat(OKTA_USER_PATH).concat("/A/.tokens/123"));
    hitListActive.add(hit);
    when(resultActive.getHits()).thenReturn(hitListActive);
    when(queryActive.getResult()).thenReturn(resultActive);
    
    // Verify result.
    List<String> paths = queryService.getInactiveUsers();
    assertEquals(USER_ROOT_PATH.concat(OKTA_USER_PATH).concat("/B"), paths.get(0));
    verify(session).logout();
  }

  @AfterEach
  public void after() {
    mockResolver.close();
  }
}
