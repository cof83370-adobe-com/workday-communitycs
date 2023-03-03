package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueryServiceImplTest {
    
    QueryServiceImpl queryService = new QueryServiceImpl();
    
    @Test
    void testGetNumOfTotalPages() throws Exception {
        assertEquals(0, queryService.getNumOfTotalPages());
    }
}
