package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueryServiceImplTest {
    
    QueryServiceImpl queryService = new QueryServiceImpl();
    
    @Test
    void testGetNumOfTotalPages() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE, "queryserviceuser");
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        ResourceResolverFactory resourceResolverFactory = mock(ResourceResolverFactory.class);
        lenient().when(resourceResolverFactory.getServiceResourceResolver(params)).thenReturn(resourceResolver);
        if (resourceResolver != null) {
            assertEquals(0, queryService.getNumOfTotalPages());  
        }
        else {
        assertEquals(0, queryService.getNumOfTotalPages());
        }
    }

    @Test 
    void testServiceUser() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE, "queryserviceuser");
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        ResourceResolverFactory resourceResolverFactory = mock(ResourceResolverFactory.class);
        lenient().when(resourceResolverFactory.getServiceResourceResolver(params)).thenReturn(resourceResolver);
    }

}
