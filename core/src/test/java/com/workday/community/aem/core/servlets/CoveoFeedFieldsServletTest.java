package com.workday.community.aem.core.servlets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DamException;
import com.workday.community.aem.core.models.TabularListViewModel;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.utils.HttpUtils;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class CoveoFeedFieldsServletTest {
  private final AemContext context = new AemContext();

  CoveoFeedFieldsServlet coveoFeedFieldsServlet;

  @Mock
  private transient ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    context.registerService(objectMapper);
  }

  @Test
  public void testDoGet() throws DamException {
    try (MockedStatic<HttpUtils> mockHttpUtils = mockStatic(HttpUtils.class)){
      SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
      SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
      ResourceResolver resourceResolver = mock(ResourceResolver.class);
      UserService userService = mock(UserService.class);
      mockHttpUtils.when(() -> HttpUtils.forbiddenResponse(request, response, userService)).thenReturn(false);

      lenient().when(request.getResourceResolver()).thenReturn(resourceResolver);
      TabularListViewModel model = mock(TabularListViewModel.class);
      lenient().when(request.adaptTo(TabularListViewModel.class)).thenReturn(model);

      JsonArray fields = new JsonArray();
      JsonObject field = new JsonObject();
      field.addProperty("name", "testName");
      field.addProperty("desc", "testDesc");
      fields.add(field);
      lenient().when(model.getFields()).thenReturn(fields);
      coveoFeedFieldsServlet = new CoveoFeedFieldsServlet();
      coveoFeedFieldsServlet.userService = userService;
      coveoFeedFieldsServlet.doGet(request, response);
      verify(request).setAttribute(anyString(), any());
    }

  }
}
