package com.workday.community.aem.core.pojos.restclient;

import java.net.URI;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

/**
 * Class for wrapping API requests.
 */
@Getter
public class ApiRequest {
  /**
   * The request body.
   */
  @Setter private String body;

  /**
   * The method used in request.
   */
  @Setter private String method;

  /**
   * The url of request.
   */
  @Setter private String url;

  /**
   * The request headers.
   */
  private final HashMap<String, String> headers;

  /**
   * The request form data.
   */
  private final HashMap<String, String> formData;

  public ApiRequest() {
    this.headers = new HashMap<>();
    this.formData = new HashMap<>();
  }

  /**
   * Getter for Uri.
   */
  public URI getUri() {
    return URI.create(url);
  }

  /**
   * Adds a header in the request headers.
   *
   * @param header the current header name.
   * @param value  the header value.
   */
  public ApiRequest addHeader(String header, String value) {
    this.headers.put(header, value);

    return this;
  }

  /**
   * Adds a key value in the request form data.
   *
   * @param key   the key name.
   * @param value the value.
   */
  public ApiRequest addFormData(String key, String value) {
    this.formData.put(key, value);

    return this;
  }

}
