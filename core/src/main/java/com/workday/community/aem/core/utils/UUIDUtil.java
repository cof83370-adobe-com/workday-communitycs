package com.workday.community.aem.core.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class UUIDUtil {
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private static final UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");

  /**
   * Generate user's client id in default namespace with given email address.
   *
   * @param email The pass-in user's email address as string
   * @return the user's client id as UUID.
   */
  public static UUID getUserClientId(String email) {
    if (StringUtils.isEmpty(email)) {
      return null;
    }

    byte[] name = Objects.requireNonNull(email, "name == null").getBytes(UTF8);
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new InternalError("SHA-256 not supported");
    }
    md.update(toBytes(Objects.requireNonNull(NAMESPACE_URL, "namespace is null")));
    md.update(Objects.requireNonNull(name, "name is null"));
    byte[] sha1Bytes = md.digest();
    sha1Bytes[6] &= 0x0f;
    sha1Bytes[6] |= 0x50;
    sha1Bytes[8] &= 0x3f;
    sha1Bytes[8] |= 0x80;
    return fromBytes(sha1Bytes);
  }

  private static UUID fromBytes(byte[] data) {
    long msb = 0;
    long lsb = 0;
    assert data.length >= 16;
    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (data[i] & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (data[i] & 0xff);
    }
    return new UUID(msb, lsb);
  }

  private static byte[] toBytes(UUID uuid) {
    byte[] out = new byte[16];
    long msb = uuid.getMostSignificantBits();
    long lsb = uuid.getLeastSignificantBits();
    for (int i = 0; i < 8; i++) {
      out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
    }
    return out;
  }
}
