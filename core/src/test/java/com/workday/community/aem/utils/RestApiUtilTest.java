package com.workday.community.aem.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.exceptions.LmsException;
import com.workday.community.aem.core.exceptions.SnapException;
import com.workday.community.aem.core.utils.RestApiUtil;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * The Class RESTAPIUtilTest.
 */
public class RestApiUtilTest {
  @Test
  public void testDoGetMenu()
      throws SnapException, IOException, InterruptedException {
    HttpClient httpClient = mock(HttpClient.class);
    java.net.http.HttpClient.Builder clientBuilder = mock(java.net.http.HttpClient.Builder.class);

    Builder requestBuilder = mock(Builder.class);
    HttpRequest request = mock(HttpRequest.class);

    try (MockedStatic<HttpClient> mockedClient = mockStatic(HttpClient.class);
         MockedStatic<HttpRequest> mockedrequest = mockStatic(HttpRequest.class)) {
      mockedClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);
      mockedrequest.when(HttpRequest::newBuilder).thenReturn(requestBuilder);
      lenient().when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
      lenient().when(clientBuilder.build()).thenReturn(httpClient);

      lenient().when(requestBuilder.uri(any())).thenReturn(requestBuilder);
      lenient().when(requestBuilder.GET()).thenReturn(requestBuilder);
      lenient().when(requestBuilder.build()).thenReturn(request);

      HttpResponse response = mock(HttpResponse.class);
      lenient().when(httpClient.send(any(), any())).thenReturn(response);

      when(response.statusCode()).thenReturn(200);
      when(response.body()).thenReturn("");

      RestApiUtil.doMenuGet("url", "apiToken", "apiKey", "traceId");
      RestApiUtil.doSnapGet("url", "authToken", "xapiKey");
    }
  }

  /**
   * Test method for doLmsTokenGet.
   */
  @Test
  public void testDoLmsTokenGet()
      throws LmsException, IOException, InterruptedException {
    HttpClient httpClient = mock(HttpClient.class);
    java.net.http.HttpClient.Builder clientBuilder = mock(java.net.http.HttpClient.Builder.class);

    Builder requestBuilder = mock(Builder.class);
    HttpRequest request = mock(HttpRequest.class);

    try (MockedStatic<HttpClient> mockedClient = mockStatic(HttpClient.class);
         MockedStatic<HttpRequest> mockedrequest = mockStatic(HttpRequest.class)) {
      mockedClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);
      mockedrequest.when(HttpRequest::newBuilder).thenReturn(requestBuilder);
      lenient().when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
      lenient().when(clientBuilder.build()).thenReturn(httpClient);

      lenient().when(requestBuilder.uri(any())).thenReturn(requestBuilder);
      lenient().when(requestBuilder.POST(any())).thenReturn(requestBuilder);
      lenient().when(requestBuilder.build()).thenReturn(request);

      HttpResponse response = mock(HttpResponse.class);
      lenient().when(httpClient.send(any(), any())).thenReturn(response);

      when(response.statusCode()).thenReturn(200);
      when(response.body()).thenReturn("");

      RestApiUtil.doLmsTokenGet("url", "clientId", "clientSecret", "refreshToken");
    }
  }

  /**
   * Test method for doLmsCourseDetailGet.
   */
  @Test
  public void testDoLmsCourseDetailGet()
      throws LmsException, IOException, InterruptedException {
    HttpClient httpClient = mock(HttpClient.class);
    java.net.http.HttpClient.Builder clientBuilder = mock(java.net.http.HttpClient.Builder.class);

    Builder requestBuilder = mock(Builder.class);
    HttpRequest request = mock(HttpRequest.class);

    try (MockedStatic<HttpClient> mockedClient = mockStatic(HttpClient.class);
         MockedStatic<HttpRequest> mockedrequest = mockStatic(HttpRequest.class)) {
      mockedClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);
      mockedrequest.when(HttpRequest::newBuilder).thenReturn(requestBuilder);
      lenient().when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
      lenient().when(clientBuilder.build()).thenReturn(httpClient);

      lenient().when(requestBuilder.uri(any())).thenReturn(requestBuilder);
      lenient().when(requestBuilder.GET()).thenReturn(requestBuilder);
      lenient().when(requestBuilder.build()).thenReturn(request);

      HttpResponse response = mock(HttpResponse.class);
      lenient().when(httpClient.send(any(), any())).thenReturn(response);

      when(response.statusCode()).thenReturn(200);
      when(response.body()).thenReturn("");

      RestApiUtil.doLmsCourseDetailGet("url", "bearerToken");
    }
  }

  /**
   * Test method for doDrupalTokenGet.
   */
  @Test
  public void testDoDrupalTokenGet()
      throws DrupalException, IOException, InterruptedException {
    HttpClient httpClient = mock(HttpClient.class);
    java.net.http.HttpClient.Builder clientBuilder = mock(java.net.http.HttpClient.Builder.class);

    Builder requestBuilder = mock(Builder.class);
    HttpRequest request = mock(HttpRequest.class);

    try (MockedStatic<HttpClient> mockedClient = mockStatic(HttpClient.class);
        MockedStatic<HttpRequest> mockedrequest = mockStatic(HttpRequest.class)) {
      mockedClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);
      mockedrequest.when(HttpRequest::newBuilder).thenReturn(requestBuilder);
      lenient().when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
      lenient().when(clientBuilder.build()).thenReturn(httpClient);

      lenient().when(requestBuilder.uri(any())).thenReturn(requestBuilder);
      lenient().when(requestBuilder.POST(any())).thenReturn(requestBuilder);
      lenient().when(requestBuilder.build()).thenReturn(request);

      HttpResponse response = mock(HttpResponse.class);
      lenient().when(httpClient.send(any(), any())).thenReturn(response);

      when(response.statusCode()).thenReturn(200);
      when(response.body()).thenReturn("");

      RestApiUtil.doDrupalTokenGet("url", "clientId", "clientSecret");
    }
  }

  /**
   * Test method for doDrupalUserDataGet.
   */
  @Test
  public void testDoDrupalUserDataGet()
      throws DrupalException, IOException, InterruptedException {
    HttpClient httpClient = mock(HttpClient.class);
    java.net.http.HttpClient.Builder clientBuilder = mock(java.net.http.HttpClient.Builder.class);

    Builder requestBuilder = mock(Builder.class);
    HttpRequest request = mock(HttpRequest.class);

    try (MockedStatic<HttpClient> mockedClient = mockStatic(HttpClient.class);
        MockedStatic<HttpRequest> mockedrequest = mockStatic(HttpRequest.class)) {
      mockedClient.when(HttpClient::newBuilder).thenReturn(clientBuilder);
      mockedrequest.when(HttpRequest::newBuilder).thenReturn(requestBuilder);
      lenient().when(clientBuilder.connectTimeout(any())).thenReturn(clientBuilder);
      lenient().when(clientBuilder.build()).thenReturn(httpClient);

      lenient().when(requestBuilder.uri(any())).thenReturn(requestBuilder);
      lenient().when(requestBuilder.GET()).thenReturn(requestBuilder);
      lenient().when(requestBuilder.build()).thenReturn(request);

      HttpResponse response = mock(HttpResponse.class);
      lenient().when(httpClient.send(any(), any())).thenReturn(response);

      when(response.statusCode()).thenReturn(200);
      when(response.body()).thenReturn("");

      RestApiUtil.doDrupalGet("url", "bearerToken");
    }
  }
}
