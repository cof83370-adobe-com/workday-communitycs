package com.workday.community.aem.core.services.impl;

import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.workday.community.aem.core.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * The EmailService implementation class.
 */
@Slf4j
@Component(service = { EmailService.class }, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
public class EmailServiceImpl implements EmailService {

  /** The message gateway service. */
  @Reference
  private MessageGatewayService messageGatewayService;

  /**
   * Activates the Email service.
   */
  @Activate
  @Modified
  public void activate() {
    log.debug("Activate Email service.");
  }

  /**
   * Deactivates the Email service.
   */
  @Deactivate
  public void deactivate() {
    log.debug("Deactivate Email service.");
  }

  /**
   * Send Email. Mail will be send to particular user.
   *
   * @param emailTo the emailTo
   * @param subject the subject
   * @param message the message
   */
  @Override
  public void sendEmail(String emailTo, String subject, String message) throws EmailException {
    log.debug("in sendEmail >>");
    Email email = new SimpleEmail();
    email.addTo(emailTo);
    email.setSubject(subject);
    email.setMsg(message);
    MessageGateway<Email> messageGateway;

    // Inject a Messagegateway Service and send the message
    messageGateway = messageGatewayService.getGateway(Email.class);

    // check the logs to see that messageGateway is not null
    messageGateway.send(email);
  }

}
