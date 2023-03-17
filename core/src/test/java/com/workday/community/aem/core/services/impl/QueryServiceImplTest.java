package com.workday.community.aem.core.services.impl;

import static com.workday.community.aem.core.services.impl.QueryServiceImpl.SERVICE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.result.SearchResult;
import com.workday.community.aem.core.utils.ResolverUtil;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.day.cq.search.QueryBuilder;

import javax.jcr.Session;

import com.day.cq.search.Query;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class QueryServiceImplTest {

  QueryServiceImpl queryService = new QueryServiceImpl();

  @Mock
  QueryBuilder queryBuilder;

  @Mock
  ResourceResolverFactory resourceResolverFactory;

  @BeforeEach
  public void setup() {
    queryService.setQueryBuilder(queryBuilder);
    queryService.setResovlerFactory(resourceResolverFactory);
  }

  @Test
  void testGetNumOfTotalPages() throws Exception {
    try (MockedStatic<ResolverUtil> mockResolver = mockStatic(ResolverUtil.class)) {
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

        assertEquals(10, queryService.getNumOfTotalPages());

        // case 1
        mockResolver.when(() -> ResolverUtil.newResolver(any(), eq(SERVICE_USER))).thenThrow(new RuntimeException());
        assertEquals(0, queryService.getNumOfTotalPages());
      }
    }
  }
}
