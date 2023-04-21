package com.workday.community.aem.core.constants.lambda;

/**
 * The functional interface for forming Bearer Token.
 */
public interface BearerToken {
  String token(String token);
}
