package com.workday.community.aem.utils;

import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.utils.RestApiUtil;

import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * The Class RESTAPIUtilTest.
 */
public class RestApiUtilTest {
  @Test
  public void testDoGetMenu() throws SnapException, IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    HttpClientBuilder builder = mock(HttpClientBuilder.class);
    HttpClientBuilder clientBuilder = mock(HttpClientBuilder.class);

    try (MockedStatic<HttpClients> MockedHttpClients = mockStatic(HttpClients.class)) {
      MockedHttpClients.when(HttpClients::custom).thenReturn(builder);
      lenient().when(builder.setDefaultRequestConfig(any())).thenReturn(clientBuilder);
      lenient().when(clientBuilder.build()).thenReturn(httpClient);

      CloseableHttpResponse response = mock(CloseableHttpResponse.class);
      lenient().when(httpClient.execute(any())).thenReturn(response);

      StatusLine statusLine = new StatusLine() {
        @Override
        public ProtocolVersion getProtocolVersion() {
          return null;
        }

        @Override
        public int getStatusCode() {
          return 200;
        }

        @Override
        public String getReasonPhrase() {
          return null;
        }
      };

      when(response.getStatusLine()).thenReturn(statusLine);
      HttpEntity entity = mock(HttpEntity.class);
      when(response.getEntity()).thenReturn(entity);

      RestApiUtil.doGetMenu("url", "apiToken", "apiKey", "traceId");
      RestApiUtil.doSnapGet("url", "authToken", "xapiKey");
    }

  }
}
