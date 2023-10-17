package com.workday.community.aem.core.pojos.restclient;

import lombok.Getter;
import lombok.Setter;

/**
 * Class for wrapping API responses.
 */
@Getter
@Setter
public class ApiResponse {

  /**
   * The response body.
   */
  private String responseBody;

  /**
   * The response code.
   */
  private int responseCode;

}
