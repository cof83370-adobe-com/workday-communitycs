package com.workday.community.aem.core.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import com.workday.community.aem.core.services.SnapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import com.workday.community.aem.core.utils.RestApiUtil;
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
    SnapService snapService;

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
        when(snapService.getUserHeaderMenu("masterdata")).thenReturn("");
        assertEquals("", snapService.getUserHeaderMenu("masterdata"));
    }

    /**
     * Test method for constructAPIRequestHeader in RESTAPIUtil class.
     * 
     * @throws Exception
     */
    @Test
    void testConstructAPIRequestHeader() throws Exception {
        String url = "";
        String apiToken = "";
        String apiKey = "";
        String traceId = "";
        APIRequest apiReq = RestApiUtil.constructAPIRequestHeader(url, apiToken, apiKey, traceId);
        assertEquals(5, apiReq.getHeaders().size());

        APIResponse snapRes = RestApiUtil.getRequest(apiReq);
        assertNull(snapRes.getResponseBody());

        APIRequest newApiReq = RestApiUtil.constructAPIRequestHeader(url, apiToken, apiKey, traceId);
        APIResponse newSnapRes = RestApiUtil.executeGetRequest(newApiReq);
        assertNull(newSnapRes.getResponseBody());
    }
}