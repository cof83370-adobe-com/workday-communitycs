package com.workday.community.aem.core.servlets;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class CoveoEventsTypeServletTest {


  @InjectMocks
  CoveoEventsTypeServlet coveoEventTypeServlet;

  @Test
  public void testDoGet() throws IOException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);
    coveoEventTypeServlet.doGet(request, response);
  }
}