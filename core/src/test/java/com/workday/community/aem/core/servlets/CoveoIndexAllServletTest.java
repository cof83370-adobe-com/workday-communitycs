package com.workday.community.aem.core.servlets;

import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import com.workday.community.aem.core.services.IndexServices;
import com.workday.community.aem.core.services.QueryService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * The Class CoveoIndexAllServletTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
class CoveoIndexAllServletTest {

    /** Index service. */
    @Mock
    private IndexServices indexServices;

    /** Query service. */
    @Mock
    private QueryService queryService;

    /** CoveoIndexApiConfigService service. */
    @Mock
    private CoveoIndexApiConfigService coveoIndexApiConfigService;

    /** The servlet CoveoIndexAllServlet. */
    @InjectMocks
    CoveoIndexAllServlet servlet;

    /**
     * Test doPost.
     */
    @ParameterizedTest(name = "Test {0}")
    @CsvSource({
            "Template value is empty,true,false",
            "No content found,false,true",
            "Contents indexed,false,false"
    })
    public void doPostTest(String test, boolean emptyTemplate, boolean noContent) throws IOException {
        MockSlingHttpServletRequest request = mock(MockSlingHttpServletRequest.class);
        String[] templates;
        if (emptyTemplate) {
            templates = null;
        } else {
            templates = new String[]{"/template/path"};
        }

        when(request.getParameterValues("templates")).thenReturn(templates);

        MockSlingHttpServletResponse response = mock(MockSlingHttpServletResponse.class);

        when(coveoIndexApiConfigService.isCoveoIndexEnabled()).thenReturn(true);

        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        List<String> paths = new ArrayList<>();
        if (!noContent) {
            paths.add("/page/path");
        }

        if (!emptyTemplate) {
            when(queryService.getPagesByTemplates(templates)).thenReturn(paths);
        }

        servlet.doPost(request, response);

        if (emptyTemplate) {
            verify(printWriter).append("Missing template value.");
        } else if (noContent) {
            verify(printWriter).append("No matching items found.");
        } else {
            verify(printWriter).append("1 number of content(s) indexed.");
        }
    }

}