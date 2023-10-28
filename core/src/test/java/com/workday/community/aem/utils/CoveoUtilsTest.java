package com.workday.community.aem.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workday.community.aem.core.services.UserService;
import com.workday.community.aem.core.services.DrupalService;
import com.workday.community.aem.core.utils.CoveoUtils;
import com.workday.community.aem.core.utils.OurmUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class CoveoUtilsTest {
  @Test
  void testGetCurrentUserProfile() {
    String testData =
        "{\"success\":true,\"contactId\":\"sadsadadsa\",\"email\":\"foo@fiooo.com\",\"timeZone\":\"America/Los_Angeles\",\"contextInfo\":{\"functionalArea\":\"Other\",\"contactRole\":\"Workmate;Workday-professionalservices;workday;workday_professional_services;BetaUser\",\"productLine\":\"Other\",\"superIndustry\":\"Communications,Media&Technology\",\"isWorkmate\":true,\"type\":\"customer\"},\"contactInformation\":{\"propertyAccess\":\"Community\",\"nscSupporting\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"wsp\":\"WSP-Guided\",\"lastName\":\"Zhang\",\"firstName\":\"Wangchun\",\"customerOf\":\"Workday;Scout;AdaptivePlanning;Peakon;VNDLY\",\"customerSince\":\"2019-01-28\"}}";
    JsonParser gsonParser = new JsonParser();
    JsonObject testDataObject = gsonParser.parse(testData).getAsJsonObject();
    JsonObject userContext = testDataObject.get("contextInfo").getAsJsonObject();

    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    DrupalService drupalService = mock(DrupalService.class);
    UserService userService = mock(UserService.class);
    try (MockedStatic<OurmUtils> mockOurmUtils = mockStatic(OurmUtils.class)) {
      mockOurmUtils.when(() -> OurmUtils.getSalesForceId(any(), any())).thenReturn("testSFID");
      lenient().when(drupalService.getUserContext(eq("testSFID"))).thenReturn(userContext);
      String ret = CoveoUtils.getCurrentUserContext(request, drupalService, userService);
      assertEquals(ret,
          "{\"functionalArea\":\"Other\",\"contactRole\":\"Workmate;Workday-professionalservices;workday;workday_professional_services;BetaUser\",\"productLine\":\"Other\",\"superIndustry\":\"Communications,Media&Technology\",\"isWorkmate\":true,\"type\":\"customer\"}");
    }
  }
}
