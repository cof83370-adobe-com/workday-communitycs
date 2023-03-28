package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.services.CoveoPushApiService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class CoveoDeleteAllServletTest {

    /** The push API service. */
    @Mock
    private CoveoPushApiService coveoPushApiService;

    @InjectMocks
    CoveoDeleteAllServlet servlet;

    @Test
    public void testDoDelete() throws IOException {
        MockSlingHttpServletRequest request = mock(MockSlingHttpServletRequest.class);
        MockSlingHttpServletResponse response = mock(MockSlingHttpServletResponse.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        servlet.doDelete(request, response);
        verify(coveoPushApiService).callDeleteAllItemsUri();
        verify(printWriter).append("Delete all request sent.");
    }

}