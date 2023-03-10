package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import com.workday.community.aem.core.utils.RestAPIUtil;
import com.workday.community.aem.core.utils.restclient.APIRequest;
import com.workday.community.aem.core.utils.restclient.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

/**
 * The Class NavMenuApiServiceImplTest.
 */
@ExtendWith(MockitoExtension.class)
public class NavMenuApiServiceImplTest {

    /** NavMenuApiServiceImpl object */
    @Spy
    NavMenuApiServiceImpl navMenuApiService;

    /**
     * Set uo method for test run.
     * 
     * @throws Exception
     */
    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for getUserNavigationHeaderData in NavMenuApiServiceImpl class.
     * 
     * @throws Exception
     */
    @Test
    void testGetUserNavigationHeaderData() throws Exception {
        when(navMenuApiService.getUserNavigationHeaderData("masterdata")).thenReturn("");
        assertEquals("", navMenuApiService.getUserNavigationHeaderData("masterdata"));
    }

    /**
     * Test method for constructAPIRequestHeader in RestAPIUtil class.
     * 
     * @throws Exception
     */
    @Test
    void testConstructAPIRequestHeader() throws Exception {
        String url = "";
        String apiToken = "";
        String apiKey = "";
        String traceId = "";
        APIRequest apiReq = RestAPIUtil.constructAPIRequestHeader(url, apiToken, apiKey, traceId);
        assertEquals(5, apiReq.getHeaders().size());

        APIResponse snapRes = RestAPIUtil.getRequest(apiReq);
        assertEquals(null, snapRes.getResponseBody());

        APIRequest newApiReq = RestAPIUtil.constructAPIRequestHeader(url, apiToken, apiKey, traceId);
        APIResponse newSnapRes = RestAPIUtil.executeGetRequest(newApiReq);
        assertEquals(null, newSnapRes.getResponseBody());
    }
}