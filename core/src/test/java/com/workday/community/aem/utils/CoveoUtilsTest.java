package com.workday.community.aem.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.services.JcrUserService;
import com.workday.community.aem.core.services.SnapService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class CoveoUtilsTest {
  @Test
  void testGetCurrentUserProfile() {
    String testData = "{\"success\":true,\"contactId\":\"sadsadadsa\",\"email\":\"foo@fiooo.com\",\"timeZone\":\"America/Los_Angeles\",\"contextInfo\":{\"functionalArea\":\"Other\",\"contactRole\":\"Workmate;Workday-professionalservices;workday;workday_professional_services;BetaUser\",\"productLine\":\"Other\",\"superIndustry\":\"Communications,Media&Technology\",\"isWorkmate\":true,\"type\":\"customer\"},\"contactInformation\":{\"propertyAccess\":\"Community\",\"nscSupporting\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"wsp\":\"WSP-Guided\",\"lastName\":\"Zhang\",\"firstName\":\"Wangchun\",\"customerOf\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"customerSince\":\"2019-01-28\"}}";
    JsonParser gsonParser = new JsonParser();
    JsonObject userContext = gsonParser.parse(testData).getAsJsonObject();

    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SnapService snapService = mock(SnapService.class);
    JcrUserService userService = mock(JcrUserService.class);
    try (MockedStatic<OurmUtils> mockOurmUtils = mockStatic(OurmUtils.class)) {
      mockOurmUtils.when(() -> OurmUtils.getSalesForceId(any(), any())).thenReturn("testSFID");
      lenient().when(snapService.getUserContext(eq("testSFID"))).thenReturn(userContext);
      String ret = CoveoUtils.getCurrentUserContext(request, snapService, userService);
      assertEquals(ret, "{\"functionalArea\":\"Other\",\"contactRole\":\"Workmate;Workday-professionalservices;workday;workday_professional_services;BetaUser\",\"productLine\":\"Other\",\"superIndustry\":\"Communications,Media&Technology\",\"isWorkmate\":true,\"type\":\"customer\"}");
    }
  }
}
