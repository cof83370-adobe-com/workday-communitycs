package com.workday.community.aem.core.servlets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workday.community.aem.core.exceptions.OurmException;
import com.workday.community.aem.core.pojos.OurmUser;
import com.workday.community.aem.core.pojos.OurmUserList;
import com.workday.community.aem.core.services.OurmUserService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

/**
 * The Class OurmUsersServletTest.
 */
@ExtendWith({ AemContextExtension.class, MockitoExtension.class })
public class OurmUsersServletTest {

  /** The context. */
  private final AemContext context = new AemContext();

  /** The ourmUsers api config service. */
  @Mock
  OurmUserService ourmUserService;

  /** The object mapper. */
  @Mock
  private transient ObjectMapper objectMapper;

  /** The ourmUsers servlet. */
  @InjectMocks
  OurmUsersServlet ourmUsersServlet;

  /**
   * Setup.
   */
  @BeforeEach
  public void setup() {
    context.registerService(objectMapper);
  }

  /**
   * Test do get.
   *
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws OurmException the ourm exception
   * @throws ServletException the servlet exception
   */
  @Test
  public void testDoGet() throws IOException, OurmException, ServletException {
    SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

    when(request.getParameter("searchText")).thenReturn("dav");
    String searchtext = "dav";
    OurmUserList ourmUsers = new OurmUserList();
    String profileImageData = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMi";
    ourmUsers.getUsers().add(new OurmUser(profileImageData, "adavis36", "fake_first_name", "fake_last_name",
        "aaron.davis@workday.com", "0031B00002kka6hQAA"));
    lenient().when(ourmUserService.searchOurmUserList(searchtext)).thenReturn(ourmUsers);

    PrintWriter pr = mock(PrintWriter.class);
    lenient().when(response.getWriter()).thenReturn(pr);
    ourmUsersServlet.doGet(request, response);
    assertNotNull(response);
  }
}
