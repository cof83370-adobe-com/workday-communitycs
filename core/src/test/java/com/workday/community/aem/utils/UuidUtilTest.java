package com.workday.community.aem.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workday.community.aem.core.utils.UuidUtil;
import org.junit.jupiter.api.Test;

/**
 * The UUIDUtilTest class.
 */
public class UuidUtilTest {
  @Test
  void testGetUserClientId() {
    String uuid = UuidUtil.getUserClientId("foo@workday.com").toString();
    String uuid1 = UuidUtil.getUserClientId("foo@workday.com").toString();
    assertEquals(uuid, uuid1);
  }
}
