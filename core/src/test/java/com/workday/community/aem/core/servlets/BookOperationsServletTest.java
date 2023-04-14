package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.services.BookOperationsService;
import com.workday.community.aem.core.services.OktaService;
import com.workday.community.aem.core.utils.HttpUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class BookOperationsServletTest {
    private final AemContext aemContext = new AemContext();

    @Mock
    BookOperationsService bookOperationsService;
    @InjectMocks
    BookOperationsServlet bookOperationsServlet;
    MockSlingHttpServletRequest mockSlingRequest;
    MockSlingHttpServletResponse mockSlingResponse;

    @BeforeEach
    void setUp() throws Exception {
        mockSlingRequest = aemContext.request();
        mockSlingResponse = aemContext.response();
    }

    @Test
    public void testBookOperationServletDoGet() throws Exception {
        bookOperationsServlet.doGet(mockSlingRequest, mockSlingResponse);
        assertNotNull(mockSlingResponse);     
    }
}
