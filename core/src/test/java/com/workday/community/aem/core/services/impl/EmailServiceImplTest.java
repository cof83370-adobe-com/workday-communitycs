package com.workday.community.aem.core.services.impl;

import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.workday.community.aem.core.services.BookOperationsService;
import com.workday.community.aem.core.services.EmailService;
import com.workday.community.aem.core.services.QueryService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.mockito.Mockito.lenient;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * EmailServiceImplTest class.
 */
@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EmailServiceImplTest {
  /**
   * The email service impl.
   */
  private EmailService emailServiceImpl;

  /**
   * AemContext
   */
  private final AemContext context = new AemContext();

  @Mock
  MessageGatewayService messageGatewayService;
  
  @Mock
  MessageGateway<Email> messageGatewaySimpleEmail;

  /**
   * Set up method for test run.
   */
  @BeforeEach
  public void setup() {
	lenient().when(messageGatewayService.getGateway(Email.class)).thenReturn(messageGatewaySimpleEmail);  
	  
    context.registerService(MessageGatewayService.class, messageGatewayService);
    emailServiceImpl =
        context.registerInjectActivateService(new EmailServiceImpl());
  }

  /**
   * Test method for sendEmail method.
   *
   * @throws EmailException EmailException
   */
  @Test
  public void testSendEmail() throws EmailException {
	  final String expectedMessage = "This is just a message";
      final String expectedSenderName = "John Smith";
      final String expectedSenderEmailAddress = "john@smith.com";

      final Map<String, String> params = new HashMap<String, String>();
      params.put("message", expectedMessage);
      params.put("senderName", expectedSenderName);
      params.put("senderEmailAddress", expectedSenderEmailAddress);
      
      emailServiceImpl.sendEmail(expectedSenderEmailAddress, expectedMessage, "to retire in days");
  }
}
