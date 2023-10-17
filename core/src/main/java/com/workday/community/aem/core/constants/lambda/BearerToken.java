package com.workday.community.aem.core.constants.lambda;

/**
 * The functional interface for forming Bearer Token.
 */
public interface BearerToken {

  /**
   * Returns a string that can be passed with the "Authorization" HTTP header.
   *
   * @param token A bearer token.
   * @return The formatted header.
   */
  String token(String token);

}
