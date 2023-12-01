package com.workday.community.aem.core.services;

import org.apache.commons.mail.EmailException;
import org.osgi.annotation.versioning.ProviderType;

/**
 * The Email definition interface.
 */
@ProviderType
public interface EmailService {

  /**
   * Send Email.
   */
  void sendEmail(String emailTo, String subject, String message) throws EmailException;

}
