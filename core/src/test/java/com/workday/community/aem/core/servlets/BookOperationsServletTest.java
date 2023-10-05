package com.workday.community.aem.core.servlets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.workday.community.aem.core.services.BookOperationsService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class BookOperationsServletTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class BookOperationsServletTest {

    /** The aem context. */
    private final AemContext aemContext = new AemContext();

    /** The book operations service. */
    @Mock
    BookOperationsService bookOperationsService;

    /** The book operations servlet. */
    @InjectMocks
    BookOperationsServlet bookOperationsServlet;

    /** The mock sling request. */
    MockSlingHttpServletRequest mockSlingRequest;

    /** The mock sling response. */
    MockSlingHttpServletResponse mockSlingResponse;

    /**
     * Setup for the test
     *
     * @throws Exception the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        mockSlingRequest = aemContext.request();
        mockSlingResponse = aemContext.response();
    }

    /**
     * Test book operation servlet do get.
     *
     * @throws Exception the exception
     */
    @Test
    public void testBookOperationServletDoGet() throws Exception {
        bookOperationsServlet.doPost(mockSlingRequest, mockSlingResponse);
        assertNotNull(mockSlingResponse);
    }
}
