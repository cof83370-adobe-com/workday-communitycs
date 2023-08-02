package com.workday.community.aem.utils;

import com.workday.community.aem.core.exceptions.RestAPIException;
import com.workday.community.aem.core.utils.RestApiUtil;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;

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
  public void testDoGetMenu()
      throws RestAPIException, IOException, InterruptedException {
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
   * Test method for doLMSTokenPost.
   * 
   * @throws RestAPIException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testDoLMSTokenPost()
      throws RestAPIException, IOException, InterruptedException {
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

      RestApiUtil.doLMSTokenPost("url", "clientId", "clientSecret", "refreshToken");
    }
  }

  /**
   * Test method for doLMSCourseDetailGet.
   * 
   * @throws RestAPIException
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testDoLMSCourseDetailGet()
      throws RestAPIException, IOException, InterruptedException {
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

      RestApiUtil.doLMSCourseDetailGet("url", "bearerToken");
    }
  }
}
