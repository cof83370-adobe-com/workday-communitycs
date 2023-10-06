package com.workday.community.aem.core.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.workday.community.aem.core.constants.RestApiConstants;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;
import org.apache.sling.api.servlets.HttpConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class HttpsURLConnectionServiceTest.
 */
@ExtendWith(MockitoExtension.class)
public class HttpsURLConnectionServiceTest {

  /**
   * The service HttpsURLConnectionService.
   */
  @Spy
  HttpsUrlConnectionService service;

  /**
   * The HttpsURLConnection.
   */
  @Mock
  HttpsURLConnection request;

  /**
   * The header.
   */
  HashMap<String, String> header = new HashMap<String, String>();

  /**
   * The api uri.
   */
  String url = "https://www.test/com/";

  /**
   * Test send api call successfully.
   *
   * @throws IOException The exception
   */
  @Test
  public void testSendSccessed() throws IOException {
    header.put(RestApiConstants.CONTENT_TYPE, RestApiConstants.APPLICATION_SLASH_JSON);
    int expected = 200;
    doReturn(request).when(service).getHttpsUrlConnection(any());
    doReturn(expected).when(request).getResponseCode();
    String response = "response";
    InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
    doReturn(stream).when(request).getInputStream();
    HashMap<String, Object> apiResponse = service.send(url, header, "GET", "");
    Assertions.assertEquals(response, apiResponse.get("response").toString());
  }

  /**
   * Test send api call failed.
   *
   * @throws IOException The exception
   */
  @Test
  public void testSendFailed() throws IOException {
    header.put(HttpConstants.HEADER_ACCEPT, RestApiConstants.APPLICATION_SLASH_JSON);
    int expected = 403;
    doReturn(request).when(service).getHttpsUrlConnection(any());
    doReturn(expected).when(request).getResponseCode();
    String response = "";
    InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
    doReturn(stream).when(request).getInputStream();
    HashMap<String, Object> apiResponse = service.send(url, header, "GET", "");
    Assertions.assertEquals("", apiResponse.get("response"));
  }
}
