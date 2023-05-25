package com.workday.community.aem.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.models.CoveoTabListModel;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class CoveoFeedFieldsServletTest {
  private final AemContext context = new AemContext();

  @Mock
  private transient ObjectMapper objectMapper;

  CoveoFeedFieldsServlet coveoFeedFieldsServlet;

  @BeforeEach
  public void setup() {
    context.registerService(objectMapper);
  }

  @Test
  public void testDoGet() {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
    ResourceResolver resourceResolver = mock(ResourceResolver.class);
    lenient().when(request.getResourceResolver()).thenReturn(resourceResolver);
    CoveoTabListModel model = mock(CoveoTabListModel.class);
    lenient().when(request.adaptTo(CoveoTabListModel.class)).thenReturn(model);

    JsonArray fields = new JsonArray();
    JsonObject field = new JsonObject();
    field.addProperty("name", "testName");
    field.addProperty("desc", "testDesc");
    fields.add(field);
    lenient().when(model.getFields()).thenReturn(fields);
    coveoFeedFieldsServlet = new CoveoFeedFieldsServlet();
    coveoFeedFieldsServlet.doGet(request, response);
    verify(request).setAttribute(anyString(), any());
  }
}