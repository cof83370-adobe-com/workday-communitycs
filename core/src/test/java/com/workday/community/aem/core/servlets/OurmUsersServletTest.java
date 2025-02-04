package com.workday.community.aem.core.servlets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workday.community.aem.core.exceptions.DrupalException;
import com.workday.community.aem.core.services.DrupalService;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class OurmUsersServletTest.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class OurmUsersServletTest {

  private final Gson gson = new Gson();

  /**
   * The DrupalService.
   */
  @Mock
  DrupalService drupalService;

  /**
   * The ourmUsers servlet.
   */
  @InjectMocks
  OurmUsersServlet ourmUsersServlet;

  /**
   * Setup.
   */
  @BeforeEach
  public void setup() {
  }

  /**
   * Test do get.
   *
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws DrupalException  the drupal exception
   * @throws ServletException the servlet exception
   */
  @Test
  public void testDoGet() throws IOException, DrupalException, ServletException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

    when(request.getParameter("searchText")).thenReturn("fakeText");

    String testUserContext =
        "{\"users\":[{\"sfId\":\"fakeSfId\",\"username\":\"fakeUserName\",\"firstName\":\"fake_first_name\",\"lastName\":\"fake_last_name\",\"email\":\"fakeEmail\",\"profileImageData\":\"fakeProfileData\"}]}";
    JsonObject userContext = gson.fromJson(testUserContext, JsonObject.class);
    when(drupalService.searchOurmUserList(anyString())).thenReturn(userContext);
    PrintWriter pr = mock(PrintWriter.class);
    lenient().when(response.getWriter()).thenReturn(pr);
    ourmUsersServlet.doGet(request, response);
    assertNotNull(response);
  }
}
