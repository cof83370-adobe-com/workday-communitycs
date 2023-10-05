package com.workday.community.aem.core.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * The utility class to generate OAuth authorization header contents. This
 * authorization header conforms to OAuth 1.0A specification.
 *
 * @author Gourab Sarkar
 */
public class OAuth1Util {

  private static final Random RANDOM = new Random();

  private static final String OAUTH_SIGNATURE_ALG_HEADER = "HMAC-SHA1";

  private static final String OAUTH_VER_HEADER = "1.0";

  private static final String OAUTH_CONSUMER_KEY_HEADER = "oauth_consumer_key";

  private static final String OAUTH_SIGNATURE_METHOD_HEADER = "oauth_signature_method";

  private static final String OAUTH_TIMESTAMP_HEADER = "oauth_timestamp";

  private static final String OAUTH_NONCE_HEADER = "oauth_nonce";

  private static final String OAUTH_VERSION_HEADER = "oauth_version";

  private static final String OAUTH_SIGNATURE_HEADER = "oauth_signature";

  private static final String HMAC_SHA1_ALG = "HmacSHA1";

  private static final String OAUTH_AUTH_PREAMBLE = "OAuth ";

  private static final String EQUALS_LITERAL = "=";

  private static final String AMPERSAND_LITERAL = "&";

  private static final char ASTERISK_LITERAL = '*';

  private static final String ASTERISK_PE_REPLACEMENT = "%2A";

  private static final char PLUS_LITERAL = '+';

  private static final String PLUS_PE_REPLACEMENT = "%20";

  private static final String PERCENT7E_LITERAL = "%7E";

  private static final String PERCENT7E_PE_REPLACEMENT = "~";

  /**
   * Generates OAuth 1.0A header which can be passed as Authorization header.
   *
   * @param httpMethod    Method.
   * @param url           Url of the API.
   * @param requestParams request.
   * @return String
   * @throws NoSuchAlgorithmException NoSuchAlgorithmException exception.
   * @throws InvalidKeyException      InvalidKeyException exception.
   */
  public static String getHeader(String httpMethod, String url, String consumerKey,
                                 String consumerSecret,
                                 Map<String, String> requestParams)
      throws NoSuchAlgorithmException, InvalidKeyException {
    StringBuilder base = new StringBuilder();

    final String nonce = getNonce();
    final String timestamp = getTimestamp();
    final String baseSignatureString =
        generateSignatureBaseString(httpMethod, url, consumerKey, requestParams, nonce,
            timestamp);
    final String signature = generateSignature(baseSignatureString, consumerSecret);

    base.append(OAUTH_AUTH_PREAMBLE);
    append(base, OAUTH_CONSUMER_KEY_HEADER, consumerKey);
    append(base, OAUTH_SIGNATURE_METHOD_HEADER, OAUTH_SIGNATURE_ALG_HEADER);
    append(base, OAUTH_TIMESTAMP_HEADER, timestamp);
    append(base, OAUTH_NONCE_HEADER, nonce);
    append(base, OAUTH_VERSION_HEADER, OAUTH_VER_HEADER);
    append(base, OAUTH_SIGNATURE_HEADER, signature);
    base.deleteCharAt(base.length() - 1);
    return base.toString();
  }

  /**
   * Generate base signature string to generate the oauth_signature.
   *
   * @param httpMethod    method.
   * @param url           url of the api.
   * @param requestParams request.
   * @return String
   */
  private static String generateSignatureBaseString(final String httpMethod, final String url,
                                                    String consumerKey,
                                                    final Map<String, String> requestParams,
                                                    final String nonce, final String timestamp) {
    Map<String, String> params = new HashMap<>();
    requestParams.forEach((key1, value1) -> putEncoded(params, key1, value1));
    putEncoded(params, OAUTH_CONSUMER_KEY_HEADER, consumerKey);
    putEncoded(params, OAUTH_NONCE_HEADER, nonce);
    putEncoded(params, OAUTH_SIGNATURE_METHOD_HEADER, OAUTH_SIGNATURE_ALG_HEADER);
    putEncoded(params, OAUTH_TIMESTAMP_HEADER, timestamp);
    putEncoded(params, OAUTH_VERSION_HEADER, OAUTH_VER_HEADER);

    Map<String, String> sortedParams = params.entrySet().stream().sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (oldValue, newValue) -> oldValue,
            LinkedHashMap::new));
    StringBuilder baseSignatureString = new StringBuilder();
    sortedParams.forEach((key, value) -> baseSignatureString.append(key)
        .append(EQUALS_LITERAL).append(value).append(AMPERSAND_LITERAL));

    baseSignatureString.deleteCharAt(baseSignatureString.length() - 1);
    return httpMethod.toUpperCase() + AMPERSAND_LITERAL + percentEncode(url)
        + AMPERSAND_LITERAL + percentEncode(baseSignatureString.toString());
  }

  /**
   * Encrypts and encodes the base signature string using HMAC-SHA1 algorithm to
   * generate the OAuth signature as per OAuth 1.0A specification.
   *
   * @param signatureBaseString signature.
   * @return String
   * @throws NoSuchAlgorithmException NoSuchAlgorithmException exception.
   * @throws InvalidKeyException      InvalidKeyException
   */
  private static String generateSignature(final String signatureBaseString, String consumerSecret)
      throws NoSuchAlgorithmException, InvalidKeyException {
    final String secret = percentEncode(consumerSecret) +
        AMPERSAND_LITERAL;
    final byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    final SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1_ALG);

    final Mac mac = Mac.getInstance(HMAC_SHA1_ALG);
    mac.init(key);
    final byte[] signatureBytes = mac.doFinal(signatureBaseString.getBytes(StandardCharsets.UTF_8));
    return new String(Base64.getEncoder().encode(signatureBytes));
  }

  /**
   * Percentage encode String as per RFC 3986, Section 2.1.
   *
   * @param value String to encode
   * @return String
   */
  private static String percentEncode(String value) {
    String encoded;
    encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);

    // Process encoded string to avoid issues with the auth provider.
    StringBuilder builder = new StringBuilder();
    char focus;
    for (int i = 0; i < encoded.length(); i++) {
      focus = encoded.charAt(i);
      if (focus == ASTERISK_LITERAL) {
        builder.append(ASTERISK_PE_REPLACEMENT);
      } else if (focus == PLUS_LITERAL) {
        builder.append(PLUS_PE_REPLACEMENT);
      } else if (focus == PERCENT7E_LITERAL.charAt(0) && i + 1 < encoded.length()
          && encoded.charAt(i + 1) == PERCENT7E_LITERAL.charAt(1)
          && encoded.charAt(i + 2) == PERCENT7E_LITERAL.charAt(2)) {
        // if "%7" is present, then it's definitely "%7E".
        builder.append(PERCENT7E_PE_REPLACEMENT);
      } else {
        builder.append(focus);
      }
    }

    return builder.toString();
  }

  /**
   * Encodes and puts entries inside the specified {@link Map}.
   *
   * @param map   map.
   * @param key   key.
   * @param value value.
   */
  private static void putEncoded(Map<String, String> map, String key, String value) {
    map.put(percentEncode(key), percentEncode(value));
  }

  /**
   * Encodes and appends the specified key-value pair to a {@link StringBuilder}.
   *
   * @param builder String builder.
   * @param key     key.
   * @param value   value.
   */
  private static void append(StringBuilder builder, String key, String value) {
    builder.append(percentEncode(key)).append("=\"").append(percentEncode(value)).append("\",");
  }

  /**
   * Generates the "NONCE" attribute for the OAuth 1.0A header.
   *
   * @return String
   */
  private static String getNonce() {
    // Returns an alphanumeric pseudorandom string.
    return RANDOM.ints(48, 123).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(10)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
  }

  /**
   * Generates timestamp as per OAuth 1.0A specification.
   *
   * @return String
   */
  private static String getTimestamp() {
    return String.valueOf(System.currentTimeMillis() / 1000);
  }

}
