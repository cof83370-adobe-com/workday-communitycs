package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.services.impl.QueryServiceImpl.SERVICE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
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
import java.util.List;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class QueryServiceImplTest {

  @Mock
  QueryBuilder queryBuilder;

  @Mock
  ResourceResolverFactory resourceResolverFactory;

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
    mockResolver.when(() -> ResolverUtil.newResolver(any(), eq(SERVICE_USER))).thenReturn(resourceResolver);

    Session session = mock(Session.class);
    lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);

    Query query = mock(Query.class);

    try (MockedStatic<PredicateGroup> mockPredicate = mockStatic(PredicateGroup.class)) {
      PredicateGroup pg = mock(PredicateGroup.class);
      mockPredicate.when(() -> PredicateGroup.create(anyMap())).thenReturn(pg);

      lenient().when(queryBuilder.createQuery(eq(pg), eq(session))).thenReturn(query);
      SearchResult result = mock(SearchResult.class);
      lenient().when(query.getResult()).thenReturn(result);
      lenient().when(result.getTotalMatches()).thenReturn(10l);

      assertEquals(10, queryService.getNumOfTotalPublishedPages());

      // case 1
      mockResolver.when(() -> ResolverUtil.newResolver(any(), eq(SERVICE_USER))).thenThrow(new RuntimeException());
      assertEquals(0, queryService.getNumOfTotalPublishedPages());
    }
  }

  @Test
  void testPagesByTemplates() throws RepositoryException {
    ResourceResolver resourceResolver = mock(ResourceResolver.class);
    mockResolver.when(() -> ResolverUtil.newResolver(any(), eq(SERVICE_USER))).thenReturn(resourceResolver);

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

    List<String> paths = queryService.getPagesByTemplates(new String[]{"template/path"});
    assertEquals("/test/path", paths.get(0));
    verify(session).logout();

  }

  @AfterEach
  public void after() {
    mockResolver.close();
  }
}
