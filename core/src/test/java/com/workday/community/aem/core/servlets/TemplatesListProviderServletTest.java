package com.workday.community.aem.core.servlets;

import static com.workday.community.aem.core.servlets.TemplatesListProviderServlet.TEMPLATES_PATH;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.workday.community.aem.core.services.CoveoIndexApiConfigService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.List;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class TemplatesListProviderServletTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class TemplatesListProviderServletTest {

  @InjectMocks
  TemplatesListProviderServlet servlet;

  @Mock
  private transient CoveoIndexApiConfigService coveoIndexApiConfigService;

  @Test
  void testDoGet() {
    MockSlingHttpServletRequest request = mock(MockSlingHttpServletRequest.class);
    MockSlingHttpServletResponse response = mock(MockSlingHttpServletResponse.class);
    ResourceResolver resourceResolverMock = mock(ResourceResolver.class);
    when(request.getResourceResolver()).thenReturn(resourceResolverMock);
    Resource resourceMock = mock(Resource.class);
    Resource resourceMock1 = mock(Resource.class);
    when(resourceMock1.getName()).thenReturn("Title1");
    when(resourceMock1.getPath()).thenReturn("/path/template1");
    Resource resourceMock2 = mock(Resource.class);
    when(resourceMock2.getName()).thenReturn("Title2");
    when(resourceMock2.getPath()).thenReturn("/path/template2");
    List<Resource> mockList = List.of(resourceMock1, resourceMock2);
    when(coveoIndexApiConfigService.isCoveoIndexEnabled()).thenReturn(true);
    when(resourceMock.listChildren()).thenReturn(mockList.listIterator());
    when(resourceResolverMock.getResource(TEMPLATES_PATH)).thenReturn(resourceMock);
    servlet.doGet(request, response);
    verify(request).setAttribute(eq(DataSource.class.getName()), any(SimpleDataSource.class));
  }

}
