package com.workday.community.aem.core.servlets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.pojos.SpeakerPojo;
import com.workday.community.aem.core.pojos.Speakers;
import com.workday.community.aem.core.services.SpeakersApiConfigService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class SpeakersServletTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class SpeakersServletTest {
  
  /** The context. */
  private final AemContext context = new AemContext();

  /** The speakers api config service. */
  @Mock
  SpeakersApiConfigService speakersApiConfigService;

  /** The object mapper. */
  @Mock
  private transient ObjectMapper objectMapper;

  /** The speakers servlet. */
  @InjectMocks
  SpeakersServlet speakersServlet;

  /**
   * Setup.
   */
  @BeforeEach
  public void setup() {
    context.registerService(objectMapper);
  }

  /**
   * Test do get.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testDoGet() throws IOException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

    try (MockedStatic<HttpClients> httpClientsMockedStatic = mockStatic(HttpClients.class)) {

      CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
      httpClientsMockedStatic.when(HttpClients::createDefault).thenReturn(httpClient);
      lenient().when(speakersApiConfigService.getSearchFieldLookupAPI())
          .thenReturn("https://den.community-workday.com/user/search/");
      lenient().when(speakersApiConfigService.getSearchFieldConsumerKey())
          .thenReturn("r4hd9dxB9ToJWYBQpJAhUauGXoh4r35r");
      lenient().when(speakersApiConfigService.getSearchFieldConsumerSecret())
          .thenReturn("Gx9qk47hwzubLymkfyv4xCS42oTJiDMv");
      when(request.getParameter("searchText")).thenReturn("dav");

      CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
      StatusLine statusLine = mock(StatusLine.class);
      HttpEntity entity = mock(HttpEntity.class);
      InputStream inputStream = mock(InputStream.class);
      lenient().when(httpClient.execute(any())).thenReturn(httpResponse);
      lenient().when(httpResponse.getStatusLine()).thenReturn(statusLine);
      lenient().when(httpResponse.getEntity()).thenReturn(entity);
      lenient().when(entity.getContent()).thenReturn(inputStream);

      lenient().when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
      HashMap<String, String> result = new HashMap<>();
      result.put("token", "testToken");
      Speakers speakers = new Speakers();
      String profileImageData = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGNsaXAtcnVsZT0iZXZlbm9kZCIgZD0iTTE2IDMyQzI0LjgzNjYgMzIgMzIgMjQuODM2NiAzMiAxNkMzMiA3LjE2MzQ0IDI0LjgzNjYgMCAxNiAwQzcuMTYzNDQgMCAwIDcuMTYzNDQgMCAxNkMwIDI0LjgzNjYgNy4xNjM0NCAzMiAxNiAzMloiIGZpbGw9IiMwMDVDQjkiLz4KICA8bWFzayBpZD0ibWFzazAiIG1hc2stdHlwZT0iYWxwaGEiIG1hc2tVbml0cz0idXNlclNwYWNlT25Vc2UiIHg9IjAiIHk9IjAiIHdpZHRoPSIzMiIgaGVpZ2h0PSIzMiI+CiAgICA8cGF0aCBmaWxsLXJ1bGU9ImV2ZW5vZGQiIGNsaXAtcnVsZT0iZXZlbm9kZCIgZD0iTTE2IDMyQzI0LjgzNjYgMzIgMzIgMjQuODM2NiAzMiAxNkMzMiA3LjE2MzQ0IDI0LjgzNjYgMCAxNiAwQzcuMTYzNDQgMCAwIDcuMTYzNDQgMCAxNkMwIDI0LjgzNjYgNy4xNjM0NCAzMiAxNiAzMloiIGZpbGw9IndoaXRlIi8+CiAgPC9tYXNrPgogIDxnIG1hc2s9InVybCgjbWFzazApIj4KICAgIDxyZWN0IHg9IjIiIHk9IjIiIHdpZHRoPSIyOCIgaGVpZ2h0PSIyOCIgZmlsbD0iIzAwNUNCOSIvPgogICAgPHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik0yMy41IDIyLjkzMzhDMjMuNSAyMy4xNzU0IDIzLjMwNDEgMjMuMzcxMyAyMy4wNjI1IDIzLjM3MTNIOS45Mzc1QzkuNjk1ODggMjMuMzcxMyA5LjUgMjMuMTc1NCA5LjUgMjIuOTMzOFYyMS42MjE3QzkuNSAxOSAxMS40NjQ5IDE3LjA3NDEgMTMuNjYxOSAxNi4wOTgxQzEyLjcyMTMgMTUuMjE1NiAxMi4xMjUgMTMuOTAyNiAxMi4xMjUgMTIuNDM2NEMxMi4xMjUgOS43NzkxMiAxNC4wODM4IDcuNjI1IDE2LjUgNy42MjVDMTguOTE2MiA3LjYyNSAyMC44NzUgOS43NzkxMiAyMC44NzUgMTIuNDM2NEMyMC44NzUgMTMuOTAxOSAyMC4yNzkyIDE1LjIxNDQgMTkuMzM5NCAxNi4wOTY5QzIxLjUzODQgMTcuMDcyMiAyMy41IDE4Ljk5NTYgMjMuNSAyMS42MjE3VjIyLjkzMzhaTTExLjMyMjggMjEuNjIxN0gyMS43NUMyMS43NSAxOSAxOS41NjI1IDE3LjI0NzcgMTYuNSAxNy4yNDc3QzEzLjQzNzUgMTcuMjQ3NyAxMS4yNSAxOSAxMS4zMjI4IDIxLjYyMTdaTTE2LjUgMTUuNDk4MUMxNy45NDk3IDE1LjQ5ODEgMTkuMTI1IDE0LjEyNzMgMTkuMTI1IDEyLjQzNjRDMTkuMTI1IDEwLjc0NTQgMTcuOTQ5NyA5LjM3NDU5IDE2LjUgOS4zNzQ1OUMxNS4wNTAzIDkuMzc0NTkgMTMuODc1IDEwLjc0NTQgMTMuODc1IDEyLjQzNjRDMTMuODc1IDE0LjEyNzMgMTUuMDUwMyAxNS40OTgxIDE2LjUgMTUuNDk4MVoiIGZpbGw9IndoaXRlIi8+CiAgPC9nPgo8L3N2Zz4K";
      speakers.getUsers().add(new SpeakerPojo(profileImageData, "adavis36", "fake_first_name", "fake_last_name",
          "aaron.davis@workday.com.uat", "0031B00002kka6hQAA"));
      lenient().when(objectMapper.readValue(inputStream, HashMap.class)).thenReturn(result);
      lenient().when(objectMapper.readValue(inputStream, Speakers.class)).thenReturn(speakers);
      PrintWriter pr = mock(PrintWriter.class);
      lenient().when(response.getWriter()).thenReturn(pr);
      speakersServlet.setObjectMapper(this.objectMapper);
      try {
        speakersServlet.doGet(request, response);
      } catch (ServletException | IOException exception) {
        // do nothing.
      }
    }
  }
}