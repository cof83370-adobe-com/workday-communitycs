package com.workday.community.aem.core.servlets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.CoveoPushApiService;
import com.workday.community.aem.core.services.IndexServices;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class CoveoDeleteAllServletTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class CoveoDeleteAllServletTest {

    /** Index service. */
    @Mock
    private IndexServices indexServices;

    /** The push API service. */
    @Mock
    private CoveoPushApiService coveoPushApiService;

    /** The servlet CoveoDeleteAllServlet. */
    @InjectMocks
    CoveoDeleteAllServlet servlet;

    /** The CoveoIndexApiConfigService service. */
    @Mock
    private CoveoIndexApiConfigService coveoIndexApiConfigService;

    /**
     * Test doDelete.
     */
    @Test
    public void testDoDelete() throws IOException {
        MockSlingHttpServletRequest request = mock(MockSlingHttpServletRequest.class);
        MockSlingHttpServletResponse response = mock(MockSlingHttpServletResponse.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        when(coveoIndexApiConfigService.isCoveoIndexEnabled()).thenReturn(true);

        servlet.doDelete(request, response);
        verify(coveoPushApiService).callDeleteAllItemsUri();
        verify(printWriter).append("Delete all request sent.");
    }

}
