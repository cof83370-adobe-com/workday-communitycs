package com.workday.community.aem.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workday.community.aem.core.utils.UUIDUtil;
import org.junit.jupiter.api.Test;

/**
 * The UUIDUtilTest class.
 */
public class UUIDUtilTest {
  @Test
  void testGetUserClientId() {
    String uuid = UUIDUtil.getUserClientId("foo@workday.com").toString();
    String uuid1 = UUIDUtil.getUserClientId("foo@workday.com").toString();
    assertEquals(uuid, uuid1);
  }
}
